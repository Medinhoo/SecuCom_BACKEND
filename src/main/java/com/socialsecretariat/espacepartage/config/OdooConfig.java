package com.socialsecretariat.espacepartage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "odoo")
@Getter
@Setter
public class OdooConfig {
    
    private String url;
    private String database;
    private String username;
    private String password;
}
