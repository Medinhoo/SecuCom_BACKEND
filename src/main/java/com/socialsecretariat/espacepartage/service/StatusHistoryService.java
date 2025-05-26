package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.StatusHistoryDto;
import com.socialsecretariat.espacepartage.model.StatusHistory;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.StatusHistoryRepository;
import com.socialsecretariat.espacepartage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatusHistoryService {

    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;

    /**
     * Record a status change for a Dimona
     */
    public StatusHistoryDto recordDimonaStatusChange(UUID dimonaId, String previousStatus, 
                                                   String newStatus, String changeReason, 
                                                   User changedBy) {
        log.debug("Recording status change for Dimona {}: {} -> {}", 
                 dimonaId, previousStatus, newStatus);

        StatusHistory statusHistory = new StatusHistory(
                dimonaId, previousStatus, newStatus, changeReason, changedBy
        );

        StatusHistory savedHistory = statusHistoryRepository.save(statusHistory);
        log.info("Status change recorded with ID: {} for Dimona: {}", savedHistory.getId(), dimonaId);

        return convertToDto(savedHistory);
    }

    /**
     * Get status history for a specific Dimona
     */
    @Transactional(readOnly = true)
    public List<StatusHistoryDto> getDimonaStatusHistory(UUID dimonaId) {
        log.debug("Fetching status history for Dimona: {}", dimonaId);
        
        List<StatusHistory> history = statusHistoryRepository.findByDimonaIdOrderByChangedAtDesc(dimonaId);
        return history.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get the latest status change for a Dimona
     */
    @Transactional(readOnly = true)
    public StatusHistoryDto getLatestDimonaStatusChange(UUID dimonaId) {
        log.debug("Fetching latest status change for Dimona: {}", dimonaId);
        
        StatusHistory latestChange = statusHistoryRepository.findLatestByDimonaId(dimonaId);
        return latestChange != null ? convertToDto(latestChange) : null;
    }

    /**
     * Count status changes for a Dimona
     */
    @Transactional(readOnly = true)
    public long countDimonaStatusChanges(UUID dimonaId) {
        return statusHistoryRepository.countByDimonaId(dimonaId);
    }

    /**
     * Convert StatusHistory entity to DTO
     */
    private StatusHistoryDto convertToDto(StatusHistory statusHistory) {
        StatusHistoryDto dto = new StatusHistoryDto();
        dto.setId(statusHistory.getId());
        dto.setDimonaId(statusHistory.getDimonaId());
        dto.setPreviousStatus(statusHistory.getPreviousStatus());
        dto.setNewStatus(statusHistory.getNewStatus());
        dto.setChangeReason(statusHistory.getChangeReason());
        dto.setChangedByUserId(statusHistory.getChangedBy().getId());
        dto.setChangedByUserName(statusHistory.getChangedBy().getFirstName() + " " + 
                                statusHistory.getChangedBy().getLastName());
        dto.setChangedAt(statusHistory.getChangedAt());
        dto.setChangeDescription(statusHistory.getChangeDescription());
        dto.setStatusCreation(statusHistory.isStatusCreation());
        return dto;
    }
}
