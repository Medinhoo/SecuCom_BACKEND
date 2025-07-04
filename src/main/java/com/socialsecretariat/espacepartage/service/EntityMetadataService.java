package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.EntityFieldInfo;
import com.socialsecretariat.espacepartage.model.Address;
import com.socialsecretariat.espacepartage.model.Collaborator;
import com.socialsecretariat.espacepartage.model.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntityMetadataService {
    
    public List<EntityFieldInfo> getCompanyFields() {
        List<EntityFieldInfo> fields = new ArrayList<>();
        
        // Champs directs de Company
        fields.add(new EntityFieldInfo("name", "Nom de l'entreprise", "string"));
        fields.add(new EntityFieldInfo("phoneNumber", "Numéro de téléphone", "string"));
        fields.add(new EntityFieldInfo("email", "Email", "string"));
        fields.add(new EntityFieldInfo("IBAN", "IBAN", "string"));
        fields.add(new EntityFieldInfo("securityFund", "Fonds de sécurité d'existence", "string"));
        fields.add(new EntityFieldInfo("workAccidentInsurance", "Assurance accident de travail", "string"));
        fields.add(new EntityFieldInfo("bceNumber", "Numéro BCE", "string"));
        fields.add(new EntityFieldInfo("onssNumber", "Numéro ONSS", "string"));
        fields.add(new EntityFieldInfo("legalForm", "Forme juridique", "string"));
        fields.add(new EntityFieldInfo("companyName", "Dénomination sociale", "string"));
        fields.add(new EntityFieldInfo("creationDate", "Date de création", "date"));
        fields.add(new EntityFieldInfo("vatNumber", "Numéro TVA", "string"));
        fields.add(new EntityFieldInfo("workRegime", "Régime de travail", "string"));
        fields.add(new EntityFieldInfo("salaryReduction", "Réduction salaire", "string"));
        fields.add(new EntityFieldInfo("activitySector", "Secteur d'activité", "string"));
        fields.add(new EntityFieldInfo("category", "Catégorie", "string"));
        fields.add(new EntityFieldInfo("workCalendar", "Calendrier de travail", "string"));
        fields.add(new EntityFieldInfo("collaborationStartDate", "Date début collaboration", "date"));
        fields.add(new EntityFieldInfo("subscriptionFormula", "Formule souscrite", "string"));
        fields.add(new EntityFieldInfo("declarationFrequency", "Fréquence déclaration PP", "string"));
        
        // Champs de l'adresse
        fields.add(new EntityFieldInfo("address.street", "Adresse - Rue", "string"));
        fields.add(new EntityFieldInfo("address.number", "Adresse - Numéro", "string"));
        fields.add(new EntityFieldInfo("address.box", "Adresse - Boîte", "string"));
        fields.add(new EntityFieldInfo("address.postalCode", "Adresse - Code postal", "string"));
        fields.add(new EntityFieldInfo("address.city", "Adresse - Ville", "string"));
        fields.add(new EntityFieldInfo("address.country", "Adresse - Pays", "string"));
        
        return fields;
    }
    
    public List<EntityFieldInfo> getCollaboratorFields() {
        List<EntityFieldInfo> fields = new ArrayList<>();
        
        // Champs directs de Collaborator
        fields.add(new EntityFieldInfo("lastName", "Nom de famille", "string"));
        fields.add(new EntityFieldInfo("firstName", "Prénom", "string"));
        fields.add(new EntityFieldInfo("nationality", "Nationalité", "string"));
        fields.add(new EntityFieldInfo("birthDate", "Date de naissance", "date"));
        fields.add(new EntityFieldInfo("birthPlace", "Lieu de naissance", "string"));
        fields.add(new EntityFieldInfo("gender", "Genre", "string"));
        fields.add(new EntityFieldInfo("language", "Langue", "string"));
        fields.add(new EntityFieldInfo("civilStatus", "État civil", "string"));
        fields.add(new EntityFieldInfo("civilStatusDate", "Date état civil", "date"));
        fields.add(new EntityFieldInfo("partnerName", "Nom du partenaire", "string"));
        fields.add(new EntityFieldInfo("partnerBirthDate", "Date naissance partenaire", "date"));
        fields.add(new EntityFieldInfo("nationalNumber", "Numéro national", "string"));
        fields.add(new EntityFieldInfo("serviceEntryDate", "Date d'entrée en service", "date"));
        fields.add(new EntityFieldInfo("type", "Type de collaborateur", "string"));
        fields.add(new EntityFieldInfo("jobFunction", "Fonction", "string"));
        fields.add(new EntityFieldInfo("contractType", "Type de contrat", "string"));
        fields.add(new EntityFieldInfo("workRegime", "Régime de travail", "string"));
        fields.add(new EntityFieldInfo("workDurationType", "Type durée travail", "string"));
        fields.add(new EntityFieldInfo("salary", "Salaire", "decimal"));
        fields.add(new EntityFieldInfo("jointCommittee", "Commission paritaire", "string"));
        fields.add(new EntityFieldInfo("taskDescription", "Description des tâches", "text"));
        fields.add(new EntityFieldInfo("iban", "IBAN", "string"));
        
        // Champs de l'adresse personnelle
        fields.add(new EntityFieldInfo("address.street", "Adresse - Rue", "string"));
        fields.add(new EntityFieldInfo("address.number", "Adresse - Numéro", "string"));
        fields.add(new EntityFieldInfo("address.box", "Adresse - Boîte", "string"));
        fields.add(new EntityFieldInfo("address.postalCode", "Adresse - Code postal", "string"));
        fields.add(new EntityFieldInfo("address.city", "Adresse - Ville", "string"));
        fields.add(new EntityFieldInfo("address.country", "Adresse - Pays", "string"));
        
        // Champs de l'adresse de l'unité d'établissement
        fields.add(new EntityFieldInfo("establishmentUnitAddress.street", "Établissement - Rue", "string"));
        fields.add(new EntityFieldInfo("establishmentUnitAddress.number", "Établissement - Numéro", "string"));
        fields.add(new EntityFieldInfo("establishmentUnitAddress.box", "Établissement - Boîte", "string"));
        fields.add(new EntityFieldInfo("establishmentUnitAddress.postalCode", "Établissement - Code postal", "string"));
        fields.add(new EntityFieldInfo("establishmentUnitAddress.city", "Établissement - Ville", "string"));
        fields.add(new EntityFieldInfo("establishmentUnitAddress.country", "Établissement - Pays", "string"));
        
        // Champs composites utiles
        fields.add(new EntityFieldInfo("firstName lastName", "Nom complet", "string"));
        
        return fields;
    }
    
    private String getFieldType(Class<?> fieldType) {
        if (fieldType == String.class) {
            return "string";
        } else if (fieldType == LocalDate.class) {
            return "date";
        } else if (fieldType == BigDecimal.class) {
            return "decimal";
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return "number";
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return "boolean";
        } else if (fieldType.isEnum()) {
            return "string"; // Les enums sont traités comme des strings pour l'instant
        } else {
            return "string"; // Par défaut
        }
    }
}
