package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "TSocialSecretariat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialSecretariat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    private String companyNumber; // Business identifier

    private String address;

    private String phone;

    @Email
    private String email;

    private String website;
}
