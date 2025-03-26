package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.CompanyContactDto;
import com.socialsecretariat.espacepartage.dto.CompanyContactUpdateDto;
import com.socialsecretariat.espacepartage.dto.UserDto;
import com.socialsecretariat.espacepartage.model.Company;
import com.socialsecretariat.espacepartage.model.CompanyContact;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.CompanyContactRepository;
import com.socialsecretariat.espacepartage.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompanyContactService {

    private final CompanyContactRepository companyContactRepository;
    private final CompanyRepository companyRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public CompanyContactService(
            CompanyContactRepository companyContactRepository,
            CompanyRepository companyRepository,
            UserService userService,
            PasswordEncoder passwordEncoder) {
        this.companyContactRepository = companyContactRepository;
        this.companyRepository = companyRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CompanyContactDto createCompanyContact(CompanyContactDto contactDto) {
        // Check if username already exists
        if (userService.existsByUsername(contactDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userService.existsByEmail(contactDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check if company exists
        Company company = companyRepository.findById(contactDto.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Company not found with ID: " + contactDto.getCompanyId()));

        // Create new contact
        CompanyContact contact = new CompanyContact();

        // Set user properties
        contact.setFirstName(contactDto.getFirstName());
        contact.setLastName(contactDto.getLastName());
        contact.setUsername(contactDto.getUsername());
        contact.setEmail(contactDto.getEmail());
        contact.setPhoneNumber(contactDto.getPhoneNumber());
        contact.setPassword(passwordEncoder.encode(contactDto.getPassword()));

        // Set company contact specific properties
        contact.setFonction(contactDto.getFonction());
        contact.setPermissions(contactDto.getPermissions());
        contact.setCompany(company);

        // Set default user properties
        contact.setAccountStatus(User.AccountStatus.ACTIVE);
        contact.setCreatedAt(LocalDateTime.now());

        // Set role
        Set<User.Role> roles = new HashSet<>();
        roles.add(User.Role.ROLE_COMPANY);
        contact.setRoles(roles);

        // Save contact
        CompanyContact savedContact = companyContactRepository.save(contact);

        // Convert to DTO
        return convertToDto(savedContact);
    }

    @Transactional(readOnly = true)
    public List<CompanyContactDto> getAllCompanyContacts() {
        return companyContactRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompanyContactDto getCompanyContactById(UUID id) {
        CompanyContact contact = companyContactRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company contact not found with ID: " + id));
        return convertToDto(contact);
    }

    @Transactional(readOnly = true)
    public List<CompanyContactDto> getCompanyContactsByCompanyId(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new EntityNotFoundException("Company not found with ID: " + companyId);
        }
        return companyContactRepository.findByCompanyId(companyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanyContactDto updateCompanyContact(UUID id, CompanyContactUpdateDto contactDto) {
        CompanyContact contact = companyContactRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company contact not found with ID: " + id));

        // Create updates map for user properties, only including non-null values
        Map<String, Object> userUpdates = new HashMap<>();
        if (contactDto.getFirstName() != null) {
            userUpdates.put("firstName", contactDto.getFirstName());
        }
        if (contactDto.getLastName() != null) {
            userUpdates.put("lastName", contactDto.getLastName());
        }
        if (contactDto.getEmail() != null) {
            userUpdates.put("email", contactDto.getEmail());
        }
        if (contactDto.getPhoneNumber() != null) {
            userUpdates.put("phoneNumber", contactDto.getPhoneNumber());
        }

        // Only update user fields if there are changes
        if (!userUpdates.isEmpty()) {
            userService.updateUser(id, userUpdates, null);
        }

        // Check if company is being changed and if it exists
        if (contactDto.getCompanyId() != null &&
                !contact.getCompany().getId().equals(contactDto.getCompanyId())) {
            Company newCompany = companyRepository.findById(contactDto.getCompanyId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Company not found with ID: " + contactDto.getCompanyId()));
            contact.setCompany(newCompany);
        }

        // Update company contact specific properties only if provided
        if (contactDto.getFonction() != null) {
            contact.setFonction(contactDto.getFonction());
        }
        if (contactDto.getPermissions() != null) {
            contact.setPermissions(contactDto.getPermissions());
        }

        // Only update password if provided and not empty
        if (contactDto.getPassword() != null && !contactDto.getPassword().isEmpty()) {
            userService.adminUpdateUserPassword(id, contactDto.getPassword());
        }

        // Save updated contact
        CompanyContact updatedContact = companyContactRepository.save(contact);

        // Convert to DTO
        return convertToDto(updatedContact);
    }

    @Transactional
    public void deleteCompanyContact(UUID id) {
        companyContactRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company contact not found with ID: " + id));

        // Delete contact using UserService
        userService.deleteUser(id);
    }

    public CompanyContactDto convertToDto(CompanyContact contact) {
        CompanyContactDto dto = new CompanyContactDto();

        // Map base user properties
        dto.setId(contact.getId());
        dto.setFirstName(contact.getFirstName());
        dto.setLastName(contact.getLastName());
        dto.setUsername(contact.getUsername());
        dto.setEmail(contact.getEmail());
        dto.setPhoneNumber(contact.getPhoneNumber());

        // Map company contact specific properties
        dto.setFonction(contact.getFonction());
        dto.setPermissions(contact.getPermissions());

        if (contact.getCompany() != null) {
            dto.setCompanyId(contact.getCompany().getId());
            dto.setCompanyName(contact.getCompany().getName());
        }

        return dto;
    }

    public UserDto convertToUserDto(CompanyContact contact) {
        UserDto dto = userService.convertToDto(contact);

        dto.setFonction(contact.getFonction());
        dto.setPermissions(contact.getPermissions());

        if (contact.getCompany() != null) {
            dto.setCompanyId(contact.getCompany().getId());
            dto.setCompanyName(contact.getCompany().getName());
        }

        return dto;
    }

    public UserDto convertDtoToUserDto(CompanyContactDto contactDto) {
        UserDto dto = new UserDto();
        dto.setId(contactDto.getId());
        dto.setUsername(contactDto.getUsername());
        dto.setEmail(contactDto.getEmail());
        dto.setFirstName(contactDto.getFirstName());
        dto.setLastName(contactDto.getLastName());
        dto.setPhoneNumber(contactDto.getPhoneNumber());
        dto.setFonction(contactDto.getFonction());
        dto.setPermissions(contactDto.getPermissions());
        dto.setCompanyId(contactDto.getCompanyId());
        dto.setCompanyName(contactDto.getCompanyName());

        // Add ROLE_COMPANY by default
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_COMPANY");
        dto.setRoles(roles);

        return dto;
    }

    public List<UserDto> convertToUserDtoList(List<CompanyContact> contacts) {
        return contacts.stream()
                .map(this::convertToUserDto)
                .toList();
    }

    public List<UserDto> convertDtoToUserDtoList(List<CompanyContactDto> contactDtos) {
        return contactDtos.stream()
                .map(this::convertDtoToUserDto)
                .toList();
    }
}
