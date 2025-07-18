package com.socialsecretariat.espacepartage.repository;

import com.socialsecretariat.espacepartage.model.CompanyConfirmationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyConfirmationHistoryRepository extends JpaRepository<CompanyConfirmationHistory, UUID> {
    
    /**
     * Find all confirmation history for a specific company, ordered by confirmation date descending
     */
    List<CompanyConfirmationHistory> findByCompanyIdOrderByConfirmedAtDesc(UUID companyId);
    
    /**
     * Find the latest confirmation for a specific company
     */
    CompanyConfirmationHistory findFirstByCompanyIdOrderByConfirmedAtDesc(UUID companyId);
}
