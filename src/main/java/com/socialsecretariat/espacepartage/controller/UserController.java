package com.socialsecretariat.espacepartage.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.socialsecretariat.espacepartage.dto.UserDto;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.model.User.Role;
import com.socialsecretariat.espacepartage.service.UserService;

/**
 * REST Controller for User management operations.
 * Provides endpoints for creating, reading, updating, and deleting users.
 * Access to most endpoints is restricted based on user roles.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves a user by their ID.
     * Accessible to all authenticated users.
     * 
     * @param id The UUID of the user to retrieve
     * @return The user details as a DTO, without sensitive information
     * @throws RuntimeException if user is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Convert User entity to DTO to avoid exposing sensitive data
        UserDto userDto = convertToDto(user);

        return ResponseEntity.ok(userDto);
    }

    /**
     * Retrieves all users in the system.
     * Only accessible to administrators.
     * 
     * @return A list of all users as DTOs
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(this::convertToDto)
                .toList());
    }

    /**
     * Searches for users by name (first name or last name).
     * Only accessible to administrators and secretariat staff.
     * 
     * @param query The search term to match against first or last names
     * @return A list of matching users as DTOs
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SECRETARIAT')")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsersByName(query);
        return ResponseEntity.ok(users.stream()
                .map(this::convertToDto)
                .toList());
    }

    /**
     * Creates a new user in the system.
     * Only accessible to administrators.
     * Checks for existing username or email to prevent duplicates.
     * 
     * @param user The user entity to create
     * @return The created user as a DTO, with HTTP 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody User user) {
        // Check if username or email already exists
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Username already exists
        }

        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Email already exists
        }

        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToDto(createdUser));
    }

    /**
     * Updates an existing user.
     * Accessible to administrators or the user themselves.
     * Verifies that the provided ID matches the user's ID in the request body.
     * 
     * @param id   The UUID of the user to update
     * @param user The updated user entity
     * @return The updated user as a DTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @RequestBody User user) {
        // Ensure the ID in the path matches the ID in the body
        if (!id.equals(user.getId())) {
            return ResponseEntity.badRequest().body(null);
        }

        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    /**
     * Deletes a user from the system.
     * Only accessible to administrators.
     * 
     * @param id The UUID of the user to delete
     * @return HTTP 204 No Content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves users filtered by role.
     * Only accessible to administrators and secretariat staff.
     * 
     * @param role The role to filter by (ROLE_ADMIN, ROLE_SECRETARIAT,
     *             ROLE_COMPANY)
     * @return A list of users with the specified role as DTOs
     */
    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SECRETARIAT')")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users.stream()
                .map(this::convertToDto)
                .toList());
    }

    /**
     * Utility method to convert User entity to UserDto.
     * Prevents sensitive information like passwords from being exposed through the
     * API.
     * 
     * @param user The User entity to convert
     * @return A UserDto containing only the safe fields to expose
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAccountStatus(user.getAccountStatus().name());

        // Convert enum roles to strings
        List<String> roleNames = new ArrayList<>();
        for (Role role : user.getRoles()) {
            roleNames.add(role.name());
        }
        dto.setRoles(roleNames);

        return dto;
    }
}