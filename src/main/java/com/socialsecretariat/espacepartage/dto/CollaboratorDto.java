package com.socialsecretariat.espacepartage.dto;

import com.socialsecretariat.espacepartage.model.Address;
import com.socialsecretariat.espacepartage.model.Collaborator.CollaboratorType;
import com.socialsecretariat.espacepartage.model.Collaborator.Day;
import com.socialsecretariat.espacepartage.model.Collaborator.WorkDurationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorDto {
    private UUID id;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    private String nationality;
    private LocalDate birthDate;
    private String birthPlace;
    private String gender;
    private String language;
    private String civilStatus;
    private LocalDate civilStatusDate;
    private String partnerName;
    private LocalDate partnerBirthDate;
    private List<String> dependents;

    @Valid
    private Address address;

    @Size(max = 20)
    private String nationalNumber;

    @NotNull
    private LocalDate serviceEntryDate;

    private CollaboratorType type;
    private String jobFunction;
    private String contractType;
    private String workRegime;
    private WorkDurationType workDurationType;
    private Map<Day, String> typicalSchedule;
    private BigDecimal salary;
    private String jointCommittee;
    private String taskDescription;
    private List<String> extraLegalBenefits;
    private String iban;

    @NotNull
    private UUID companyId;

    @Valid
    private Address establishmentUnitAddress;

    // Audit fields
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
