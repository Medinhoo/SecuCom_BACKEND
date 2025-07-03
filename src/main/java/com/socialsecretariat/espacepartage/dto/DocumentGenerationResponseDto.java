package com.socialsecretariat.espacepartage.dto;

import com.socialsecretariat.espacepartage.model.DocumentGeneration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentGenerationResponseDto {
    private UUID id;
    private String templateName;
    private String templateDisplayName;
    private String companyName;
    private String collaboratorName;
    private String generatedByName;
    private String generatedFileName;
    private DocumentGeneration.GenerationStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private Map<String, String> formData;
}
