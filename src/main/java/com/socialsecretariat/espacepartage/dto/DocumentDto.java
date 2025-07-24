package com.socialsecretariat.espacepartage.dto;

import com.socialsecretariat.espacepartage.model.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    
    private UUID id;
    private UUID templateId;
    private String templateName;
    private UUID companyId;
    private String companyName;
    private UUID generatedById;
    private String generatedByName;
    private String generatedFileName;
    private String generatedFilePath;
    private String pdfFilePath;
    private Map<String, String> formData;
    private Document.GenerationStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private String documentType;
}
