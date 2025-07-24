package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.ContratDto;
import com.socialsecretariat.espacepartage.dto.DocumentDto;
import com.socialsecretariat.espacepartage.dto.DocumentGenerationRequestDto;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final DocumentTemplateRepository documentTemplateRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final DimonaRepository dimonaRepository;
    private final UserRepository userRepository;
    private final DocumentTemplateService documentTemplateService;
    
    @Value("${app.documents.output-path:src/main/resources/generated-documents}")
    private String outputPath;
    
    public List<DocumentDto> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ContratDto> getAllContrats() {
        return documentRepository.findAll().stream()
                .filter(document -> document instanceof Contrat)
                .map(document -> convertToContratDto((Contrat) document))
                .collect(Collectors.toList());
    }
    
    public Optional<DocumentDto> getDocumentById(UUID id) {
        return documentRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public void deleteDocument(UUID id) {
        documentRepository.deleteById(id);
    }
    
    public void terminerContrat(UUID contratId) {
        Optional<Document> documentOpt = documentRepository.findById(contratId);
        if (documentOpt.isEmpty()) {
            throw new IllegalArgumentException("Contrat not found");
        }
        
        Document document = documentOpt.get();
        if (!(document instanceof Contrat)) {
            throw new IllegalArgumentException("Document is not a contract");
        }
        
        Contrat contrat = (Contrat) document;
        
        // Vérifier si une dimona OUT existe déjà
        if (contrat.getDimonaOut() != null) {
            throw new IllegalStateException("Ce contrat a déjà une dimona OUT");
        }
        
        // Créer une dimona OUT
        Dimona dimonaOut = new Dimona();
        dimonaOut.setType("OUT");
        dimonaOut.setExitDate(new Date());
        dimonaOut.setStatus(Dimona.Status.TO_CONFIRM);
        dimonaOut.setCollaborator(contrat.getCollaborator());
        dimonaOut.setCompany(contrat.getCompany());
        dimonaOut.setContrat(contrat);
        dimonaOut = dimonaRepository.save(dimonaOut);
        
        // Mettre à jour le contrat
        contrat.setDimonaOut(dimonaOut);
        contrat.setContratStatus(Contrat.ContratStatus.TERMINE);
        contrat.setEndDate(LocalDate.now());
        
        documentRepository.save(contrat);
    }
    
    public void activerContrat(UUID contratId) {
        Optional<Document> documentOpt = documentRepository.findById(contratId);
        if (documentOpt.isEmpty()) {
            throw new IllegalArgumentException("Contrat not found");
        }
        
        Document document = documentOpt.get();
        if (!(document instanceof Contrat)) {
            throw new IllegalArgumentException("Document is not a contract");
        }
        
        Contrat contrat = (Contrat) document;
        
        // Vérifier si le contrat peut être réactivé
        if (contrat.getContratStatus() == Contrat.ContratStatus.ACTIF) {
            throw new IllegalStateException("Ce contrat est déjà actif");
        }
        
        // Réactiver le contrat
        contrat.setContratStatus(Contrat.ContratStatus.ACTIF);
        contrat.setEndDate(null); // Supprimer la date de fin
        
        documentRepository.save(contrat);
    }
    
    // Méthode principale de génération de documents
    @Transactional
    public Object generateDocument(DocumentGenerationRequestDto request, UUID userId) {
        try {
            // Validate request
            DocumentTemplate template = documentTemplateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException("Template not found"));
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            Collaborator collaborator = null;
            if (request.getCollaboratorId() != null) {
                collaborator = collaboratorRepository.findById(request.getCollaboratorId())
                        .orElseThrow(() -> new IllegalArgumentException("Collaborator not found"));
            }
            
            // Générer selon le type de template
            if (template.getDocumentType() == DocumentTemplate.DocumentType.CONTRAT) {
                return generateContrat(template, user, collaborator, request.getManualFields());
            } else {
                return generateGenericDocument(template, user, collaborator, request.getManualFields());
            }
            
        } catch (Exception e) {
            log.error("Error in document generation process", e);
            throw new RuntimeException("Failed to generate document: " + e.getMessage(), e);
        }
    }
    
    private ContratDto generateContrat(DocumentTemplate template, User user, Collaborator collaborator, 
                                     Map<String, String> manualFields) {
        if (collaborator == null) {
            throw new IllegalArgumentException("Collaborator is required for contract generation");
        }
        
        try {
            // Create Dimona IN for the contract
            Dimona dimonaIn = new Dimona();
            dimonaIn.setType("IN");
            dimonaIn.setEntryDate(new Date());
            dimonaIn.setStatus(Dimona.Status.TO_CONFIRM);
            dimonaIn.setCollaborator(collaborator);
            dimonaIn.setCompany(collaborator.getCompany());
            dimonaIn = dimonaRepository.save(dimonaIn);
            
            // Create contract
            Contrat contrat = new Contrat();
            contrat.setTemplate(template);
            contrat.setCompany(collaborator.getCompany());
            contrat.setCollaborator(collaborator);
            contrat.setGeneratedBy(user);
            contrat.setFormData(manualFields);
            contrat.setStatus(Document.GenerationStatus.PROCESSING);
            contrat.setContratStatus(Contrat.ContratStatus.ACTIF);
            contrat.setStartDate(LocalDate.now());
            contrat.setDimonaIn(dimonaIn);
            
            contrat = (Contrat) documentRepository.save(contrat);
            
            // Link dimona to contract
            dimonaIn.setContrat(contrat);
            dimonaRepository.save(dimonaIn);
            
            try {
                // Generate document file
                String generatedFileName = generateDocumentFile(template, collaborator.getCompany(), 
                                                              collaborator, manualFields, contrat.getId());
                
                // Convert to PDF
                String pdfFileName = convertToPdf(generatedFileName);
                
                // Update contract record
                contrat.setGeneratedFileName(generatedFileName);
                contrat.setGeneratedFilePath(Paths.get(outputPath, generatedFileName).toString());
                
                if (pdfFileName.endsWith(".pdf") && Files.exists(Paths.get(outputPath, pdfFileName))) {
                    contrat.setPdfFilePath(Paths.get(outputPath, pdfFileName).toString());
                    log.debug("PDF file successfully created and stored at: {}", contrat.getPdfFilePath());
                } else {
                    throw new RuntimeException("PDF conversion failed. LibreOffice may not be installed or accessible.");
                }
                
                contrat.setStatus(Document.GenerationStatus.COMPLETED);
                contrat = (Contrat) documentRepository.save(contrat);
                
                return convertToContratDto(contrat);
                
            } catch (Exception e) {
                log.error("Error generating contract document", e);
                contrat.setStatus(Document.GenerationStatus.FAILED);
                contrat.setErrorMessage(e.getMessage());
                documentRepository.save(contrat);
                throw e;
            }
            
        } catch (Exception e) {
            log.error("Error in contract generation process", e);
            throw new RuntimeException("Failed to generate contract: " + e.getMessage(), e);
        }
    }
    
    private DocumentDto generateGenericDocument(DocumentTemplate template, User user, Collaborator collaborator, 
                                              Map<String, String> manualFields) {
        // TODO: Implémenter la génération de documents génériques
        // Pour l'instant, on retourne une exception
        throw new UnsupportedOperationException("Generic document generation not yet implemented");
    }
    
    private DocumentDto convertToDto(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setTemplateId(document.getTemplate() != null ? document.getTemplate().getId() : null);
        dto.setTemplateName(document.getTemplate() != null ? document.getTemplate().getName() : null);
        dto.setCompanyId(document.getCompany() != null ? document.getCompany().getId() : null);
        dto.setCompanyName(document.getCompany() != null ? document.getCompany().getName() : null);
        dto.setGeneratedById(document.getGeneratedBy() != null ? document.getGeneratedBy().getId() : null);
        dto.setGeneratedByName(document.getGeneratedBy() != null ? 
            document.getGeneratedBy().getFirstName() + " " + document.getGeneratedBy().getLastName() : null);
        dto.setGeneratedFileName(document.getGeneratedFileName());
        dto.setGeneratedFilePath(document.getGeneratedFilePath());
        dto.setPdfFilePath(document.getPdfFilePath());
        dto.setFormData(document.getFormData());
        dto.setStatus(document.getStatus());
        dto.setErrorMessage(document.getErrorMessage());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setDocumentType(document.getClass().getSimpleName());
        return dto;
    }
    
    private ContratDto convertToContratDto(Contrat contrat) {
        ContratDto dto = new ContratDto();
        
        // Champs hérités de Document
        dto.setId(contrat.getId());
        dto.setTemplateId(contrat.getTemplate() != null ? contrat.getTemplate().getId() : null);
        dto.setTemplateName(contrat.getTemplate() != null ? contrat.getTemplate().getName() : null);
        dto.setCompanyId(contrat.getCompany() != null ? contrat.getCompany().getId() : null);
        dto.setCompanyName(contrat.getCompany() != null ? contrat.getCompany().getName() : null);
        dto.setGeneratedById(contrat.getGeneratedBy() != null ? contrat.getGeneratedBy().getId() : null);
        dto.setGeneratedByName(contrat.getGeneratedBy() != null ? 
            contrat.getGeneratedBy().getFirstName() + " " + contrat.getGeneratedBy().getLastName() : null);
        dto.setGeneratedFileName(contrat.getGeneratedFileName());
        dto.setGeneratedFilePath(contrat.getGeneratedFilePath());
        dto.setPdfFilePath(contrat.getPdfFilePath());
        dto.setFormData(contrat.getFormData());
        dto.setStatus(contrat.getStatus());
        dto.setErrorMessage(contrat.getErrorMessage());
        dto.setCreatedAt(contrat.getCreatedAt());
        
        // Champs spécifiques au Contrat
        dto.setCollaboratorId(contrat.getCollaborator() != null ? contrat.getCollaborator().getId() : null);
        dto.setCollaboratorFirstName(contrat.getCollaborator() != null ? contrat.getCollaborator().getFirstName() : null);
        dto.setCollaboratorLastName(contrat.getCollaborator() != null ? contrat.getCollaborator().getLastName() : null);
        dto.setDimonaInId(contrat.getDimonaIn() != null ? contrat.getDimonaIn().getId() : null);
        dto.setDimonaOutId(contrat.getDimonaOut() != null ? contrat.getDimonaOut().getId() : null);
        dto.setContratStatus(contrat.getContratStatus());
        dto.setStartDate(contrat.getStartDate());
        dto.setEndDate(contrat.getEndDate());
        
        return dto;
    }
    
    private String generateDocumentFile(DocumentTemplate template, Company company, Collaborator collaborator, 
                                      Map<String, String> manualFields, UUID documentId) throws IOException {
        
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
        String generatedFileName = template.getName() + "_" + documentId + "_" + timestamp + ".docx";
        
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
}
