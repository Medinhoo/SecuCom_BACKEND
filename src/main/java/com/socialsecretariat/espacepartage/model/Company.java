package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "TCompany")
@Getter
@Setter
@ToString(exclude = { "contacts", "collaborators" })
@EqualsAndHashCode(exclude = { "contacts", "collaborators" })
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // nom
    @Column(nullable = false)
    private String name;

    // numeroTel
    @Column(name = "phone_number")
    private String phoneNumber;

    @Column
    private String email;

    @Column
    private String IBAN;

    // fondDeSecuriteDexistance
    @Column(name = "security_fund")
    private String securityFund;

    // assuranceAccidentDeTravail
    @Column(name = "work_accident_insurance")
    private String workAccidentInsurance;

    // numeroBCE
    @Column(unique = true)
    private String bceNumber;

    // numeroONSS
    @Column(unique = true)
    private String onssNumber;

    // formeJuridique
    @Column(name = "legal_form")
    private String legalForm;

    // denominationSociale
    @Column(name = "company_name")
    private String companyName;

    // dateCreation
    @Column(name = "creation_date")
    private LocalDate creationDate;

    // numeroTVA
    @Column(unique = true, name = "vat_number")
    private String vatNumber;

    // regimeTravail
    @Column(name = "work_regime")
    private String workRegime;

    // reductionSalaire
    @Column(name = "salary_reduction")
    private String salaryReduction;

    // secteurActivite
    @Column(name = "activity_sector")
    private String activitySector;

    // commissionParitaire
    @ElementCollection
    @CollectionTable(name = "company_joint_committees", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "joint_committee")
    private List<String> jointCommittees;

    // categorie
    private String category;

    // calendrierTravail
    @Column(name = "work_calendar")
    private String workCalendar;

    // dateDebutCollaboration
    @Column(name = "collaboration_start_date")
    private LocalDate collaborationStartDate;

    // formuleSouscrite
    @Column(name = "subscription_formula")
    private String subscriptionFormula;

    // frequenceDeclarationPP
    @Column(name = "declaration_frequency")
    private String declarationFrequency;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompanyContact> contacts = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private Set<Collaborator> collaborators = new HashSet<>();

    public void addContact(CompanyContact contact) {
        contacts.add(contact);
        contact.setCompany(this);
    }

    public void removeContact(CompanyContact contact) {
        contacts.remove(contact);
        contact.setCompany(null);
    }

    public void addCollaborator(Collaborator collaborator) {
        collaborators.add(collaborator);
        collaborator.setCompany(this);
    }

    public void removeCollaborator(Collaborator collaborator) {
        collaborators.remove(collaborator);
        collaborator.setCompany(null);
    }
}
