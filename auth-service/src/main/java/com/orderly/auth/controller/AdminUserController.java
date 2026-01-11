package com.orderly.auth.controller;

import com.orderly.auth.dto.RegisterRequest;
import com.orderly.auth.entity.User;
import com.orderly.auth.repository.UserRepository;
import com.orderly.auth.service.AuthService;
import com.orderly.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public AdminUserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Remove passwords from response
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/create-admin")
    public ResponseEntity<ApiResponse<User>> createAdmin(@Valid @RequestBody RegisterRequest request) {
        try {
            User admin = authService.createAdmin(request);
            admin.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Admin created successfully", admin));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{userId}/disable")
    public ResponseEntity<ApiResponse<User>> disableUser(@PathVariable String userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setEnabled(false);
                    user = userRepository.save(user);
                    user.setPassword(null);
                    return ResponseEntity.ok(ApiResponse.success("User disabled", user));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    @PutMapping("/{userId}/enable")
    public ResponseEntity<ApiResponse<User>> enableUser(@PathVariable String userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setEnabled(true);
                    user = userRepository.save(user);
                    user.setPassword(null);
                    return ResponseEntity.ok(ApiResponse.success("User enabled", user));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }
}
