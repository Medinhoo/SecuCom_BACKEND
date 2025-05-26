package com.socialsecretariat.espacepartage.repository;

import com.socialsecretariat.espacepartage.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, UUID> {

    /**
     * Find all status history for a specific Dimona
     */
    List<StatusHistory> findByDimonaIdOrderByChangedAtDesc(UUID dimonaId);

    /**
     * Find the latest status change for a Dimona
     */
    @Query("SELECT sh FROM StatusHistory sh WHERE sh.dimonaId = :dimonaId ORDER BY sh.changedAt DESC LIMIT 1")
    StatusHistory findLatestByDimonaId(@Param("dimonaId") UUID dimonaId);

    /**
     * Count status changes for a Dimona
     */
    long countByDimonaId(UUID dimonaId);
}
