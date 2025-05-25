package com.socialsecretariat.espacepartage.dto;

import com.socialsecretariat.espacepartage.model.Notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    
    private UUID id;
    private String message;
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;
    private UUID recipientId;
    private UUID entityId;
}
