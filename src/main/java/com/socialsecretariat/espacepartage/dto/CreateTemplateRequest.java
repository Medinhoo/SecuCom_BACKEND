package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateRequest {
    private String templateName;
    private String displayName;
    private String description;
    private MultipartFile docxFile;
    private List<VariableMapping> mappings;
}
