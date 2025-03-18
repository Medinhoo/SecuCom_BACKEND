package com.socialsecretariat.espacepartage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyContactDto {

    private UUID id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "Username is required")
    private String username;

    // Only used for creating new contacts
    private String password;

    private String fonction;
    private String permissions;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    private String companyName; // For display purposes only
}
