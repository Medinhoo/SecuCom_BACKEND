package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.TemplateAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateAnalysisService {
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    public TemplateAnalysisResult extractVariablesFromDocx(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".docx")) {
            throw new IllegalArgumentException("Le fichier doit être au format DOCX");
        }
        
        Set<String> variables = new HashSet<>();
        
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            // Extraire les variables des paragraphes
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null) {
                    extractVariablesFromText(text, variables);
                }
            }
            
            // TODO: Ajouter l'extraction des variables dans les tableaux, headers, footers si nécessaire
            // Pour l'instant, on se concentre sur les paragraphes principaux
            
        } catch (IOException e) {
            log.error("Erreur lors de l'analyse du fichier DOCX: {}", file.getOriginalFilename(), e);
            throw new IOException("Impossible d'analyser le fichier DOCX: " + e.getMessage(), e);
        }
        
        List<String> variablesList = new ArrayList<>(variables);
        variablesList.sort(String::compareToIgnoreCase);
        
        log.info("Variables extraites du fichier {}: {}", file.getOriginalFilename(), variablesList);
        
        return new TemplateAnalysisResult(variablesList, file.getOriginalFilename());
    }
    
    private void extractVariablesFromText(String text, Set<String> variables) {
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            if (!variable.isEmpty()) {
                variables.add(variable);
            }
        }
    }
    
    public boolean isValidDocxFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        
        return filename.toLowerCase().endsWith(".docx");
    }
}
