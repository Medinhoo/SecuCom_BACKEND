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
@Table(name = "TDimonaStatusHistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "dimona_id", nullable = false)
    private UUID dimonaId;

    @Column(name = "previous_status", length = 50)
    private String previousStatus;

    @NotBlank
    @Column(name = "new_status", nullable = false, length = 50)
    private String newStatus;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedBy;

    @CreatedDate
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    // Constructor for easy creation
    public StatusHistory(UUID dimonaId, String previousStatus, 
                        String newStatus, String changeReason, User changedBy) {
        this.dimonaId = dimonaId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changeReason = changeReason;
        this.changedBy = changedBy;
    }

    // Helper method to check if this is a status creation (no previous status)
    public boolean isStatusCreation() {
        return previousStatus == null || previousStatus.trim().isEmpty();
    }

    // Helper method to get a formatted change description
    public String getChangeDescription() {
        if (isStatusCreation()) {
            return String.format("Statut initial défini: %s", newStatus);
        }
        return String.format("Statut changé de '%s' vers '%s'", previousStatus, newStatus);
    }
}
