package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping("/public/hello")
    public String publicHello() {
        logger.info("Public hello accessed");
        return "Hello, world!";
    }

    // testing helper: create a user via GET
    @GetMapping("/public/makeuser")
    public User makeUser(@RequestParam String username, @RequestParam String password) {
        // if exists return existing
        return userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(password));
            u.setRole("ROLE_USER");
            return userRepository.save(u);
        });
    }

    @PostMapping("/public/register")
    public User register(@RequestBody User user) {
        // simple registration with idempotence: if the username already exists,
        // only accept if the provided password matches the stored one.
        return userRepository.findByUsername(user.getUsername()).map(existing -> {
            if (passwordEncoder.matches(user.getPassword(), existing.getPassword())) {
                // correct password, behave like a login
                return existing;
            }
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Username already taken");
        }).orElseGet(() -> {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("ROLE_USER");
            return userRepository.save(user);
        });
    }

    @GetMapping("/private/hello")
    public String privateHello() {
        logger.info("Private hello accessed");
        return "Hello, authenticated user!";
    }
}

