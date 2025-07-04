package com.socialsecretariat.espacepartage.controller;

import com.socialsecretariat.espacepartage.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailTestController {
    
    private final EmailService emailService;
    
    @GetMapping("/test-config")
    public ResponseEntity<Map<String, Object>> testEmailConfiguration() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isConfigured = emailService.isEmailConfigured();
            response.put("success", true);
            response.put("configured", isConfigured);
            response.put("message", isConfigured ? 
                "Configuration SMTP valide" : 
                "Problème de configuration SMTP");
            
            log.info("Test de configuration SMTP: {}", isConfigured ? "OK" : "ERREUR");
            
        } catch (Exception e) {
            log.error("Erreur lors du test de configuration SMTP", e);
            response.put("success", false);
            response.put("configured", false);
            response.put("message", "Erreur: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
