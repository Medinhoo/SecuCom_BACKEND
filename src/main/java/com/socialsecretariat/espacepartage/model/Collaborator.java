package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * Entity representing a Collaborator (employee) in the system
 */
@Entity
@Table(name = "TCollaborator")
@Getter
@Setter
@ToString(exclude = "company")
@EqualsAndHashCode(exclude = "company")
@NoArgsConstructor
@AllArgsConstructor
public class Collaborator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name")
    private String lastName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 50)
    @Column(name = "nationality")
    private String nationality;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Size(max = 100)
    @Column(name = "birth_place")
    private String birthPlace;

    @Size(max = 10)
    @Column(name = "gender")
    private String gender;

    @Size(max = 20)
    @Column(name = "language")
    private String language;

    @Size(max = 100)
    @Column(name = "civil_status")
    private String civilStatus;

    @Column(name = "civil_status_date")
    private LocalDate civilStatusDate;

    @Size(max = 100)
    @Column(name = "partner_name")
    private String partnerName;

    @Column(name = "partner_birth_date")
    private LocalDate partnerBirthDate;

    @Column(name = "dependents")
    @ElementCollection
    private List<String> dependents;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "address_street")),
            @AttributeOverride(name = "number", column = @Column(name = "address_number")),
            @AttributeOverride(name = "box", column = @Column(name = "address_box")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "address_postal_code")),
            @AttributeOverride(name = "city", column = @Column(name = "address_city")),
            @AttributeOverride(name = "country", column = @Column(name = "address_country"))
    })
    private Address address;

    @Size(max = 20)
    @Column(name = "national_number", unique = true)
    private String nationalNumber;

    @NotNull
    @Column(name = "service_entry_date")
    private LocalDate serviceEntryDate;

    @Column(name = "type")
    private CollaboratorType type;

    @Size(max = 100)
    @Column(name = "job_function")
    private String jobFunction;

    @Size(max = 50)
    @Column(name = "contract_type")
    private String contractType;

    @Size(max = 50)
    @Column(name = "work_regime")
    private String workRegime;

    @Column(name = "work_duration_type")
    private WorkDurationType workDurationType; // fixed or variable

    @ElementCollection
    @CollectionTable(name = "collaborator_schedule", joinColumns = @JoinColumn(name = "collaborator_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "schedule")
    private Map<Day, String> typicalSchedule; // filled if workDurationType is fixed

    @Column(name = "salary")
    private BigDecimal salary;

    @Size(max = 50)
    @Column(name = "joint_committee")
    private String jointCommittee;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Column(name = "extra_legal_benefits")
    @ElementCollection
    private List<String> extraLegalBenefits;

    @Size(max = 100)
    @Column(name = "iban")
    private String iban;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
    @OneToMany(mappedBy = "collaborator", cascade = CascadeType.ALL)
    private List<Contrat> contrats = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "establishment_street")),
            @AttributeOverride(name = "number", column = @Column(name = "establishment_number")),
            @AttributeOverride(name = "box", column = @Column(name = "establishment_box")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "establishment_postal_code")),
            @AttributeOverride(name = "city", column = @Column(name = "establishment_city")),
            @AttributeOverride(name = "country", column = @Column(name = "establishment_country"))
    })
    private Address establishmentUnitAddress;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDate updatedAt;

    public enum CollaboratorType {
        EMPLOYEE,
        WORKER,
        FREELANCE,
        INTERN,
        STUDENT
    }

    public enum Day {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY
    }

    public enum WorkDurationType {
        FIXED,
        VARIABLE
    }

}
