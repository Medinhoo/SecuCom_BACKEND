package com.socialsecretariat.espacepartage.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.socialsecretariat.espacepartage.dto.PasswordChangeRequest;
import com.socialsecretariat.espacepartage.dto.SecretariatEmployeeDto;
import com.socialsecretariat.espacepartage.dto.SecretariatEmployeeUpdateDto;
import com.socialsecretariat.espacepartage.dto.UserDto;
import com.socialsecretariat.espacepartage.exception.ErrorResponse;
import com.socialsecretariat.espacepartage.exception.InvalidPasswordException;
import com.socialsecretariat.espacepartage.model.SecretariatEmployee;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.service.SecretariatEmployeeService;
import com.socialsecretariat.espacepartage.service.SocialSecretariatService;
import com.socialsecretariat.espacepartage.service.UserService;
import com.socialsecretariat.espacepartage.service.CompanyContactService;
import com.socialsecretariat.espacepartage.dto.CompanyContactDto;
import com.socialsecretariat.espacepartage.dto.CompanyContactUpdateDto;
import com.socialsecretariat.espacepartage.model.CompanyContact;

/**
 * REST Controller for User management operations.
 * Provides endpoints for creating, reading, updating, and deleting users.
 * Access to most endpoints is restricted based on user roles.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final SecretariatEmployeeService secretariatEmployeeService;
    private final CompanyContactService companyContactService;

    public UserController(UserService userService,
            SocialSecretariatService socialSecretariatService,
            SecretariatEmployeeService secretariatEmployeeService,
            CompanyContactService companyContactService) {
        this.userService = userService;
        this.secretariatEmployeeService = secretariatEmployeeService;
        this.companyContactService = companyContactService;
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.getUserById(#id).isPresent() && @userService.getUserById(#id).get().getUsername() == authentication.principal.username")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        // First check if it's a secretariat employee
        Optional<SecretariatEmployee> employeeOpt = userService.getSecretariatEmployeeById(id);

        if (employeeOpt.isPresent()) {
            // Convert SecretariatEmployee to UserDto using service
            UserDto userDto = secretariatEmployeeService.convertToUserDto(employeeOpt.get());
            return ResponseEntity.ok(userDto);
        } else {
            // Check if it's a company contact
            Optional<CompanyContact> contactOpt = userService.getCompanyContactById(id);
            if (contactOpt.isPresent()) {
                // Convert CompanyContact to UserDto using service
                UserDto userDto = companyContactService.convertToUserDto(contactOpt.get());
                return ResponseEntity.ok(userDto);
            } else {
                // Handle regular user
                User user = userService.getUserById(id)
                        .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

                // Convert User entity to DTO using service
                UserDto userDto = userService.convertToDto(user);
                return ResponseEntity.ok(userDto);
            }
        }
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
        List<UserDto> allUserDtos = new ArrayList<>();

        // Get regular users and convert to DTOs
        userService.getAllUsers().forEach(user -> {
            if (!(user instanceof SecretariatEmployee) && !(user instanceof CompanyContact)) {
                allUserDtos.add(userService.convertToDto(user));
            }
        });

        // Get secretariat employees and convert to DTOs
        userService.getAllUsers().stream()
                .filter(user -> user instanceof SecretariatEmployee)
                .forEach(employee -> allUserDtos.add(
                        secretariatEmployeeService.convertToUserDto((SecretariatEmployee) employee)));

        // Get company contacts and convert to DTOs
        userService.getAllUsers().stream()
                .filter(user -> user instanceof CompanyContact)
                .forEach(contact -> allUserDtos.add(
                        companyContactService.convertToUserDto((CompanyContact) contact)));

        return ResponseEntity.ok(allUserDtos);
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
                .body(userService.convertToDto(createdUser));
    }

    /**
     * Creates a new secretariat employee in the system.
     * Only accessible to administrators.
     * 
     * @param employee      The employee to create
     * @param secretariatId The ID of the secretariat
     * @return The created employee as a DTO
     */
    @PostMapping("/secretariat-employees")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> createSecretariatEmployee(
            @RequestBody SecretariatEmployee employee,
            @RequestParam UUID secretariatId) {

        // Check if username or email already exists
        if (userService.existsByUsername(employee.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Username already exists
        }

        if (userService.existsByEmail(employee.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Email already exists
        }

        // Create employee with secretariat association
        SecretariatEmployee createdEmployee = userService.createSecretariatEmployee(employee, secretariatId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(secretariatEmployeeService.convertToUserDto(createdEmployee));
    }

    /**
     * Updates an existing user with partial information.
     * Accessible to administrators or the user themselves.
     * 
     * @param id      The UUID of the user to update
     * @param updates A map containing only the fields to be updated
     * @return The updated user as a DTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.getUserById(#id).isPresent() && @userService.getUserById(#id).get().getUsername() == authentication.principal.username")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        // Verify the update is not trying to change the ID
        if (updates.containsKey("id") && !id.equals(updates.get("id"))) {
            throw new IllegalArgumentException("User ID in path must match ID in request body");
        }

        User updatedUser = userService.updateUser(id, updates, authentication);

        // Check if it's a secretariat employee
        if (updatedUser instanceof SecretariatEmployee) {
            return ResponseEntity.ok(secretariatEmployeeService.convertToUserDto((SecretariatEmployee) updatedUser));
        } else if (updatedUser instanceof CompanyContact) {
            return ResponseEntity.ok(companyContactService.convertToUserDto((CompanyContact) updatedUser));
        } else {
            return ResponseEntity.ok(userService.convertToDto(updatedUser));
        }
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

            // Check if it's a secretariat employee
            if (updatedUser instanceof SecretariatEmployee) {
                return ResponseEntity
                        .ok(secretariatEmployeeService.convertToUserDto((SecretariatEmployee) updatedUser));
            } else if (updatedUser instanceof CompanyContact) {
                return ResponseEntity
                        .ok(companyContactService.convertToUserDto((CompanyContact) updatedUser));
            } else {
                return ResponseEntity.ok(userService.convertToDto(updatedUser));
            }
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

    @GetMapping("/secretariat-employees")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SECRETARIAT')")
    public ResponseEntity<List<UserDto>> getAllSecretariatEmployees() {
        List<SecretariatEmployeeDto> employeeDtos = secretariatEmployeeService.getAllSecretariatEmployees();
        return ResponseEntity.ok(secretariatEmployeeService.convertDtoToUserDtoList(employeeDtos));
    }

    @GetMapping("/secretariat-employees/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SECRETARIAT')")
    public ResponseEntity<UserDto> getSecretariatEmployeeById(@PathVariable UUID id) {
        SecretariatEmployeeDto employeeDto = secretariatEmployeeService.getSecretariatEmployeeById(id);
        return ResponseEntity.ok(secretariatEmployeeService.convertDtoToUserDto(employeeDto));
    }

    @GetMapping("/secretariat-employees/by-secretariat/{secretariatId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SECRETARIAT')")
    public ResponseEntity<List<UserDto>> getSecretariatEmployeesBySecretariatId(@PathVariable UUID secretariatId) {
        List<SecretariatEmployeeDto> employeeDtos = secretariatEmployeeService
                .getSecretariatEmployeesBySecretariatId(secretariatId);
        return ResponseEntity.ok(secretariatEmployeeService.convertDtoToUserDtoList(employeeDtos));
    }

    @PutMapping("/secretariat-employees/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SECRETARIAT')")
    public ResponseEntity<UserDto> updateSecretariatEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody SecretariatEmployeeUpdateDto employeeDto) {
        SecretariatEmployeeDto updatedEmployeeDto = secretariatEmployeeService.updateSecretariatEmployee(id,
                employeeDto);
        return ResponseEntity.ok(secretariatEmployeeService.convertDtoToUserDto(updatedEmployeeDto));
    }

    @DeleteMapping("/secretariat-employees/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteSecretariatEmployee(@PathVariable UUID id) {
        secretariatEmployeeService.deleteSecretariatEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/company-contacts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> createCompanyContact(
            @RequestBody CompanyContact contact,
            @RequestParam UUID companyId) {

        // Check if username or email already exists
        if (userService.existsByUsername(contact.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Username already exists
        }

        if (userService.existsByEmail(contact.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Email already exists
        }

        // Create contact with company association
        CompanyContact createdContact = userService.createCompanyContact(contact, companyId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyContactService.convertToUserDto(createdContact));
    }

    @GetMapping("/company-contacts")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COMPANY')")
    public ResponseEntity<List<UserDto>> getAllCompanyContacts() {
        List<CompanyContactDto> contactDtos = companyContactService.getAllCompanyContacts();
        return ResponseEntity.ok(companyContactService.convertDtoToUserDtoList(contactDtos));
    }

    @GetMapping("/company-contacts/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COMPANY')")
    public ResponseEntity<UserDto> getCompanyContactById(@PathVariable UUID id) {
        CompanyContactDto contactDto = companyContactService.getCompanyContactById(id);
        return ResponseEntity.ok(companyContactService.convertDtoToUserDto(contactDto));
    }

    @GetMapping("/company-contacts/by-company/{companyId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COMPANY')")
    public ResponseEntity<List<UserDto>> getCompanyContactsByCompanyId(@PathVariable UUID companyId) {
        List<CompanyContactDto> contactDtos = companyContactService.getCompanyContactsByCompanyId(companyId);
        return ResponseEntity.ok(companyContactService.convertDtoToUserDtoList(contactDtos));
    }

    @PutMapping("/company-contacts/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_COMPANY')")
    public ResponseEntity<UserDto> updateCompanyContact(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyContactUpdateDto contactDto) {
        CompanyContactDto updatedContactDto = companyContactService.updateCompanyContact(id, contactDto);
        return ResponseEntity.ok(companyContactService.convertDtoToUserDto(updatedContactDto));
    }

    @DeleteMapping("/company-contacts/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCompanyContact(@PathVariable UUID id) {
        companyContactService.deleteCompanyContact(id);
        return ResponseEntity.noContent().build();
    }
}
