package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TDocumentTemplate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String displayName;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private String mappingConfigPath;
    
    @Column(nullable = false)
    private boolean active = true;
    
    // Email configuration fields
    @Column(name = "email_enabled")
    private Boolean emailEnabled = true;
    
    @Column(name = "default_email_subject")
    private String defaultEmailSubject;
    
    @Column(name = "default_email_body", columnDefinition = "TEXT")
    private String defaultEmailBody;
    
    @Column(name = "default_recipients", columnDefinition = "TEXT")
    private String defaultRecipients; // JSON array of recipient types
    
    @Column(name = "default_cc_recipients", columnDefinition = "TEXT")
    private String defaultCcRecipients; // JSON array of CC recipient types
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
