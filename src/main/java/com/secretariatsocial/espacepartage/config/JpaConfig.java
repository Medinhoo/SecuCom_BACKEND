package com.secretariatsocial.espacepartage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    // Ce bean est requis pour l'audit JPA, même si nous ne suivons pas qui a fait
    // les modifications
    @Bean
    public AuditorAware<String> auditorProvider() {
        // Retourne simplement un auditeur "system" par défaut
        // Dans une application réelle, vous pourriez récupérer l'utilisateur connecté
        return () -> Optional.of("system");
    }
}