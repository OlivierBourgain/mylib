package com.obourgain.mylib.service;

import com.obourgain.mylib.db.UserRepository;
import com.obourgain.mylib.vobj.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


/**
 * Business logic for user management.
 */
@Service
public class UserService {
    private static Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Delete a user.
     */
    public void delete(Long id) {
        log.info("Deleting user {}", id);
        userRepository.deleteById(id);
    }

    /**
     * Create a user.
     */
    public User create(String email) {
        log.info("Creating user " + email);
        User user = new User();
        user.setEmail(email);
        user.setRole(User.UserRole.USER);
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Create a user.
     */
    public List<User> findAll() {
        log.info("Findall users");
        return userRepository.findAll();
    }

    /**
     * Update the role of an user
     */
    public User updateRole(Long id, User.UserRole role) {
        log.info("Deleting user {}", id);
        User user = userRepository.getOne(id);
        user.setRole(role);
        user.setUpdated(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Renvoie un user par son email
     */
    public User getByEmail(String email) {
        return userRepository.getByEmail(email).orElseThrow();
    }

    /**
     * Renvoie un user par son id
     */
    public User get(Long id) {
        return userRepository.getById(id);
    }

}
