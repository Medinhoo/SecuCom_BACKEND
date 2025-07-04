package com.socialsecretariat.espacepartage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialsecretariat.espacepartage.dto.*;
import com.socialsecretariat.espacepartage.model.DocumentGeneration;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.service.DocumentGenerationService;
import com.socialsecretariat.espacepartage.service.DocumentTemplateService;
import com.socialsecretariat.espacepartage.service.EntityMetadataService;
import com.socialsecretariat.espacepartage.service.TemplateAnalysisService;
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
import com.socialsecretariat.espacepartage.service.UserService;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
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
public class DocumentController {
    
    private final DocumentTemplateService documentTemplateService;
    private final DocumentGenerationService documentGenerationService;
    private final UserService userService;
    private final TemplateAnalysisService templateAnalysisService;
    private final EntityMetadataService entityMetadataService;
    private final ObjectMapper objectMapper;

    
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
    
    @PostMapping("/generate")
    public ResponseEntity<DocumentGenerationResponseDto> generateDocument(
            @RequestBody DocumentGenerationRequestDto request) {
        
        UUID userId = getCurrentUserId();
        DocumentGenerationResponseDto response = documentGenerationService.generateDocument(request, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/generations")
    public ResponseEntity<List<DocumentGenerationResponseDto>> getAllGenerations() {
        List<DocumentGenerationResponseDto> generations = documentGenerationService.getAllGenerations();
        return ResponseEntity.ok(generations);
    }
    
    @GetMapping("/generations/{generationId}")
    public ResponseEntity<DocumentGenerationResponseDto> getGeneration(@PathVariable UUID generationId) {
        Optional<DocumentGenerationResponseDto> generation = documentGenerationService.getGenerationById(generationId);
        return generation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/generations/{generationId}/download-pdf")
    public ResponseEntity<Resource> downloadGeneratedPdf(@PathVariable UUID generationId) {
        try {
            log.debug("Attempting to download PDF for generation: {}", generationId);
            
            Optional<DocumentGenerationResponseDto> generationOpt = documentGenerationService.getGenerationById(generationId);
            
            if (generationOpt.isEmpty()) {
                log.warn("Generation not found: {}", generationId);
                return ResponseEntity.notFound().build();
            }
            
            DocumentGenerationResponseDto generation = generationOpt.get();
            log.debug("Found generation: {} with status: {}", generationId, generation.getStatus());
            
            // Check if generation is completed
            if (generation.getStatus() != DocumentGeneration.GenerationStatus.COMPLETED) {
                log.warn("Generation {} is not completed. Status: {}", generationId, generation.getStatus());
                return ResponseEntity.badRequest().build();
            }
            
            // For completed generations, PDF should always exist
            String pdfFilePath = generation.getPdfFilePath();
            log.debug("PDF file path from generation: {}", pdfFilePath);
            log.debug("DOCX file path from generation: {}", generation.getGeneratedFilePath());
            
            Path pdfPath = Paths.get(pdfFilePath);
            log.debug("Resolved PDF path: {}", pdfPath);
            
            if (!Files.exists(pdfPath)) {
                log.error("PDF file not found at path: {} for generation: {}. File may have been deleted.", pdfFilePath, generationId);
                return ResponseEntity.notFound().build();
            }
            
            // Verify the file is actually a PDF
            if (!pdfFilePath.toLowerCase().endsWith(".pdf")) {
                log.error("CRITICAL: pdfFilePath does not end with .pdf! Path: {}", pdfFilePath);
                log.error("This means the PDF conversion failed but the generation was marked as successful");
                return ResponseEntity.status(422).build();
            }
            
            log.debug("PDF file found at: {}", pdfPath);
            
            Resource resource = new FileSystemResource(pdfPath.toFile());
            log.debug("resource : {}", resource);
            String fileName = pdfPath.getFileName().toString();
            log.debug("fileName : {}", fileName);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header("Content-Transfer-Encoding", "binary")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading PDF for generation: {}", generationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/generations/{generationId}/download-docx")
    public ResponseEntity<Resource> downloadGeneratedDocx(@PathVariable UUID generationId) {
        try {
            log.debug("Attempting to download DOCX for generation: {}", generationId);
            
            Optional<DocumentGenerationResponseDto> generationOpt = documentGenerationService.getGenerationById(generationId);
            
            if (generationOpt.isEmpty()) {
                log.warn("Generation not found: {}", generationId);
                return ResponseEntity.notFound().build();
            }
            
            DocumentGenerationResponseDto generation = generationOpt.get();
            log.debug("Found generation: {} with status: {}", generationId, generation.getStatus());
            
            // Use the DOCX file path from the DTO
            String docxFilePath = generation.getGeneratedFilePath();
            
            if (docxFilePath == null || docxFilePath.isEmpty()) {
                log.warn("No DOCX file path stored for generation: {}", generationId);
                return ResponseEntity.notFound().build();
            }
            
            Path docxPath = Paths.get(docxFilePath);
            
            if (!Files.exists(docxPath)) {
                log.warn("DOCX file not found at path: {} for generation: {}", docxFilePath, generationId);
                return ResponseEntity.notFound().build();
            }
            
            log.debug("DOCX file found at: {}", docxPath);
            
            Resource resource = new FileSystemResource(docxPath.toFile());
            String fileName = docxPath.getFileName().toString();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading DOCX for generation: {}", generationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ===== NOUVEAUX ENDPOINTS POUR LA CRÉATION DE TEMPLATES =====
    
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
            @RequestParam("mappings") String mappingsJson) {
        
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
    
    // ===== FIN NOUVEAUX ENDPOINTS =====
    
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
