package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
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

    private String status;

    private String onssReference;

    private String errorMessage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collaborator_id", nullable = false)
    private Collaborator collaborator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
