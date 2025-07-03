package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.DocumentGenerationRequestDto;
import com.socialsecretariat.espacepartage.dto.DocumentGenerationResponseDto;
import com.socialsecretariat.espacepartage.dto.TemplateVariableDto;
import com.socialsecretariat.espacepartage.model.*;
import com.socialsecretariat.espacepartage.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentGenerationService {
    
    private final DocumentTemplateRepository documentTemplateRepository;
    private final DocumentGenerationRepository documentGenerationRepository;
    private final CompanyRepository companyRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final UserRepository userRepository;
    private final DocumentTemplateService documentTemplateService;
    
    @Value("${app.documents.output-path:src/main/resources/generated-documents}")
    private String outputPath;
    
    @Transactional
    public DocumentGenerationResponseDto generateDocument(DocumentGenerationRequestDto request, UUID userId) {
        try {
            // Validate request
            DocumentTemplate template = documentTemplateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found"));
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            Company company = null;
            if (request.getCompanyId() != null) {
                company = companyRepository.findById(request.getCompanyId())
                        .orElseThrow(() -> new IllegalArgumentException("Company not found"));
            }
            
            Collaborator collaborator = null;
            if (request.getCollaboratorId() != null) {
                collaborator = collaboratorRepository.findById(request.getCollaboratorId())
                        .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));
            }
            
            // Create generation record
            DocumentGeneration generation = new DocumentGeneration();
            generation.setTemplate(template);
            generation.setCompany(company);
            generation.setCollaborator(collaborator);
            generation.setGeneratedBy(user);
            generation.setFormData(request.getManualFields());
            generation.setStatus(DocumentGeneration.GenerationStatus.PROCESSING);
            
            generation = documentGenerationRepository.save(generation);
            
            try {
                // Generate document
                String generatedFileName = generateDocumentFile(template, company, collaborator, request.getManualFields(), generation.getId());
                
                // Convert to PDF
                String pdfFileName = convertToPdf(generatedFileName);
                
                // Update generation record
                generation.setGeneratedFileName(generatedFileName);
                generation.setGeneratedFilePath(Paths.get(outputPath, generatedFileName).toString());
                generation.setPdfFilePath(Paths.get(outputPath, pdfFileName).toString());
                generation.setStatus(DocumentGeneration.GenerationStatus.COMPLETED);
                
                generation = documentGenerationRepository.save(generation);
                
                return convertToResponseDto(generation);
                
            } catch (Exception e) {
                log.error("Error generating document", e);
                generation.setStatus(DocumentGeneration.GenerationStatus.FAILED);
                generation.setErrorMessage(e.getMessage());
                documentGenerationRepository.save(generation);
                throw e;
            }
            
        } catch (Exception e) {
            log.error("Error in document generation process", e);
            throw new RuntimeException("Failed to generate document: " + e.getMessage(), e);
        }
    }
    
    private String generateDocumentFile(DocumentTemplate template, Company company, Collaborator collaborator, 
                                      Map<String, String> manualFields, UUID generationId) throws IOException {
        
        // Load template file
        ClassPathResource templateResource = new ClassPathResource(template.getFilePath());
        
        // Load template variables configuration
        List<TemplateVariableDto> variables = documentTemplateService.getTemplateVariables(template.getId());
        
        // Prepare replacement data
        Map<String, String> replacementData = new HashMap<>();
        
        // Process each variable
        for (TemplateVariableDto variable : variables) {
            String value = null;
            
            if ("manual".equals(variable.getEntity())) {
                // Manual field
                value = manualFields.get(variable.getName());
            } else if ("Company".equals(variable.getEntity()) && company != null) {
                // Company field
                value = getFieldValue(company, variable.getField());
            } else if ("Collaborator".equals(variable.getEntity()) && collaborator != null) {
                // Collaborator field
                value = getFieldValue(collaborator, variable.getField());
            }
            
            // Handle null values
            if (value == null) {
                value = variable.getDefaultValue() != null ? variable.getDefaultValue().toString() : "";
            }
            
            replacementData.put("{{" + variable.getName() + "}}", value);
        }
        
        // Generate unique filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String generatedFileName = template.getName() + "_" + generationId + "_" + timestamp + ".docx";
        
        // Process DOCX template
        try (InputStream templateStream = templateResource.getInputStream();
             XWPFDocument document = new XWPFDocument(templateStream)) {
            
            // Replace variables in paragraphs
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replaceInParagraph(paragraph, replacementData);
            }
            
            // Create output directory if it doesn't exist
            Path outputDir = Paths.get(outputPath);
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            
            // Save generated document
            Path outputFile = outputDir.resolve(generatedFileName);
            try (FileOutputStream out = new FileOutputStream(outputFile.toFile())) {
                document.write(out);
            }
        }
        
        return generatedFileName;
    }
    
    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> replacementData) {
        String paragraphText = paragraph.getText();
        
        for (Map.Entry<String, String> entry : replacementData.entrySet()) {
            if (paragraphText.contains(entry.getKey())) {
                paragraphText = paragraphText.replace(entry.getKey(), entry.getValue());
            }
        }
        
        // Clear existing runs and create new one with replaced text
        if (!paragraphText.equals(paragraph.getText())) {
            // Store formatting from first run BEFORE removing it
            Boolean isBold = null;
            Boolean isItalic = null;
            String fontFamily = null;
            Integer fontSize = null;
            
            if (!paragraph.getRuns().isEmpty()) {
                XWPFRun firstRun = paragraph.getRuns().get(0);
                try {
                    isBold = firstRun.isBold();
                    isItalic = firstRun.isItalic();
                    fontFamily = firstRun.getFontFamily();
                    fontSize = firstRun.getFontSize();
                } catch (Exception e) {
                    log.debug("Could not retrieve formatting from first run: {}", e.getMessage());
                }
            }
            
            // Clear all runs
            for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }
            
            // Create new run with replaced text
            XWPFRun newRun = paragraph.createRun();
            newRun.setText(paragraphText);
            
            // Apply formatting if available
            try {
                if (isBold != null) {
                    newRun.setBold(isBold);
                }
                if (isItalic != null) {
                    newRun.setItalic(isItalic);
                }
                if (fontFamily != null) {
                    newRun.setFontFamily(fontFamily);
                }
                if (fontSize != null && fontSize > 0) {
                    newRun.setFontSize(fontSize);
                }
            } catch (Exception e) {
                log.debug("Could not apply formatting to new run: {}", e.getMessage());
            }
        }
    }
    
    private String getFieldValue(Object entity, String fieldPath) {
        try {
            if (fieldPath == null) return null;
            
            // Handle composite fields like "firstName lastName"
            if (fieldPath.contains(" ")) {
                String[] compositeFields = fieldPath.split(" ");
                StringBuilder result = new StringBuilder();
                for (String field : compositeFields) {
                    String value = getFieldValue(entity, field);
                    if (value != null && !value.isEmpty()) {
                        if (result.length() > 0) result.append(" ");
                        result.append(value);
                    }
                }
                return result.toString();
            }
            
            String[] parts = fieldPath.split("\\.");
            Object currentObject = entity;
            
            for (String part : parts) {
                if (currentObject == null) return null;
                
                Field field = currentObject.getClass().getDeclaredField(part);
                field.setAccessible(true);
                currentObject = field.get(currentObject);
            }
            
            if (currentObject == null) return null;
            
            // Format different types
            if (currentObject instanceof LocalDate) {
                return ((LocalDate) currentObject).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else if (currentObject instanceof BigDecimal) {
                return ((BigDecimal) currentObject).toString();
            } else {
                return currentObject.toString();
            }
            
        } catch (Exception e) {
            log.warn("Error getting field value for path: {} on entity: {}", fieldPath, entity.getClass().getSimpleName(), e);
            return null;
        }
    }
    
    private String convertToPdf(String docxFileName) throws IOException {
        String pdfFileName = docxFileName.replace(".docx", ".pdf");
        Path docxPath = Paths.get(outputPath, docxFileName);
        Path pdfPath = Paths.get(outputPath, pdfFileName);
        
        try {
            // Build LibreOffice command
            ProcessBuilder processBuilder = new ProcessBuilder(
                "soffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", outputPath,
                docxPath.toString()
            );
            
            // Set working directory
            processBuilder.directory(new File(outputPath));
            
            // Start the process
            Process process = processBuilder.start();
            
            // Wait for completion with timeout (30 seconds)
            boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("PDF conversion timed out after 30 seconds");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                // Read error output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String errorOutput = reader.lines().reduce("", (a, b) -> a + "\n" + b);
                    throw new IOException("LibreOffice conversion failed with exit code " + exitCode + ": " + errorOutput);
                }
            }
            
            // Verify PDF was created
            if (!Files.exists(pdfPath)) {
                throw new IOException("PDF file was not created: " + pdfPath);
            }
            
            log.info("Successfully converted {} to {}", docxFileName, pdfFileName);
            return pdfFileName;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("PDF conversion was interrupted", e);
        } catch (IOException e) {
            log.warn("PDF conversion failed for {}: {}. LibreOffice may not be installed or accessible.", docxFileName, e.getMessage());
            // Return the DOCX filename as fallback - the system will still work without PDF
            return docxFileName;
        }
    }
    
    public List<DocumentGenerationResponseDto> getGenerationHistory(UUID userId) {
        List<DocumentGeneration> generations = documentGenerationRepository.findByGeneratedByIdOrderByCreatedAtDesc(userId);
        return generations.stream()
                .map(this::convertToResponseDto)
                .toList();
    }
    
    public List<DocumentGenerationResponseDto> getAllGenerations() {
        List<DocumentGeneration> generations = documentGenerationRepository.findAllByOrderByCreatedAtDesc();
        return generations.stream()
                .map(this::convertToResponseDto)
                .toList();
    }
    
    public Optional<DocumentGenerationResponseDto> getGenerationById(UUID generationId) {
        return documentGenerationRepository.findById(generationId)
                .map(this::convertToResponseDto);
    }
    
    private DocumentGenerationResponseDto convertToResponseDto(DocumentGeneration generation) {
        DocumentGenerationResponseDto dto = new DocumentGenerationResponseDto();
        dto.setId(generation.getId());
        dto.setTemplateName(generation.getTemplate().getName());
        dto.setTemplateDisplayName(generation.getTemplate().getDisplayName());
        dto.setCompanyName(generation.getCompany() != null ? generation.getCompany().getName() : null);
        dto.setCollaboratorName(generation.getCollaborator() != null ? 
                generation.getCollaborator().getFirstName() + " " + generation.getCollaborator().getLastName() : null);
        dto.setGeneratedByName(generation.getGeneratedBy().getUsername());
        dto.setGeneratedFileName(generation.getGeneratedFileName());
        dto.setStatus(generation.getStatus());
        dto.setErrorMessage(generation.getErrorMessage());
        dto.setCreatedAt(generation.getCreatedAt());
        dto.setFormData(generation.getFormData());
        
        return dto;
    }
}
