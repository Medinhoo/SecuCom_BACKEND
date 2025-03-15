package com.socialsecretariat.espacepartage.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialsecretariat.espacepartage.exception.InvalidPasswordException;
import com.socialsecretariat.espacepartage.model.SecretariatEmployee;
import com.socialsecretariat.espacepartage.model.SocialSecretariat;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.model.User.AccountStatus;
import com.socialsecretariat.espacepartage.model.User.Role;
import com.socialsecretariat.espacepartage.repository.SecretariatEmployeeRepository;
import com.socialsecretariat.espacepartage.repository.SocialSecretariatRepository;
import com.socialsecretariat.espacepartage.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.socialsecretariat.espacepartage.dto.UserDto;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SecretariatEmployeeRepository secretariatEmployeeRepository;
    private final SocialSecretariatRepository socialSecretariatRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            SecretariatEmployeeRepository secretariatEmployeeRepository,
            SocialSecretariatRepository socialSecretariatRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.secretariatEmployeeRepository = secretariatEmployeeRepository;
        this.socialSecretariatRepository = socialSecretariatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user) {
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public SecretariatEmployee createSecretariatEmployee(SecretariatEmployee employee, UUID secretariatId) {
        // Check if secretariat exists
        SocialSecretariat secretariat = socialSecretariatRepository.findById(secretariatId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Social secretariat not found with ID: " + secretariatId));

        // Encode password
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        // Set secretariat
        employee.setSecretariat(secretariat);

        // Save employee
        SecretariatEmployee savedEmployee = secretariatEmployeeRepository.save(employee);

        // Update secretariat's employees collection
        secretariat.addEmployee(savedEmployee);
        socialSecretariatRepository.save(secretariat);

        return savedEmployee;
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
    public Optional<SecretariatEmployee> getSecretariatEmployeeById(UUID id) {
        return secretariatEmployeeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<SecretariatEmployee> getSecretariatEmployeesBySecretariatId(UUID secretariatId) {
        return secretariatEmployeeRepository.findBySecretariatId(secretariatId);
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
        // First check if it's a secretariat employee
        Optional<SecretariatEmployee> employee = secretariatEmployeeRepository.findById(id);

        if (employee.isPresent()) {
            // Remove from secretariat
            SocialSecretariat secretariat = employee.get().getSecretariat();
            if (secretariat != null) {
                secretariat.removeEmployee(employee.get());
                socialSecretariatRepository.save(secretariat);
            }

            // Then delete the employee
            secretariatEmployeeRepository.deleteById(id);
        } else {
            // Otherwise, just delete the user
            userRepository.deleteById(id);
        }
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
        // First check if it's a secretariat employee
        Optional<SecretariatEmployee> employeeOpt = secretariatEmployeeRepository.findById(userId);

        if (employeeOpt.isPresent()) {
            // Handle employee update
            return updateSecretariatEmployee(employeeOpt.get(), updates, authentication);
        } else {
            // Handle regular user update
            // Find the existing user
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return updateUserFields(existingUser, updates, authentication);
        }
    }

    /**
     * Updates the fields of a SecretariatEmployee
     */
    private User updateSecretariatEmployee(SecretariatEmployee employee, Map<String, Object> updates,
            Authentication authentication) {
        // First update the base user fields
        updateUserFields(employee, updates, authentication);

        // Then update employee-specific fields
        if (updates.containsKey("position")) {
            String position = (String) updates.get("position");
            employee.setPosition(position);
        }

        if (updates.containsKey("specialization")) {
            String specialization = (String) updates.get("specialization");
            employee.setSpecialization(specialization);
        }

        // Handle secretariat change if provided
        if (updates.containsKey("secretariatId")) {
            UUID secretariatId = UUID.fromString((String) updates.get("secretariatId"));

            // Only allow admins to change secretariat
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new AccessDeniedException("Only administrators can change secretariat assignment");
            }

            // Check if new secretariat exists
            if (!employee.getSecretariat().getId().equals(secretariatId)) {
                SocialSecretariat newSecretariat = socialSecretariatRepository.findById(secretariatId)
                        .orElseThrow(() -> new EntityNotFoundException("Social secretariat not found"));

                // Remove from old secretariat
                SocialSecretariat oldSecretariat = employee.getSecretariat();
                oldSecretariat.removeEmployee(employee);
                socialSecretariatRepository.save(oldSecretariat);

                // Add to new secretariat
                employee.setSecretariat(newSecretariat);
                newSecretariat.addEmployee(employee);
                socialSecretariatRepository.save(newSecretariat);
            }
        }

        return secretariatEmployeeRepository.save(employee);
    }

    /**
     * Updates the base fields of any User
     */
    private User updateUserFields(User user, Map<String, Object> updates, Authentication authentication) {
        // Update base user fields
        if (updates.containsKey("firstName")) {
            String firstName = (String) updates.get("firstName");
            user.setFirstName(firstName);
        }

        if (updates.containsKey("lastName")) {
            String lastName = (String) updates.get("lastName");
            user.setLastName(lastName);
        }

        if (updates.containsKey("email")) {
            String email = (String) updates.get("email");
            // Check if email is being changed and if it's already in use
            if (!email.equals(user.getEmail()) &&
                    userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email is already in use");
            }
            user.setEmail(email);
        }

        if (updates.containsKey("phoneNumber")) {
            String phoneNumber = (String) updates.get("phoneNumber");
            user.setPhoneNumber(phoneNumber);
        }

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
                user.setAccountStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid account status value: " + statusValue);
            }
        }

        // Handle roles update (admin only)
        if (updates.containsKey("roles")) {
            // Check if current user has admin role
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new AccessDeniedException("Only administrators can update user roles");
            }

            // This would need custom handling based on your role structure
            // Not implemented here for simplicity
        }

        return userRepository.save(user);
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
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRolesContaining(role);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByAccountStatus(AccountStatus status) {
        return userRepository.findByAccountStatus(status);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsersByName(String searchTerm) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm);
    }

    /**
     * Converts a User entity to UserDto.
     * Prevents sensitive information like passwords from being exposed.
     * 
     * @param user The User entity to convert
     * @return A UserDto containing only the safe fields to expose
     */
    public UserDto convertToDto(User user) {
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

    /**
     * Converts a list of User entities to DTOs
     * 
     * @param users List of User entities
     * @return List of UserDtos
     */
    public List<UserDto> convertToDtoList(List<User> users) {
        return users.stream()
                .map(this::convertToDto)
                .toList();
    }
}
