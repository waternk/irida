package ca.corefacility.bioinformatics.irida.web.controller;

import ca.corefacility.bioinformatics.irida.model.User;
import ca.corefacility.bioinformatics.irida.service.CRUDService;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.UserResource;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for managing users.
 *
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
@Controller
@RequestMapping(value = "/users")
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);
    private final CRUDService<String, User> userService;

    @Autowired
    public UsersController(@Qualifier("userService") CRUDService<String, User> userService) {
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showUsersPage(Model model) {
        List<User> users = userService.list();
        List<UserResource> resources = new ArrayList<>(users.size());
        for (User u : users) {
            UserResource resource = new UserResource(u);
            Link link = linkTo(UsersController.class).slash(resource.getUUID()).withSelfRel();
            resource.add(link);
            resources.add(resource);
        }
        model.addAttribute("users", resources);
        return "users/index";
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody String create(HttpServletResponse response,
        @RequestParam(value = "username", required = false) String username,
        @RequestParam(value = "firstName", required = false) String firstName,
        @RequestParam(value = "lastName", required = false) String lastName,
        @RequestParam(value = "email", required = false) String email,
        @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
        @RequestParam(value = "password", required = false) String password) {
        User user = new User(username, email, password, firstName, lastName, phoneNumber);
        try {
            userService.create(user);
        } catch (ConstraintViolationException e) {
            Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return validationMessages(constraintViolations);
        }
        return "success";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getUser(@PathVariable int id, Model model) {
        model.addAttribute("userId", id);
        return "users/user";
    }    

    private String validationMessages(Set<ConstraintViolation<?>> failures) {
        Map<String, List<String>> mp = new HashMap();
        for (ConstraintViolation<?> failure : failures) {
            logger.debug(failure.getPropertyPath().toString() + ": " + failure.getMessage());
            String property = failure.getPropertyPath().toString();
            if(mp.containsKey(property)){
                mp.get(failure.getPropertyPath().toString()).add(failure.getMessage());
            }
            else {
                List<String> list= new ArrayList<String>();
                list.add(failure.getMessage());
                mp.put(property, list);
            }
        }
        Gson g = new Gson();
        return g.toJson(mp);
    }
}
