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
public class SecretariatEmployeeDto {

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

    // Only used for creating new employees
    private String password;

    private String position;

    private String specialization;

    @NotNull(message = "Secretariat ID is required")
    private UUID secretariatId;

    private String secretariatName; // For display purposes only
}