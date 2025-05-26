package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryDto {
    
    private UUID id;
    private UUID dimonaId;
    private String previousStatus;
    private String newStatus;
    private String changeReason;
    private UUID changedByUserId;
    private String changedByUserName;
    private LocalDateTime changedAt;
    private String changeDescription;
    private boolean isStatusCreation;
}
