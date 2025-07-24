package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TContrat")
@DiscriminatorValue("CONTRAT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contrat extends Document {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaborator_id", nullable = false)
    private Collaborator collaborator;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimona_in_id", nullable = false)
    private Dimona dimonaIn;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimona_out_id")
    private Dimona dimonaOut;
    
    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL)
    private List<Dimona> dimonaModifications = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "contrat_status", nullable = false)
    private ContratStatus contratStatus = ContratStatus.ACTIF;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    public enum ContratStatus {
        ACTIF,
        TERMINE
    }
}
