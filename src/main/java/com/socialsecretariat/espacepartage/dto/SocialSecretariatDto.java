package com.socialsecretariat.espacepartage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialSecretariatDto {

    private UUID id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Company number is required")
    private String companyNumber;

    private String address;

    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    private String website;
}