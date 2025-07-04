package com.socialsecretariat.espacepartage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.socialsecretariat.espacepartage.dto.CreateTemplateRequest;
import com.socialsecretariat.espacepartage.dto.DocumentTemplateDto;
import com.socialsecretariat.espacepartage.dto.TemplateVariableDto;
import com.socialsecretariat.espacepartage.dto.VariableMapping;
import com.socialsecretariat.espacepartage.model.DocumentTemplate;
import com.socialsecretariat.espacepartage.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
    
    @Value("${app.templates.base-path:src/main/resources}")
    private String templatesBasePath;
    
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
    
    @Transactional
    public DocumentTemplateDto createTemplateFromMapping(CreateTemplateRequest request) throws IOException {
        // Vérifier que le nom n'existe pas déjà
        if (documentTemplateRepository.findByNameAndActiveTrue(request.getTemplateName()).isPresent()) {
            throw new IllegalArgumentException("Un template avec ce nom existe déjà: " + request.getTemplateName());
        }
        
        // Générer les noms de fichiers
        String docxFileName = request.getTemplateName() + ".docx";
        String jsonFileName = request.getTemplateName() + ".json";
        
        // Chemins relatifs pour la base de données
        String docxFilePath = docxFileName;
        String jsonConfigPath = "templates/mappings/" + jsonFileName;
        
        // Chemins absolus pour la sauvegarde
        Path docxAbsolutePath = Paths.get(templatesBasePath, docxFileName);
        Path jsonAbsolutePath = Paths.get(templatesBasePath, "templates", "mappings", jsonFileName);
        
        try {
            // Créer les répertoires si nécessaire
            Files.createDirectories(docxAbsolutePath.getParent());
            Files.createDirectories(jsonAbsolutePath.getParent());
            
            // Sauvegarder le fichier DOCX
            try (FileOutputStream docxOut = new FileOutputStream(docxAbsolutePath.toFile())) {
                request.getDocxFile().getInputStream().transferTo(docxOut);
            }
            
            // Générer et sauvegarder le fichier JSON de configuration
            String jsonConfig = generateJsonConfig(request);
            Files.writeString(jsonAbsolutePath, jsonConfig);
            
            // Créer l'entité DocumentTemplate
            DocumentTemplate template = new DocumentTemplate();
            template.setName(request.getTemplateName());
            template.setDisplayName(request.getDisplayName());
            template.setDescription(request.getDescription());
            template.setFileName(docxFileName);
            template.setFilePath(docxFilePath);
            template.setMappingConfigPath(jsonConfigPath);
            template.setActive(true);
            
            template = documentTemplateRepository.save(template);
            
            log.info("Template créé avec succès: {} (ID: {})", template.getName(), template.getId());
            
            return convertToDto(template);
            
        } catch (Exception e) {
            // Nettoyer les fichiers en cas d'erreur
            try {
                Files.deleteIfExists(docxAbsolutePath);
                Files.deleteIfExists(jsonAbsolutePath);
            } catch (IOException cleanupException) {
                log.warn("Erreur lors du nettoyage des fichiers après échec de création du template", cleanupException);
            }
            throw new IOException("Erreur lors de la création du template: " + e.getMessage(), e);
        }
    }
    
    public boolean isTemplateNameAvailable(String templateName) {
        return documentTemplateRepository.findByNameAndActiveTrue(templateName).isEmpty();
    }
    
    private String generateJsonConfig(CreateTemplateRequest request) throws IOException {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("templateName", request.getTemplateName());
        rootNode.put("displayName", request.getDisplayName());
        rootNode.put("description", request.getDescription() != null ? request.getDescription() : "");
        
        ObjectNode variablesNode = objectMapper.createObjectNode();
        
        for (VariableMapping mapping : request.getMappings()) {
            ObjectNode variableNode = objectMapper.createObjectNode();
            variableNode.put("displayName", mapping.getDisplayName());
            variableNode.put("entity", mapping.getEntity());
            
            if (mapping.getField() != null) {
                variableNode.put("field", mapping.getField());
            } else {
                variableNode.putNull("field");
            }
            
            variableNode.put("type", mapping.getType());
            variableNode.put("required", mapping.isRequired());
            variableNode.put("description", mapping.getDescription() != null ? mapping.getDescription() : "");
            
            variablesNode.set(mapping.getVariableName(), variableNode);
        }
        
        rootNode.set("variables", variablesNode);
        
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
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
