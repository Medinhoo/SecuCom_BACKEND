package com.socialsecretariat.espacepartage.controller;

import com.socialsecretariat.espacepartage.dto.DocumentGenerationRequestDto;
import com.socialsecretariat.espacepartage.dto.DocumentGenerationResponseDto;
import com.socialsecretariat.espacepartage.dto.DocumentTemplateDto;
import com.socialsecretariat.espacepartage.dto.TemplateVariableDto;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.service.DocumentGenerationService;
import com.socialsecretariat.espacepartage.service.DocumentTemplateService;
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


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
            Optional<DocumentGenerationResponseDto> generationOpt = documentGenerationService.getGenerationById(generationId);
            
            if (generationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            DocumentGenerationResponseDto generation = generationOpt.get();
            
            // Get PDF file path from generation record
            String pdfFileName = generation.getGeneratedFileName().replace(".docx", ".pdf");
            Path pdfPath = Paths.get("src/main/resources/generated-documents", pdfFileName);
            
            if (!Files.exists(pdfPath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(pdfPath.toFile());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfFileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading PDF for generation: {}", generationId, e);
            return ResponseEntity.internalServerError().build();
        }
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
