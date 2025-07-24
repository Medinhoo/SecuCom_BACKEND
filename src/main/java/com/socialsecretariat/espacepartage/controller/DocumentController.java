package com.socialsecretariat.espacepartage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialsecretariat.espacepartage.dto.*;
import com.socialsecretariat.espacepartage.model.Document;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.service.DocumentService;
import com.socialsecretariat.espacepartage.service.DocumentTemplateService;
import com.socialsecretariat.espacepartage.service.EntityMetadataService;
import com.socialsecretariat.espacepartage.service.TemplateAnalysisService;
import com.socialsecretariat.espacepartage.service.EmailService;
import com.socialsecretariat.espacepartage.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DocumentController {
    
    private final DocumentTemplateService documentTemplateService;
    private final DocumentService documentService;
    private final UserService userService;
    private final TemplateAnalysisService templateAnalysisService;
    private final EntityMetadataService entityMetadataService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    // ===== ENDPOINTS TEMPLATES =====
    
    @GetMapping("/templates")
    public ResponseEntity<List<DocumentTemplateDto>> getAllTemplates() {
        List<DocumentTemplateDto> templates = documentTemplateService.getAllActiveTemplates();
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/templates/{templateId}")
    public ResponseEntity<DocumentTemplateDto> getTemplate(@PathVariable UUID templateId) {
        Optional<DocumentTemplateDto> template = documentTemplateService.getTemplateById(templateId);
        return template.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/templates/{templateId}/variables")
    public ResponseEntity<List<TemplateVariableDto>> getTemplateVariables(@PathVariable UUID templateId) {
        List<TemplateVariableDto> variables = documentTemplateService.getTemplateVariables(templateId);
        return ResponseEntity.ok(variables);
    }
    
    @GetMapping("/templates/by-name/{templateName}/variables")
    public ResponseEntity<List<TemplateVariableDto>> getTemplateVariablesByName(@PathVariable String templateName) {
        List<TemplateVariableDto> variables = documentTemplateService.getTemplateVariablesByName(templateName);
        return ResponseEntity.ok(variables);
    }
    
    // ===== ENDPOINTS GÉNÉRATION =====
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateDocument(@RequestBody DocumentGenerationRequestDto request) {
        try {
            UUID userId = getCurrentUserId();
            Object response = documentService.generateDocument(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating document", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== ENDPOINTS DOCUMENTS =====
    
    @GetMapping
    public ResponseEntity<List<?>> getAllDocuments(@RequestParam(required = false) String type) {
        if ("CONTRAT".equals(type)) {
            List<ContratDto> contrats = documentService.getAllContrats();
            return ResponseEntity.ok(contrats);
        } else {
            List<DocumentDto> documents = documentService.getAllDocuments();
            return ResponseEntity.ok(documents);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocumentById(@PathVariable UUID id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting document", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ===== ENDPOINTS CONTRATS =====
    
    @PutMapping("/contrats/{id}/terminer")
    public ResponseEntity<?> terminerContrat(@PathVariable UUID id) {
        try {
            documentService.terminerContrat(id);
            return ResponseEntity.ok(Map.of("message", "Contrat terminé avec succès"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error terminating contract", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de la terminaison du contrat"));
        }
    }
    
    @PutMapping("/contrats/{id}/activer")
    public ResponseEntity<?> activerContrat(@PathVariable UUID id) {
        try {
            documentService.activerContrat(id);
            return ResponseEntity.ok(Map.of("message", "Contrat activé avec succès"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error activating contract", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de l'activation du contrat"));
        }
    }
    
    // ===== ENDPOINTS TÉLÉCHARGEMENT =====
    
    @GetMapping("/{documentId}/download-pdf")
    public ResponseEntity<Resource> downloadPdf(@PathVariable UUID documentId) {
        try {
            Optional<DocumentDto> documentOpt = documentService.getDocumentById(documentId);
            
            if (documentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            DocumentDto document = documentOpt.get();
            
            if (document.getStatus() != Document.GenerationStatus.COMPLETED) {
                return ResponseEntity.badRequest().build();
            }
            
            String pdfFilePath = document.getPdfFilePath();
            if (pdfFilePath == null) {
                return ResponseEntity.notFound().build();
            }
            
            Path pdfPath = Paths.get(pdfFilePath);
            if (!Files.exists(pdfPath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(pdfPath.toFile());
            String fileName = pdfPath.getFileName().toString();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading PDF for document: {}", documentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{documentId}/download-docx")
    public ResponseEntity<Resource> downloadDocx(@PathVariable UUID documentId) {
        try {
            Optional<DocumentDto> documentOpt = documentService.getDocumentById(documentId);
            
            if (documentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            DocumentDto document = documentOpt.get();
            String docxFilePath = document.getGeneratedFilePath();
            
            if (docxFilePath == null) {
                return ResponseEntity.notFound().build();
            }
            
            Path docxPath = Paths.get(docxFilePath);
            if (!Files.exists(docxPath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(docxPath.toFile());
            String fileName = docxPath.getFileName().toString();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading DOCX for document: {}", documentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ===== ENDPOINTS CRÉATION DE TEMPLATES =====
    
    @PostMapping("/templates/analyze")
    public ResponseEntity<?> analyzeTemplate(@RequestParam("file") MultipartFile file) {
        try {
            if (!templateAnalysisService.isValidDocxFile(file)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le fichier doit être au format DOCX"));
            }
            
            TemplateAnalysisResult result = templateAnalysisService.extractVariablesFromDocx(file);
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            log.error("Erreur lors de l'analyse du template", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de l'analyse du fichier: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'analyse du template", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur inattendue: " + e.getMessage()));
        }
    }
    
    @GetMapping("/templates/metadata")
    public ResponseEntity<Map<String, List<EntityFieldInfo>>> getEntityMetadata() {
        Map<String, List<EntityFieldInfo>> metadata = new HashMap<>();
        metadata.put("Company", entityMetadataService.getCompanyFields());
        metadata.put("Collaborator", entityMetadataService.getCollaboratorFields());
        return ResponseEntity.ok(metadata);
    }
    
    @GetMapping("/templates/check-name/{name}")
    public ResponseEntity<Map<String, Boolean>> checkTemplateName(@PathVariable String name) {
        boolean available = documentTemplateService.isTemplateNameAvailable(name);
        return ResponseEntity.ok(Map.of("available", available));
    }
    
    @PostMapping("/templates/create")
    public ResponseEntity<?> createTemplate(
            @RequestParam("templateName") String templateName,
            @RequestParam("displayName") String displayName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("docxFile") MultipartFile docxFile,
            @RequestParam("mappings") String mappingsJson,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "emailEnabled", required = false) Boolean emailEnabled,
            @RequestParam(value = "defaultEmailSubject", required = false) String defaultEmailSubject,
            @RequestParam(value = "defaultEmailBody", required = false) String defaultEmailBody,
            @RequestParam(value = "defaultRecipients", required = false) String defaultRecipients,
            @RequestParam(value = "defaultCcRecipients", required = false) String defaultCcRecipients) {
        
        try {
            // Validation du fichier
            if (!templateAnalysisService.isValidDocxFile(docxFile)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le fichier doit être au format DOCX"));
            }
            
            // Parse des mappings JSON
            List<VariableMapping> mappings = objectMapper.readValue(mappingsJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, VariableMapping.class));
            
            // Créer la requête
            CreateTemplateRequest request = new CreateTemplateRequest();
            request.setTemplateName(templateName);
            request.setDisplayName(displayName);
            request.setDescription(description);
            request.setDocxFile(docxFile);
            request.setMappings(mappings);
            
            // Ajouter les paramètres email
            request.setEmailEnabled(emailEnabled);
            request.setDefaultEmailSubject(defaultEmailSubject);
            request.setDefaultEmailBody(defaultEmailBody);
            request.setDefaultRecipients(defaultRecipients);
            request.setDefaultCcRecipients(defaultCcRecipients);
            
            // Créer le template
            DocumentTemplateDto createdTemplate = documentTemplateService.createTemplateFromMapping(request);
            
            return ResponseEntity.ok(createdTemplate);
            
        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation lors de la création du template: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            log.error("Erreur I/O lors de la création du template", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la sauvegarde: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la création du template", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur inattendue: " + e.getMessage()));
        }
    }
    
    // ===== ENDPOINTS EMAIL =====
    
    @GetMapping("/{documentId}/email-template")
    public ResponseEntity<EmailTemplateDto> getEmailTemplate(@PathVariable UUID documentId) {
        try {
            // Get the document
            Optional<DocumentDto> documentOpt = documentService.getDocumentById(documentId);
            if (documentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            DocumentDto document = documentOpt.get();
            
            // Get the template by name to access email configuration
            Optional<DocumentTemplateDto> templateOpt = documentTemplateService.getTemplateByName(document.getTemplateName());
            
            EmailTemplateDto emailTemplate = new EmailTemplateDto();
            
            if (templateOpt.isPresent()) {
                DocumentTemplateDto template = templateOpt.get();
                
                // Use template configuration if available
                emailTemplate.setEmailEnabled(template.getEmailEnabled() != null ? template.getEmailEnabled() : true);
                
                // Process subject with variables
                String subject = template.getDefaultEmailSubject();
                if (subject == null || subject.isEmpty()) {
                    subject = "Document généré - " + template.getDisplayName();
                } else {
                    // Replace variables in subject
                    subject = processEmailVariables(subject, document);
                }
                emailTemplate.setDefaultSubject(subject);
                
                // Process body with variables
                String body = template.getDefaultEmailBody();
                if (body == null || body.isEmpty()) {
                    body = "Bonjour,\n\nVeuillez trouver ci-joint le document généré.\n\nCordialement";
                } else {
                    // Replace variables in body
                    body = processEmailVariables(body, document);
                }
                emailTemplate.setDefaultBody(body);
                
                // Parse recipients from JSON
                try {
                    if (template.getDefaultRecipients() != null && !template.getDefaultRecipients().isEmpty()) {
                        List<String> recipients = objectMapper.readValue(template.getDefaultRecipients(), 
                                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                        emailTemplate.setDefaultRecipients(recipients);
                    } else {
                        emailTemplate.setDefaultRecipients(List.of());
                    }
                } catch (Exception e) {
                    log.warn("Error parsing default recipients JSON: {}", template.getDefaultRecipients(), e);
                    emailTemplate.setDefaultRecipients(List.of());
                }
                
                // Parse CC recipients from JSON
                try {
                    if (template.getDefaultCcRecipients() != null && !template.getDefaultCcRecipients().isEmpty()) {
                        List<String> ccRecipients = objectMapper.readValue(template.getDefaultCcRecipients(), 
                                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                        emailTemplate.setDefaultCcRecipients(ccRecipients);
                    } else {
                        emailTemplate.setDefaultCcRecipients(List.of());
                    }
                } catch (Exception e) {
                    log.warn("Error parsing default CC recipients JSON: {}", template.getDefaultCcRecipients(), e);
                    emailTemplate.setDefaultCcRecipients(List.of());
                }
            } else {
                // Fallback if template not found
                emailTemplate.setEmailEnabled(true);
                emailTemplate.setDefaultSubject("Document généré - " + document.getTemplateName());
                emailTemplate.setDefaultBody("Bonjour,\n\nVeuillez trouver ci-joint le document généré.\n\nCordialement");
                emailTemplate.setDefaultRecipients(List.of());
                emailTemplate.setDefaultCcRecipients(List.of());
            }
            
            return ResponseEntity.ok(emailTemplate);
            
        } catch (Exception e) {
            log.error("Error getting email template for document: {}", documentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/send-email")
    public ResponseEntity<SendEmailResponse> sendDocumentEmail(@RequestBody SendEmailRequest request) {
        try {
            log.info("Sending email for document: {}", request.getDocumentGenerationId());
            
            // Validate request
            if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
                SendEmailResponse errorResponse = new SendEmailResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Au moins un destinataire est requis");
                return ResponseEntity.status(400).body(errorResponse);
            }
            
            // Get the document
            Optional<DocumentDto> documentOpt = documentService.getDocumentById(request.getDocumentGenerationId());
            if (documentOpt.isEmpty()) {
                SendEmailResponse errorResponse = new SendEmailResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Document not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            DocumentDto document = documentOpt.get();
            
            // Check if document is completed
            if (document.getStatus() != Document.GenerationStatus.COMPLETED) {
                SendEmailResponse errorResponse = new SendEmailResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Le document n'est pas encore prêt");
                return ResponseEntity.status(400).body(errorResponse);
            }
            
            // Prepare attachments
            List<java.io.File> attachments = new java.util.ArrayList<>();
            
            if (request.isIncludePdf() && document.getPdfFilePath() != null) {
                java.io.File pdfFile = new java.io.File(document.getPdfFilePath());
                if (pdfFile.exists()) {
                    attachments.add(pdfFile);
                }
            }
            
            if (request.isIncludeDocx() && document.getGeneratedFilePath() != null) {
                java.io.File docxFile = new java.io.File(document.getGeneratedFilePath());
                if (docxFile.exists()) {
                    attachments.add(docxFile);
                }
            }
            
            // If no specific attachments requested, default to PDF
            if (attachments.isEmpty() && document.getPdfFilePath() != null) {
                java.io.File pdfFile = new java.io.File(document.getPdfFilePath());
                if (pdfFile.exists()) {
                    attachments.add(pdfFile);
                }
            }
            
            // Send email
            SendEmailResponse response = emailService.sendDocumentEmail(request, attachments);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error sending email for document: {}", request.getDocumentGenerationId(), e);
            
            SendEmailResponse errorResponse = new SendEmailResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Erreur lors de l'envoi de l'email: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Process email template variables
     */
    private String processEmailVariables(String template, DocumentDto document) {
        if (template == null) {
            return "";
        }
        
        String processed = template;
        
        // Replace common variables
        processed = processed.replace("{templateDisplayName}", 
            document.getTemplateName() != null ? document.getTemplateName() : "");
        processed = processed.replace("{documentName}", 
            document.getGeneratedFileName() != null ? document.getGeneratedFileName() : "");
        processed = processed.replace("{companyName}", 
            document.getCompanyName() != null ? document.getCompanyName() : "");
        processed = processed.replace("{generatedByName}", 
            document.getGeneratedByName() != null ? document.getGeneratedByName() : "");
        
        // Format creation date
        if (document.getCreatedAt() != null) {
            processed = processed.replace("{generationDate}", 
                document.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        
        return processed;
    }
    
    /**
     * Get current user ID from security context
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.debug("Getting user ID for username: {}", username);
        
        // Get the user by username and return their ID
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        return user.getId();
    }
}
