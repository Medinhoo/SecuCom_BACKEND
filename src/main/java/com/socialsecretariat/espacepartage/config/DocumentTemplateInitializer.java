package com.socialsecretariat.espacepartage.config;

import com.socialsecretariat.espacepartage.model.DocumentTemplate;
import com.socialsecretariat.espacepartage.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentTemplateInitializer implements CommandLineRunner {
    
    private final DocumentTemplateRepository documentTemplateRepository;
    
    @Override
    public void run(String... args) throws Exception {
        initializeTemplates();
    }
    
    private void initializeTemplates() {
        // Check if CNT_Employe template already exists
        if (documentTemplateRepository.findByName("CNT_Employe").isEmpty()) {
            DocumentTemplate cntEmployeTemplate = new DocumentTemplate();
            cntEmployeTemplate.setName("CNT_Employe");
            cntEmployeTemplate.setDisplayName("Contrat de travail employé");
            cntEmployeTemplate.setDescription("Template pour générer un contrat de travail pour un employé");
            cntEmployeTemplate.setFileName("CNT_Employe.docx");
            cntEmployeTemplate.setFilePath("CNT_Employe.docx");
            cntEmployeTemplate.setMappingConfigPath("templates/mappings/CNT_Employe.json");
            cntEmployeTemplate.setActive(true);
            
            documentTemplateRepository.save(cntEmployeTemplate);
            log.info("Initialized CNT_Employe document template");
        } else {
            log.info("CNT_Employe template already exists, skipping initialization");
        }
    }
}
