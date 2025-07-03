package com.socialsecretariat.espacepartage.repository;

import com.socialsecretariat.espacepartage.model.DocumentGeneration;
import com.socialsecretariat.espacepartage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentGenerationRepository extends JpaRepository<DocumentGeneration, UUID> {
    
    List<DocumentGeneration> findByGeneratedByOrderByCreatedAtDesc(User generatedBy);
    
    List<DocumentGeneration> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
    
    List<DocumentGeneration> findByCollaboratorIdOrderByCreatedAtDesc(UUID collaboratorId);
    
    @Query("SELECT dg FROM DocumentGeneration dg WHERE dg.template.id = :templateId ORDER BY dg.createdAt DESC")
    List<DocumentGeneration> findByTemplateIdOrderByCreatedAtDesc(@Param("templateId") UUID templateId);
    
    @Query("SELECT dg FROM DocumentGeneration dg WHERE dg.generatedBy.id = :userId ORDER BY dg.createdAt DESC")
    List<DocumentGeneration> findByGeneratedByIdOrderByCreatedAtDesc(@Param("userId") UUID userId);
    
    List<DocumentGeneration> findAllByOrderByCreatedAtDesc();
}
