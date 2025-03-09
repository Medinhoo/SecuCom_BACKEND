package com.socialsecretariat.espacepartage.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.socialsecretariat.espacepartage.dto.PasswordChangeRequest;
import com.socialsecretariat.espacepartage.dto.UserDto;
import com.socialsecretariat.espacepartage.exception.ErrorResponse;
import com.socialsecretariat.espacepartage.exception.InvalidPasswordException;
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
    /**
     * Checks if the requested user ID belongs to the currently authenticated user.
     * This works by:
     * 1. Using @userService to access the UserService Spring bean
     * 2. Finding the User entity with the requested ID
     * 3. Comparing that user's username with the authenticated user's username
     * 4. The isPresent() check prevents NullPointerException if user ID doesn't
     * exist
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.getUserById(#id).isPresent() && @userService.getUserById(#id).get().getUsername() == authentication.principal.username")
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

    // /**
    // * Searches for users by name (first name or last name).
    // * Only accessible to administrators (for the moment)
    // *
    // * @param query The search term to match against first or last names
    // * @return A list of matching users as DTOs
    // */
    // @GetMapping("/search")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query)
    // {
    // List<User> users = userService.searchUsersByName(query);
    // return ResponseEntity.ok(users.stream()
    // .map(this::convertToDto)
    // .toList());
    // }

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
     * 
     * @param id   The UUID of the user to update
     * @param user The updated user entity. Note that the password field can be:
     *             - Null/empty: The existing password will be preserved
     *             - Same as current: No change will be made
     *             - New value: Will be encoded before saving
     * @return The updated user as a DTO
     * 
     *         Security:
     *         - Admin users can update any user
     *         - Regular users can only update their own profile
     *         - Password handling is managed in the service layer with proper
     *         encoding
     *         - Sensitive data is never exposed through the API response (uses DTO)
     * 
     *         Validation:
     *         - Ensures the ID in the path matches the ID in the request body
     *         - Full entity validation occurs in the service layer
     *         - Proper error responses are returned for invalid requests
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.getUserById(#id).isPresent() && @userService.getUserById(#id).get().getUsername() == authentication.principal.username")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @RequestBody User user) {
        // Ensure the ID in the path matches the ID in the body
        if (!id.equals(user.getId())) {
            return ResponseEntity.badRequest().body(null);
        }

        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    /**
     * Updates the password for a user.
     * - Regular users must provide their current password for verification
     * - Admins can change passwords without knowing the current password
     * 
     * @param id              The UUID of the user whose password will be updated
     * @param passwordRequest Contains the current and new password
     * @return The updated user as a DTO, or an error if verification fails
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.getUserById(#id).isPresent() && @userService.getUserById(#id).get().getUsername() == authentication.principal.username")
    public ResponseEntity<?> updateUserPassword(@PathVariable UUID id,
            @RequestBody PasswordChangeRequest passwordRequest) {
        try {
            // Check if current user is an admin
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            User updatedUser;
            if (isAdmin) {
                // Admin can update without the current password
                updatedUser = userService.adminUpdateUserPassword(id, passwordRequest.getNewPassword());
            } else {
                // Regular users must verify their current password
                updatedUser = userService.updateUserPassword(id,
                        passwordRequest.getCurrentPassword(),
                        passwordRequest.getNewPassword());
            }

            return ResponseEntity.ok(convertToDto(updatedUser));
        } catch (InvalidPasswordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Current password is incorrect",
                    null));
        }
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

    // /**
    // * Retrieves users filtered by role.
    // * Only accessible to administrators and secretariat staff.
    // *
    // * @param role The role to filter by (ROLE_ADMIN, ROLE_SECRETARIAT,
    // * ROLE_COMPANY)
    // * @return A list of users with the specified role as DTOs
    // */
    // @GetMapping("/by-role/{role}")
    // @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SECRETARIAT')")
    // public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable Role role)
    // {
    // List<User> users = userService.getUsersByRole(role);
    // return ResponseEntity.ok(users.stream()
    // .map(this::convertToDto)
    // .toList());
    // }

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

        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());

        return dto;
    }
}