package com.socialsecretariat.espacepartage.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialsecretariat.espacepartage.dto.EmailTemplateDto;
import com.socialsecretariat.espacepartage.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {
    
    private final ObjectMapper objectMapper;
    private final CompanyService companyService;
    private final CollaboratorService collaboratorService;
    private final UserService userService;
    
    public EmailTemplateDto processEmailTemplate(DocumentTemplate template, Document document) {
        try {
            EmailTemplateDto emailTemplate = new EmailTemplateDto();
            
            // Check if email is enabled for this template
            emailTemplate.setEmailEnabled(template.getEmailEnabled() != null ? template.getEmailEnabled() : true);
            
            if (!emailTemplate.isEmailEnabled()) {
                return emailTemplate;
            }
            
            // Process subject with variable replacement
            String processedSubject = processTemplateVariables(
                template.getDefaultEmailSubject() != null ? template.getDefaultEmailSubject() : "Document généré",
                document
            );
            emailTemplate.setDefaultSubject(processedSubject);
            
            // Process body with variable replacement
            String processedBody = processTemplateVariables(
                template.getDefaultEmailBody() != null ? template.getDefaultEmailBody() : 
                "Bonjour,\n\nVeuillez trouver ci-joint le document généré.\n\nCordialement",
                document
            );
            emailTemplate.setDefaultBody(processedBody);
            
            // Process recipients
            emailTemplate.setDefaultRecipients(resolveRecipients(template.getDefaultRecipients(), document));
            emailTemplate.setDefaultCcRecipients(resolveRecipients(template.getDefaultCcRecipients(), document));
            
            return emailTemplate;
            
        } catch (Exception e) {
            log.error("Error processing email template for document: {}", document.getId(), e);
            
            // Return a basic template in case of error
            EmailTemplateDto fallbackTemplate = new EmailTemplateDto();
            fallbackTemplate.setEmailEnabled(true);
            fallbackTemplate.setDefaultSubject("Document généré");
            fallbackTemplate.setDefaultBody("Bonjour,\n\nVeuillez trouver ci-joint le document généré.\n\nCordialement");
            fallbackTemplate.setDefaultRecipients(new ArrayList<>());
            fallbackTemplate.setDefaultCcRecipients(new ArrayList<>());
            
            return fallbackTemplate;
        }
    }
    
    private String processTemplateVariables(String template, Document document) {
        if (template == null) {
            return "";
        }
        
        String processed = template;
        
        // Replace common variables
        processed = processed.replace("{templateDisplayName}", 
            document.getTemplate() != null ? document.getTemplate().getDisplayName() : "");
        processed = processed.replace("{documentName}", 
            document.getGeneratedFileName() != null ? document.getGeneratedFileName() : "");
        processed = processed.replace("{generationDate}", 
            document.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        
        // Replace company variables
        if (document.getCompany() != null) {
            processed = processed.replace("{companyName}", document.getCompany().getName());
        }
        
        // Replace collaborator variables (only for contracts)
        if (document instanceof Contrat) {
            Contrat contrat = (Contrat) document;
            if (contrat.getCollaborator() != null) {
                String collaboratorName = contrat.getCollaborator().getFirstName() + " " + contrat.getCollaborator().getLastName();
                processed = processed.replace("{collaboratorName}", collaboratorName);
            }
        }
        
        // Replace user variables
        if (document.getGeneratedBy() != null) {
            processed = processed.replace("{generatedByName}", document.getGeneratedBy().getUsername());
        }
        
        return processed;
    }
    
    private List<String> resolveRecipients(String recipientsJson, Document document) {
        List<String> resolvedRecipients = new ArrayList<>();
        
        if (recipientsJson == null || recipientsJson.trim().isEmpty()) {
            return resolvedRecipients;
        }
        
        try {
            // Parse JSON array of recipient types
            List<String> recipientTypes = objectMapper.readValue(recipientsJson, new TypeReference<List<String>>() {});
            
            for (String recipientType : recipientTypes) {
                List<String> emails = resolveRecipientType(recipientType, document);
                resolvedRecipients.addAll(emails);
            }
            
        } catch (Exception e) {
            log.warn("Failed to parse recipients JSON: {}, treating as single email", recipientsJson);
            // If JSON parsing fails, treat as a single email address
            if (isValidEmail(recipientsJson)) {
                resolvedRecipients.add(recipientsJson);
            }
        }
        
        return resolvedRecipients;
    }
    
    private List<String> resolveRecipientType(String recipientType, Document document) {
        List<String> emails = new ArrayList<>();
        
        try {
            switch (recipientType.toUpperCase()) {
                case "COMPANY_EMAIL":
                    if (document.getCompany() != null) {
                        // Get company email from company service
                        // This would need to be implemented based on your Company model
                        log.debug("Resolving company email for company ID: {}", document.getCompany().getId());
                        // emails.add(companyService.getCompanyEmail(document.getCompany().getId()));
                    }
                    break;
                    
                case "COLLABORATOR_EMAIL":
                    if (document instanceof Contrat) {
                        Contrat contrat = (Contrat) document;
                        if (contrat.getCollaborator() != null) {
                            // Get collaborator email from collaborator service
                            log.debug("Resolving collaborator email for collaborator ID: {}", contrat.getCollaborator().getId());
                            // emails.add(collaboratorService.getCollaboratorEmail(contrat.getCollaborator().getId()));
                        }
                    }
                    break;
                    
                case "CURRENT_USER_EMAIL":
                    if (document.getGeneratedBy() != null) {
                        // Get current user email
                        log.debug("Resolving current user email for user ID: {}", document.getGeneratedBy().getId());
                        // emails.add(userService.getUserEmail(document.getGeneratedBy().getId()));
                    }
                    break;
                    
                default:
                    // Check if it's a custom email (starts with CUSTOM:)
                    if (recipientType.startsWith("CUSTOM:")) {
                        String customEmail = recipientType.substring(7); // Remove "CUSTOM:" prefix
                        if (isValidEmail(customEmail)) {
                            emails.add(customEmail);
                        }
                    } else if (isValidEmail(recipientType)) {
                        // Direct email address
                        emails.add(recipientType);
                    }
                    break;
            }
        } catch (Exception e) {
            log.warn("Failed to resolve recipient type: {}", recipientType, e);
        }
        
        return emails;
    }
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Basic email validation
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
}
