package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTemplateDto {
    private UUID id;
    private String name;
    private String displayName;
    private String description;
    private String fileName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TemplateVariableDto> variables;
    
    // Email configuration fields
    private Boolean emailEnabled;
    private String defaultEmailSubject;
    private String defaultEmailBody;
    private String defaultRecipients;
    private String defaultCcRecipients;
}
