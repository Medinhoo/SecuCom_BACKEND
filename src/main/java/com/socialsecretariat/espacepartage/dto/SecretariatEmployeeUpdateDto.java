package com.socialsecretariat.espacepartage.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecretariatEmployeeUpdateDto {
    private String firstName;
    private String lastName;
    private String username;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;
    private String password;
    private String position;
    private String specialization;
    private UUID secretariatId;
}
