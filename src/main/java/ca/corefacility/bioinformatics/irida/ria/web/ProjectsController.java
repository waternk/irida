package ca.corefacility.bioinformatics.irida.ria.web;

import java.security.Principal;
import java.util.*;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.enums.ProjectRole;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectUserJoin;
import ca.corefacility.bioinformatics.irida.model.joins.impl.RelatedProjectJoin;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.ria.utilities.DataTable;
import ca.corefacility.bioinformatics.irida.ria.utilities.Formats;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;

import com.google.common.collect.ImmutableMap;

/**
 * Controller for all project related views
 * 
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
@Controller
@RequestMapping(value = "/projects")
public class ProjectsController {
	private static final String PROJECTS_DIR = "projects/";
	private static final String PROJECTS_PAGE = PROJECTS_DIR + "projects";
	private static final String PROJECT_USERS = PROJECTS_DIR + "project_collaborators";
	private static final String SPECIFIC_PROJECT_PAGE = PROJECTS_DIR + "project_details";
	private static final String CREATE_NEW_PROJECT_PAGE = PROJECTS_DIR + "project_new";
	private static final String PROJECT_METADATA_PAGE = PROJECTS_DIR + "project_metadata";
	private static final String SORT_BY_ID = "id";
	private static final String SORT_BY_NAME = "name";
	private static final String SORT_BY_CREATED_DATE = "createdDate";
	private static final String SORT_BY_MODIFIED_DATE = "modifiedDate";
	private static final String SORT_ASCENDING = "asc";
	private static final Logger logger = LoggerFactory.getLogger(ProjectsController.class);
	private final ProjectService projectService;
	private final SampleService sampleService;
	private final UserService userService;

	// Key is the column number in the datatable.
	private final ImmutableMap<Integer, String> COLUMN_SORT_MAP = ImmutableMap.<Integer, String> builder()
			.put(0, SORT_BY_ID).put(1, SORT_BY_NAME).put(5, SORT_BY_CREATED_DATE).put(6, SORT_BY_MODIFIED_DATE).build();

	@Autowired
	public ProjectsController(ProjectService projectService, SampleService sampleService, UserService userService) {
		this.projectService = projectService;
		this.sampleService = sampleService;
		this.userService = userService;
	}

	/**
	 * Request for the page to display a list of all projects available to the
	 * currently logged in user.
	 * 
	 * @return The name of the page.
	 */
	@RequestMapping
	public String getProjectsPage() {
		return PROJECTS_PAGE;
	}

	/**
	 * Request for a specific project details page.
	 * 
	 * @param projectId
	 *            The id for the project to show details for.
	 * @param model
	 *            Spring model to populate the html page.
	 * @return The name of the project details page.
	 */
	@RequestMapping(value = "/{projectId}")
	public String getProjectSpecificPage(@PathVariable Long projectId, final Model model, final Principal principal) {
		logger.debug("Getting project information for [Project " + projectId + "]");
		Project project = projectService.read(projectId);
		model.addAttribute("project", project);
		getProjectTemplateDetails(model, principal, project);
		return SPECIFIC_PROJECT_PAGE;
	}

	private void getProjectTemplateDetails(Model model, Principal principal, Project project) {
		Collection<Join<Project, User>> ownerJoinList = userService.getUsersForProjectByRole(project,
				ProjectRole.PROJECT_OWNER);
		User owner = null;
		if (ownerJoinList.size() > 0) {
			owner = (ownerJoinList.iterator().next()).getObject();
		}
		model.addAttribute("owner", owner);

		int sampleSize = sampleService.getSamplesForProject(project).size();
		model.addAttribute("samples", sampleSize);

		int userSize = userService.getUsersForProject(project).size();
		model.addAttribute("users", userSize);

		// TODO: (Josh - 14-06-23) Get list of recent activities on project.

		// Add any associated projects
		User currentUser = userService.getUserByUsername(principal.getName());
		List<Map<String, String>> associatedProjects = getAssociatedProjects(project, currentUser);
		model.addAttribute("associatedProjects", associatedProjects);
	}

	/**
	 * Gets the name of the template for the project collaborators page.
	 * Populates the template with standard info.
	 * 
	 * @param model
	 *            {@link Model}
	 * @param principal
	 *            {@link Principal}
	 * @param projectId
	 *            Id for the project to show the users for
	 * @return The name of the project collaborators page.
	 */
	@RequestMapping("/{projectId}/collaborators")
	public String getProjectUsersPage(final Model model, final Principal principal, @PathVariable Long projectId) {
		Project project = projectService.read(projectId);
		model.addAttribute("project", project);
		getProjectTemplateDetails(model, principal, project);
		return PROJECT_USERS;
	}

	/**
	 * Gets the name of the template for the new project page
	 * 
	 * @param model
	 *            {@link Model}
	 * @return The name of the create new project page
	 */
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String getCreateProjectPage(final Model model) {
		if (!model.containsAttribute("errors")) {
			model.addAttribute("errors", new HashMap<>());
		}
		return CREATE_NEW_PROJECT_PAGE;
	}

	/**
	 * Creates a new project and displays a list of users for the user to add to
	 * the project
	 * 
	 * @param model
	 *            {@link Model}
	 * @param name
	 *            String name of the project
	 * @param organism
	 *            Organism name
	 * @param projectDescription
	 *            Brief description of the project
	 * @param remoteURL
	 *            URL for the project wiki
	 * @return The name of the add users to project page
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public String createNewProject(final Model model, @RequestParam(required = false, defaultValue = "") String name,
			@RequestParam(required = false, defaultValue = "") String organism,
			@RequestParam(required = false, defaultValue = "") String projectDescription,
			@RequestParam(required = false, defaultValue = "") String remoteURL) {

		Project p = new Project(name);
		p.setOrganism(organism);
		p.setProjectDescription(projectDescription);
		p.setRemoteURL(remoteURL);
		Project project;
		try {
			project = projectService.create(p);
		} catch (ConstraintViolationException e) {
			model.addAttribute("errors", getErrorsFromViolationException(e));
			return getCreateProjectPage(model);
		}

		return "redirect:/projects/" + project.getId() + "/metadata";
	}

	/**
	 * Returns the name of a page to add users to a *new* project.
	 * 
	 * @param model
	 *            {@link Model}
	 * @param projectId
	 *            the id of the project to find the metadata for.
	 * @return The name of the add users to new project page.
	 */
	@RequestMapping("/{projectId}/metadata")
	public String getProjectMetadataPage(final Model model, @PathVariable long projectId) {
		Project p = projectService.read(projectId);
		model.addAttribute("project", p);
		return PROJECT_METADATA_PAGE;
	}

	@RequestMapping(value = "/ajax/{projectId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Map<String, Collection<Join<Project, User>>> getAjaxUsersListForProject(@PathVariable Long projectId) {
		Map<String, Collection<Join<Project, User>>> data = new HashMap<>();
		try {
			Project project = projectService.read(projectId);
			Collection<Join<Project, User>> users = userService.getUsersForProject(project);
			data.put("data", users);
		} catch (Exception e) {
			logger.error("Trying to access a project that does not exist.");
		}
		return data;
	}

	/**
	 * Handles AJAX request for getting a list of projects available to the
	 * logged in user. Produces JSON.
	 * 
	 * @param principal
	 *            {@link Principal} The currently authenticated users
	 * @param start
	 *            The start position in the list to page.
	 * @param length
	 *            The size of the page to display.
	 * @param draw
	 *            Id for the table to draw, this must be returned.
	 * @param sortColumn
	 *            The id for the column to sort by.
	 * @param direction
	 *            The direction of the sort.
	 * @param searchValue
	 *            Any search terms.
	 * @return JSON value of the page data.
	 */
	@RequestMapping(value = "/ajax/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Map<String, Object> getAjaxProjectList(final Principal principal,
			@RequestParam(DataTable.REQUEST_PARAM_START) Integer start,
			@RequestParam(DataTable.REQUEST_PARAM_LENGTH) Integer length,
			@RequestParam(DataTable.REQUEST_PARAM_DRAW) Integer draw,
			@RequestParam(value = DataTable.REQUEST_PARAM_SORT_COLUMN, defaultValue = "6") Integer sortColumn,
			@RequestParam(value = DataTable.REQUEST_PARAM_SORT_DIRECTION, defaultValue = "desc") String direction,
			@RequestParam(DataTable.REQUEST_PARAM_SEARCH_VALUE) String searchValue) {
		User user = userService.getUserByUsername(principal.getName());

		String sortString;

		if (COLUMN_SORT_MAP.containsKey(sortColumn)) {
			sortString = COLUMN_SORT_MAP.get(sortColumn);
		} else {
			sortString = SORT_BY_MODIFIED_DATE;
		}

		Sort.Direction sortDirection = direction.equals(SORT_ASCENDING) ? Sort.Direction.ASC : Sort.Direction.DESC;

		int pageNum = start / length;
		Page<ProjectUserJoin> page = projectService.searchProjectsByNameForUser(user, searchValue, pageNum, length,
				sortDirection, sortString);
		List<ProjectUserJoin> projectList = page.getContent();

		Map<String, Object> map = new HashMap<>();
		map.put(DataTable.RESPONSE_PARAM_DRAW, draw);
		map.put(DataTable.RESPONSE_PARAM_RECORDS_TOTAL, page.getTotalElements());
		map.put(DataTable.RESPONSE_PARAM_RECORDS_FILTERED, page.getTotalElements());

		// Create the format required by DataTable
		List<List<String>> projectsData = new ArrayList<>();
		for (ProjectUserJoin projectUserJoin : projectList) {
			Project p = projectUserJoin.getSubject();
			ProjectRole role = projectUserJoin.getProjectRole();
			List<String> l = new ArrayList<>();
			l.add(p.getId().toString());
			l.add(p.getName());
			l.add(p.getOrganism());
			l.add(role.toString());
			l.add(String.valueOf(sampleService.getSamplesForProject(p).size()));
			l.add(String.valueOf(userService.getUsersForProject(p).size()));
			l.add(Formats.DATE.format(p.getTimestamp()));
			l.add(p.getModifiedDate().toString());
			projectsData.add(l);
		}
		map.put(DataTable.RESPONSE_PARAM_DATA, projectsData);
		return map;
	}

	/**
	 * Find all projects that have been associated with a project.
	 * 
	 * @param currentProject
	 *            The project to find the associated projects of.
	 * @param currentUser
	 *            The currently logged in user.
	 * @return List of Maps containing information about the associated
	 *         projects.
	 */
	private List<Map<String, String>> getAssociatedProjects(Project currentProject, User currentUser) {
		List<RelatedProjectJoin> relatedProjectJoins = projectService.getRelatedProjects(currentProject);

		// Need to know if the user has rights to view the project
		List<Join<Project, User>> userProjectJoin = projectService.getProjectsForUser(currentUser);

		List<Map<String, String>> projects = new ArrayList<>();
		// Create a quick lookup list
		Map<Long, Boolean> usersProjects = new HashMap<>(userProjectJoin.size());
		for (Join<Project, User> join : userProjectJoin) {
			usersProjects.put(join.getSubject().getId(), true);
		}

		for (RelatedProjectJoin rpj : relatedProjectJoins) {
			Project project = rpj.getObject();

			Map<String, String> map = new HashMap<>();
			map.put("name", project.getLabel());
			map.put("id", project.getId().toString());
			map.put("auth", usersProjects.containsKey(project.getId()) ? "authorized" : "");

			// TODO: (Josh - 2014-07-07) Will need to add remote location
			// information here.
			projects.add(map);
		}
		return projects;
	}

	/**
	 * Changes a {@link ConstraintViolationException} to a usable map of strings
	 * for displaing in the UI.
	 * 
	 * @param e
	 *            {@link ConstraintViolationException} for the form submitted.
	 * @return Map of string {fieldName, error}
	 */
	private Map<String, String> getErrorsFromViolationException(ConstraintViolationException e) {
		Map<String, String> errors = new HashMap<>();
		for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
			String message = violation.getMessage();
			String field = violation.getPropertyPath().toString();
			errors.put(field, message);
		}
		return errors;
	}
}
