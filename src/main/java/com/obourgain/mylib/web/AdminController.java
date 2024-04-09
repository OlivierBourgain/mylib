package com.obourgain.mylib.web;

import com.obourgain.mylib.service.UserService;
import com.obourgain.mylib.util.auth.WebUser;
import com.obourgain.mylib.vobj.User;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
public class AdminController extends AbstractController {
    private static Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    /**
     * Retrieve the clientId from the Google Auth token.
     */
    private boolean isAdmin(WebUser webUser)  {
        User user = userService.getByEmail(webUser.getEmail());
        return user.getRole() == User.UserRole.ADMIN;
    }

    /**
     * Get the user list.
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String adminUsers(HttpServletRequest request, Model model) {
        log.info("Controller adminUser");
        WebUser user = getUserDetail();
        if (!isAdmin(user)) return "books";

        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("user", user);
        return "userList";
    }

    /**
     * Create user.
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST, params = "action=create")
    public String createUser(String email) {
        log.info("Controller createUser");

        WebUser user = getUserDetail();
        if (!isAdmin(user)) return "redirect:/books";

        userService.create(email);
        return "redirect:/users";
    }

    @RequestMapping(value = "/deleteUser", method = RequestMethod.POST)
    public String deleteUser(Long id) {
        log.info("Delete user");
        WebUser webUser = getUserDetail();
        if (!isAdmin(webUser)) return "redirect:/books";

        User user = userService.get(id);
        if (user != null) userService.delete(id);
        return "empty";
    }

    @RequestMapping(value = "/changeRole", method = RequestMethod.POST)
    public String changeRole(Long id, String role) {
        log.info("Change user role {}", role);
        WebUser webUser = getUserDetail();
        if (!isAdmin(webUser)) return "redirect:/books";

        User user = userService.get(id);
        if (user != null) {
            switch(role) {
                case "ADMIN": userService.updateRole(id, User.UserRole.ADMIN); break;
                case "USER": userService.updateRole(id, User.UserRole.USER); break;
            }
        }
        return "empty";
    }
}
