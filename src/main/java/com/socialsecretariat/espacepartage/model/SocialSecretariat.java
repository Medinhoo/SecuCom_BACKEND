package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "TSocialSecretariat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "employees" })
@ToString(exclude = { "employees" })
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

    @OneToMany(mappedBy = "secretariat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SecretariatEmployee> employees = new HashSet<>();

    // Helper methods for relationship management
    public void addEmployee(SecretariatEmployee employee) {
        employees.add(employee);
        employee.setSecretariat(this);
    }

    public void removeEmployee(SecretariatEmployee employee) {
        employees.remove(employee);
        employee.setSecretariat(null);
    }
}