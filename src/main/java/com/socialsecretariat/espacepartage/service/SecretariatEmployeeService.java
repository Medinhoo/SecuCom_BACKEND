package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.SecretariatEmployeeDto;
import com.socialsecretariat.espacepartage.dto.SecretariatEmployeeUpdateDto;
import com.socialsecretariat.espacepartage.model.SecretariatEmployee;
import com.socialsecretariat.espacepartage.model.SocialSecretariat;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.SecretariatEmployeeRepository;
import com.socialsecretariat.espacepartage.repository.SocialSecretariatRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.socialsecretariat.espacepartage.dto.UserDto;

@Service
public class SecretariatEmployeeService {

    private final SecretariatEmployeeRepository secretariatEmployeeRepository;
    private final SocialSecretariatRepository socialSecretariatRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public SecretariatEmployeeService(
            SecretariatEmployeeRepository secretariatEmployeeRepository,
            SocialSecretariatRepository socialSecretariatRepository,
            UserService userService,
            PasswordEncoder passwordEncoder) {
        this.secretariatEmployeeRepository = secretariatEmployeeRepository;
        this.socialSecretariatRepository = socialSecretariatRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Create a new secretariat employee
    @Transactional
    public SecretariatEmployeeDto createSecretariatEmployee(SecretariatEmployeeDto employeeDto) {
        // Check if username already exists using UserService
        if (userService.existsByUsername(employeeDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists using UserService
        if (userService.existsByEmail(employeeDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check if secretariat exists
        SocialSecretariat secretariat = socialSecretariatRepository.findById(employeeDto.getSecretariatId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Social secretariat not found with ID: " + employeeDto.getSecretariatId()));

        // Create new employee
        SecretariatEmployee employee = new SecretariatEmployee();

        // Set user properties
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        employee.setUsername(employeeDto.getUsername());
        employee.setEmail(employeeDto.getEmail());
        employee.setPhoneNumber(employeeDto.getPhoneNumber());
        employee.setPassword(passwordEncoder.encode(employeeDto.getPassword()));

        // Set secretariat employee specific properties
        employee.setPosition(employeeDto.getPosition());
        employee.setSpecialization(employeeDto.getSpecialization());
        employee.setSecretariat(secretariat);

        // Set default user properties
        employee.setAccountStatus(User.AccountStatus.ACTIVE);
        employee.setCreatedAt(LocalDateTime.now());

        // Set role
        Set<User.Role> roles = new HashSet<>();
        roles.add(User.Role.ROLE_SECRETARIAT);
        employee.setRoles(roles);

        // Save using UserService to leverage any additional logic there
        SecretariatEmployee savedEmployee = secretariatEmployeeRepository.save(employee);

        // Update secretariat's employees collection
        secretariat.addEmployee(savedEmployee);
        socialSecretariatRepository.save(secretariat);

        // Convert to DTO
        return convertToDto(savedEmployee);
    }

    // Get all secretariat employees
    @Transactional(readOnly = true)
    public List<SecretariatEmployeeDto> getAllSecretariatEmployees() {
        return secretariatEmployeeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get secretariat employee by ID
    @Transactional(readOnly = true)
    public SecretariatEmployeeDto getSecretariatEmployeeById(UUID id) {
        SecretariatEmployee employee = secretariatEmployeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Secretariat employee not found with ID: " + id));

        return convertToDto(employee);
    }

    // Get secretariat employees by secretariat ID
    @Transactional(readOnly = true)
    public List<SecretariatEmployeeDto> getSecretariatEmployeesBySecretariatId(UUID secretariatId) {
        // Check if secretariat exists
        if (!socialSecretariatRepository.existsById(secretariatId)) {
            throw new EntityNotFoundException("Social secretariat not found with ID: " + secretariatId);
        }

        return secretariatEmployeeRepository.findBySecretariatId(secretariatId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Updates only the provided fields of a secretariat employee.
     * Fields that are null in the DTO will not be updated.
     */
    @Transactional
    public SecretariatEmployeeDto updateSecretariatEmployee(UUID id, SecretariatEmployeeUpdateDto employeeDto) {
        // Check if employee exists
        SecretariatEmployee employee = secretariatEmployeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Secretariat employee not found with ID: " + id));

        // Create updates map for user properties, only including non-null values
        Map<String, Object> userUpdates = new HashMap<>();
        if (employeeDto.getFirstName() != null) {
            userUpdates.put("firstName", employeeDto.getFirstName());
        }
        if (employeeDto.getLastName() != null) {
            userUpdates.put("lastName", employeeDto.getLastName());
        }
        if (employeeDto.getEmail() != null) {
            userUpdates.put("email", employeeDto.getEmail());
        }
        if (employeeDto.getPhoneNumber() != null) {
            userUpdates.put("phoneNumber", employeeDto.getPhoneNumber());
        }

        // Only update user fields if there are changes
        if (!userUpdates.isEmpty()) {
            userService.updateUser(id, userUpdates, null);
        }

        // Check if secretariat is being changed and if it exists
        if (employeeDto.getSecretariatId() != null &&
                !employee.getSecretariat().getId().equals(employeeDto.getSecretariatId())) {
            SocialSecretariat newSecretariat = socialSecretariatRepository.findById(employeeDto.getSecretariatId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Social secretariat not found with ID: " + employeeDto.getSecretariatId()));

            // Remove from old secretariat
            SocialSecretariat oldSecretariat = employee.getSecretariat();
            oldSecretariat.removeEmployee(employee);
            socialSecretariatRepository.save(oldSecretariat);

            // Add to new secretariat
            employee.setSecretariat(newSecretariat);
            newSecretariat.addEmployee(employee);
            socialSecretariatRepository.save(newSecretariat);
        }

        // Update secretariat employee specific properties only if provided
        if (employeeDto.getPosition() != null) {
            employee.setPosition(employeeDto.getPosition());
        }
        if (employeeDto.getSpecialization() != null) {
            employee.setSpecialization(employeeDto.getSpecialization());
        }

        // Only update password if provided and not empty
        if (employeeDto.getPassword() != null && !employeeDto.getPassword().isEmpty()) {
            userService.adminUpdateUserPassword(id, employeeDto.getPassword());
        }

        // Save updated employee
        SecretariatEmployee updatedEmployee = secretariatEmployeeRepository.save(employee);

        // Convert to DTO
        return convertToDto(updatedEmployee);
    }

    // Delete a secretariat employee
    @Transactional
    public void deleteSecretariatEmployee(UUID id) {
        // Check if employee exists
        SecretariatEmployee employee = secretariatEmployeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Secretariat employee not found with ID: " + id));

        // Remove from secretariat
        SocialSecretariat secretariat = employee.getSecretariat();
        if (secretariat != null) {
            secretariat.removeEmployee(employee);
            socialSecretariatRepository.save(secretariat);
        }

        // Delete employee using UserService
        userService.deleteUser(id);
    }

    /**
     * Converts a SecretariatEmployee entity to SecretariatEmployeeDto
     */
    public SecretariatEmployeeDto convertToDto(SecretariatEmployee employee) {
        SecretariatEmployeeDto dto = new SecretariatEmployeeDto();

        // Map base user properties
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setUsername(employee.getUsername());
        dto.setEmail(employee.getEmail());
        dto.setPhoneNumber(employee.getPhoneNumber());

        // Don't set password in DTO for security reasons

        // Map secretariat employee specific properties
        dto.setPosition(employee.getPosition());
        dto.setSpecialization(employee.getSpecialization());

        if (employee.getSecretariat() != null) {
            dto.setSecretariatId(employee.getSecretariat().getId());
            dto.setSecretariatName(employee.getSecretariat().getName());
        }

        return dto;
    }

    /**
     * Converts a SecretariatEmployee entity to UserDto, including employee-specific
     * fields
     */
    public UserDto convertToUserDto(SecretariatEmployee employee) {
        // First get the base user fields using UserService
        UserDto dto = userService.convertToDto(employee);

        // Add employee-specific fields
        dto.setPosition(employee.getPosition());
        dto.setSpecialization(employee.getSpecialization());

        if (employee.getSecretariat() != null) {
            dto.setSecretariatId(employee.getSecretariat().getId());
            dto.setSecretariatName(employee.getSecretariat().getName());
        }

        return dto;
    }

    /**
     * Converts a SecretariatEmployeeDto to UserDto
     */
    public UserDto convertDtoToUserDto(SecretariatEmployeeDto employeeDto) {
        UserDto dto = new UserDto();
        dto.setId(employeeDto.getId());
        dto.setUsername(employeeDto.getUsername());
        dto.setEmail(employeeDto.getEmail());
        dto.setFirstName(employeeDto.getFirstName());
        dto.setLastName(employeeDto.getLastName());
        dto.setPhoneNumber(employeeDto.getPhoneNumber());
        dto.setPosition(employeeDto.getPosition());
        dto.setSpecialization(employeeDto.getSpecialization());
        dto.setSecretariatId(employeeDto.getSecretariatId());
        dto.setSecretariatName(employeeDto.getSecretariatName());

        // Add ROLE_SECRETARIAT by default
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_SECRETARIAT");
        dto.setRoles(roles);

        return dto;
    }

    /**
     * Converts a list of SecretariatEmployee entities to UserDtos
     */
    public List<UserDto> convertToUserDtoList(List<SecretariatEmployee> employees) {
        return employees.stream()
                .map(this::convertToUserDto)
                .toList();
    }

    /**
     * Converts a list of SecretariatEmployeeDto to UserDtos
     */
    public List<UserDto> convertDtoToUserDtoList(List<SecretariatEmployeeDto> employeeDtos) {
        return employeeDtos.stream()
                .map(this::convertDtoToUserDto)
                .toList();
    }
}
