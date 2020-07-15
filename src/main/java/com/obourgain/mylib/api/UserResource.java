package com.obourgain.mylib.api;

import com.obourgain.mylib.service.UserService;
import com.obourgain.mylib.vobj.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@CrossOrigin(origins = {"http://localhost:3000", "https://book.obourgain.com"})
@RestController
@RequestMapping("/api")
public class UserResource extends AbstractResource {
    private static Logger log = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    private UserService userService;

    /**
     * @return the list of users.
     */
    @GetMapping(value = "/users")
    public ResponseEntity<List<User>> getUsers(HttpServletRequest request) throws Exception {
        log.info("REST - users");
        checkAccess(request);
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    /**
     * Create a user.
     */
    @PostMapping(value = "/user")
    public ResponseEntity<User> createUser(HttpServletRequest request, @RequestParam("email") String email) throws Exception {
        log.info("REST - create user {}", email);
        checkAccess(request);
        return new ResponseEntity<>(userService.create(email), HttpStatus.OK);
    }

    /**
     * Delete a user.
     */
    @DeleteMapping(value = "/user/{id}")
    public ResponseEntity<List<User>> deleteUser(HttpServletRequest request, @PathVariable("id") Long id) throws Exception {
        log.info("REST - delete user {}", id);
        checkAccess(request, User.UserRole.ADMIN);
        userService.delete(id);
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    /**
     * Update the role of a user.
     */
    @PostMapping(value = "/user/{id}")
    public ResponseEntity<User> updateRole(HttpServletRequest request, @PathVariable("id") Long id, @RequestParam("role") User.UserRole role) throws Exception {
        log.info("REST - update role user {} {}", id, role);
        checkAccess(request, User.UserRole.ADMIN);
        return new ResponseEntity<>(userService.updateRole(id, role), HttpStatus.OK);
    }

    /**
     * Get user info
     */
    @GetMapping(value = "/user")
    public ResponseEntity<User> findUser(HttpServletRequest request, @RequestParam("email") String email) throws Exception {
        log.info("REST - getUser {}", email);
        checkAccess(request);
        User res = userService.getByEmail(email).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unable to find resource"));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}

