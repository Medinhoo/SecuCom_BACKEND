package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentGenerationRequestDto {
    private UUID templateId;
    private UUID companyId;
    private UUID collaboratorId;
    private Map<String, String> manualFields;
}
