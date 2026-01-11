package com.orderly.auth.config;

import com.orderly.auth.entity.User;
import com.orderly.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes default admin user on startup if none exists.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create default admin if no admin exists
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(User::isAdmin);

        if (!adminExists) {
            User admin = new User();
            admin.setEmail("admin@orderly.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.addRole(User.Role.ADMIN);

            userRepository.save(admin);
            log.info("Default admin user created: admin@orderly.com / admin123");
        }
    }
}
