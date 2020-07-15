package com.obourgain.mylib.service;

import com.obourgain.mylib.db.UserRepository;
import com.obourgain.mylib.ext.amazon.ItemLookupAmazon;
import com.obourgain.mylib.vobj.Book;
import com.obourgain.mylib.vobj.Tag;
import com.obourgain.mylib.vobj.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


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
    public Optional<User> getByEmail(String email) {
        return userRepository.getByEmail(email);
    }

}
