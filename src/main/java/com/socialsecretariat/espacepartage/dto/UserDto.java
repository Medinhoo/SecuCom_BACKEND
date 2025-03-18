package com.socialsecretariat.espacepartage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private List<String> roles;
    private String accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Fields specific to SecretariatEmployee
    private String position;
    private String specialization;
    private UUID secretariatId;
    private String secretariatName;

    // Fields specific to CompanyContact
    private String fonction;
    private String permissions;
    private UUID companyId;
    private String companyName;
}
