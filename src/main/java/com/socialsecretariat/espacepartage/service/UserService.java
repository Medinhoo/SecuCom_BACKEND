package com.socialsecretariat.espacepartage.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialsecretariat.espacepartage.exception.InvalidPasswordException;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.model.User.AccountStatus;
import com.socialsecretariat.espacepartage.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user) {
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    /**
     * Updates only the provided fields of an existing user.
     * 
     * @param userId  The UUID of the user to update
     * @param updates Map containing only the fields to update
     * @return The updated user entity
     * @throws RuntimeException         If the user cannot be found
     * @throws IllegalArgumentException If validation fails for any field
     */
    @Transactional
    public User updateUser(UUID userId, Map<String, Object> updates, Authentication authentication) {
        // Find the existing user
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update only the provided fields
        if (updates.containsKey("firstName")) {
            String firstName = (String) updates.get("firstName");
            existingUser.setFirstName(firstName);
        }

        if (updates.containsKey("lastName")) {
            String lastName = (String) updates.get("lastName");
            existingUser.setLastName(lastName);
        }

        if (updates.containsKey("email")) {
            String email = (String) updates.get("email");
            // Check if email is being changed and if it's already in use
            if (!email.equals(existingUser.getEmail()) &&
                    userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email is already in use");
            }
            existingUser.setEmail(email);
        }

        if (updates.containsKey("phoneNumber")) {
            String phoneNumber = (String) updates.get("phoneNumber");
            existingUser.setPhoneNumber(phoneNumber);
        }

        // if (updates.containsKey("username")) {
        // String username = (String) updates.get("username");
        // // Check if username is being changed and if it's already in use
        // if (!username.equals(existingUser.getUsername()) &&
        // userRepository.existsByUsername(username)) {
        // throw new RuntimeException("Username is already in use");
        // }
        // existingUser.setUsername(username);
        // }

        // Handle account status update (admin only)
        if (updates.containsKey("accountStatus")) {
            // Check if current user has admin role
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new AccessDeniedException("Only administrators can update account status");
            }

            // Handle the enum conversion safely
            String statusValue = (String) updates.get("accountStatus");
            try {
                AccountStatus newStatus = AccountStatus.valueOf(statusValue);
                existingUser.setAccountStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid account status value: " + statusValue);
            }
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public User updateUserPassword(UUID id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Transactional
    public User adminUpdateUserPassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update to new password (no verification needed for admin)
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRolesContaining(role);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByAccountStatus(User.AccountStatus status) {
        return userRepository.findByAccountStatus(status);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsersByName(String searchTerm) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm);
    }
}