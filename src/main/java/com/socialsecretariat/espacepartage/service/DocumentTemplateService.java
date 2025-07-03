package com.socialsecretariat.espacepartage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialsecretariat.espacepartage.dto.DocumentTemplateDto;
import com.socialsecretariat.espacepartage.dto.TemplateVariableDto;
import com.socialsecretariat.espacepartage.model.DocumentTemplate;
import com.socialsecretariat.espacepartage.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentTemplateService {
    
    private final DocumentTemplateRepository documentTemplateRepository;
    private final ObjectMapper objectMapper;
    
    public List<DocumentTemplateDto> getAllActiveTemplates() {
        List<DocumentTemplate> templates = documentTemplateRepository.findByActiveTrue();
        return templates.stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public Optional<DocumentTemplateDto> getTemplateById(UUID id) {
        return documentTemplateRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public Optional<DocumentTemplateDto> getTemplateByName(String name) {
        return documentTemplateRepository.findByNameAndActiveTrue(name)
                .map(this::convertToDto);
    }
    
    public List<TemplateVariableDto> getTemplateVariables(UUID templateId) {
        Optional<DocumentTemplate> templateOpt = documentTemplateRepository.findById(templateId);
        if (templateOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        DocumentTemplate template = templateOpt.get();
        return loadVariablesFromConfig(template.getMappingConfigPath());
    }
    
    public List<TemplateVariableDto> getTemplateVariablesByName(String templateName) {
        Optional<DocumentTemplate> templateOpt = documentTemplateRepository.findByNameAndActiveTrue(templateName);
        if (templateOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        DocumentTemplate template = templateOpt.get();
        return loadVariablesFromConfig(template.getMappingConfigPath());
    }
    
    private List<TemplateVariableDto> loadVariablesFromConfig(String configPath) {
        List<TemplateVariableDto> variables = new ArrayList<>();
        
        try {
            ClassPathResource resource = new ClassPathResource(configPath);
            JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
            JsonNode variablesNode = rootNode.get("variables");
            
            if (variablesNode != null && variablesNode.isObject()) {
                variablesNode.fields().forEachRemaining(entry -> {
                    String variableName = entry.getKey();
                    JsonNode variableConfig = entry.getValue();
                    
                    TemplateVariableDto variable = new TemplateVariableDto();
                    variable.setName(variableName);
                    variable.setDisplayName(variableConfig.get("displayName").asText());
                    variable.setEntity(variableConfig.get("entity").asText());
                    
                    JsonNode fieldNode = variableConfig.get("field");
                    variable.setField(fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null);
                    
                    variable.setType(variableConfig.get("type").asText());
                    variable.setRequired(variableConfig.get("required").asBoolean());
                    variable.setDescription(variableConfig.get("description").asText());
                    
                    JsonNode defaultValueNode = variableConfig.get("defaultValue");
                    if (defaultValueNode != null && !defaultValueNode.isNull()) {
                        variable.setDefaultValue(defaultValueNode.asText());
                    }
                    
                    variables.add(variable);
                });
            }
        } catch (IOException e) {
            log.error("Error loading template variables from config: {}", configPath, e);
        }
        
        return variables;
    }
    
    private DocumentTemplateDto convertToDto(DocumentTemplate template) {
        DocumentTemplateDto dto = new DocumentTemplateDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setDisplayName(template.getDisplayName());
        dto.setDescription(template.getDescription());
        dto.setFileName(template.getFileName());
        dto.setActive(template.isActive());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        
        // Load variables
        dto.setVariables(loadVariablesFromConfig(template.getMappingConfigPath()));
        
        return dto;
    }
}
