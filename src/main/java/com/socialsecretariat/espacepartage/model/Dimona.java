package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Transient;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "TDimona")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dimona {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String type;

    private Date entryDate;

    private Date exitDate;

    private String exitReason;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String onssReference;

    private String errorMessage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collaborator_id", nullable = false)
    private Collaborator collaborator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id")
    private Contrat contrat;

    @Transient
    private Status previousStatus;

    @PreUpdate
    public void onPreUpdate() {
        // Store the previous status before update
        if (this.previousStatus == null) {
            this.previousStatus = this.status;
        }
    }

    public void setStatus(Status newStatus) {
        this.previousStatus = this.status;
        this.status = newStatus;
    }

    public Status getPreviousStatus() {
        return this.previousStatus;
    }

    public boolean hasStatusChanged() {
        return this.previousStatus != null && !this.previousStatus.equals(this.status);
    }

    public enum Status {
        TO_CONFIRM,
        TO_SEND,
        IN_PROGRESS,
        REJECTED,
        ACCEPTED
    }
}
