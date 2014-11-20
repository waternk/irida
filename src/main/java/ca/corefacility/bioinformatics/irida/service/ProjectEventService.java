package ca.corefacility.bioinformatics.irida.service;

import org.springframework.data.domain.Page;

import ca.corefacility.bioinformatics.irida.model.event.ProjectEvent;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.user.User;

/**
 * Service for reading and managing {@link ProjectEvent}s
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
public interface ProjectEventService extends CRUDService<Long, ProjectEvent> {

	/**
	 * Get the events for a given project
	 * 
	 * @param project
	 *            The project to get events for
	 * @param max
	 *            The maximum number of events to show
	 * @return A List of {@link ProjectEvent}s
	 */
	public Page<ProjectEvent> getEventsForProject(Project project, int max);

	/**
	 * Get the events on all projects for a given user
	 * 
	 * @param user
	 *            The {@link User} to get events for
	 * @param max
	 *            The maximum number of events to show
	 * @return A List of {@link ProjectEvent}s
	 */
	public Page<ProjectEvent> getEventsForUser(User user, int max);
}
