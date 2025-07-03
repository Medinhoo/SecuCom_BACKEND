package com.socialsecretariat.espacepartage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "TDocumentGeneration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentGeneration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private DocumentTemplate template;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaborator_id")
    private Collaborator collaborator;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;
    
    @Column
    private String generatedFileName;
    
    @Column
    private String generatedFilePath;
    
    @Column
    private String pdfFilePath;
    
    @ElementCollection
    @CollectionTable(name = "document_generation_form_data", 
                    joinColumns = @JoinColumn(name = "generation_id"))
    @MapKeyColumn(name = "field_name")
    @Column(name = "field_value", columnDefinition = "TEXT")
    private Map<String, String> formData;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenerationStatus status = GenerationStatus.PENDING;
    
    @Column
    private String errorMessage;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum GenerationStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
