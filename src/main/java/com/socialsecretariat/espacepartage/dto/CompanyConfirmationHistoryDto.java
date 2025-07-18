package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyConfirmationHistoryDto {
    
    private UUID id;
    private UUID companyId;
    private UUID confirmedByUserId;
    private String confirmedByUserName;
    private LocalDateTime confirmedAt;
}
