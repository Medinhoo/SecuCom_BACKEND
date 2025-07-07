package com.socialsecretariat.espacepartage.dto;

import com.socialsecretariat.espacepartage.model.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private UUID id;

    @NotBlank(message = "Name is required")
    private String name; // nom

    private String phoneNumber; // numeroTel

    @Email(message = "Invalid email format")
    private String email;

    private String IBAN;

    private String securityFund; // fondDeSecuriteDexistance

    private String workAccidentInsurance; // assuranceAccidentDeTravail

    @NotBlank(message = "BCE number is required")
    private String bceNumber; // numeroBCE

    @NotBlank(message = "ONSS number is required")
    private String onssNumber; // numeroONSS

    private String legalForm; // formeJuridique

    private String companyName; // denominationSociale

    private LocalDate creationDate; // dateCreation

    private String vatNumber; // numeroTVA

    private String workRegime; // regimeTravail

    private String salaryReduction; // reductionSalaire

    private String activitySector; // secteurActivite

    private List<String> jointCommittees; // commissionParitaire

    private String category; // categorie

    private String workCalendar; // calendrierTravail

    private LocalDate collaborationStartDate; // dateDebutCollaboration

    private String subscriptionFormula; // formuleSouscrite

    private String declarationFrequency; // frequenceDeclarationPP

    private Address address; // Adresse de l'entreprise
}
