package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TNotification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(length = 500)
    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "is_read")
    private boolean read = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "entity_id")
    private UUID entityId;

    public enum NotificationType {
        DIMONA_CREATED,
        DIMONA_STATUS_CHANGED,
        COLLABORATOR_CREATED,
        COMPANY_COMPLETED,
        COMPANY_DATA_CONFIRMED,
        COMPANY_DATA_RECONFIRMATION_REQUIRED
    }

    // Helper method to mark as read
    public void markAsRead() {
        this.read = true;
    }

    // Helper method to check if notification is unread
    public boolean isUnread() {
        return !this.read;
    }
}
