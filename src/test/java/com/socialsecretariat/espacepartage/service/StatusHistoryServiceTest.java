package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.StatusHistoryDto;
import com.socialsecretariat.espacepartage.model.StatusHistory;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.StatusHistoryRepository;
import com.socialsecretariat.espacepartage.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusHistoryServiceTest {

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StatusHistoryService statusHistoryService;

    private User testUser;
    private UUID testDimonaId;
    private StatusHistory testStatusHistory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");

        testDimonaId = UUID.randomUUID();

        testStatusHistory = new StatusHistory();
        testStatusHistory.setId(UUID.randomUUID());
        testStatusHistory.setDimonaId(testDimonaId);
        testStatusHistory.setPreviousStatus("TO_SEND");
        testStatusHistory.setNewStatus("IN_PROGRESS");
        testStatusHistory.setChangeReason("Processing started");
        testStatusHistory.setChangedBy(testUser);
        testStatusHistory.setChangedAt(LocalDateTime.now());
    }

    @Test
    void testRecordDimonaStatusChange() {
        // Given
        when(statusHistoryRepository.save(any(StatusHistory.class))).thenReturn(testStatusHistory);

        // When
        StatusHistoryDto result = statusHistoryService.recordDimonaStatusChange(
                testDimonaId,
                "TO_SEND",
                "IN_PROGRESS",
                "Processing started",
                testUser
        );

        // Then
        assertNotNull(result);
        assertEquals(testStatusHistory.getId(), result.getId());
        assertEquals(testDimonaId, result.getDimonaId());
        assertEquals("TO_SEND", result.getPreviousStatus());
        assertEquals("IN_PROGRESS", result.getNewStatus());
        assertEquals("Processing started", result.getChangeReason());
        assertEquals(testUser.getId(), result.getChangedByUserId());
        assertEquals("John Doe", result.getChangedByUserName());

        verify(statusHistoryRepository).save(any(StatusHistory.class));
    }

    @Test
    void testGetDimonaStatusHistory() {
        // Given
        List<StatusHistory> historyList = Arrays.asList(testStatusHistory);
        when(statusHistoryRepository.findByDimonaIdOrderByChangedAtDesc(testDimonaId))
                .thenReturn(historyList);

        // When
        List<StatusHistoryDto> result = statusHistoryService.getDimonaStatusHistory(testDimonaId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStatusHistory.getId(), result.get(0).getId());
        verify(statusHistoryRepository).findByDimonaIdOrderByChangedAtDesc(testDimonaId);
    }

    @Test
    void testGetLatestDimonaStatusChange() {
        // Given
        when(statusHistoryRepository.findLatestByDimonaId(testDimonaId)).thenReturn(testStatusHistory);

        // When
        StatusHistoryDto result = statusHistoryService.getLatestDimonaStatusChange(testDimonaId);

        // Then
        assertNotNull(result);
        assertEquals(testStatusHistory.getId(), result.getId());
        verify(statusHistoryRepository).findLatestByDimonaId(testDimonaId);
    }

    @Test
    void testGetLatestDimonaStatusChangeNotFound() {
        // Given
        when(statusHistoryRepository.findLatestByDimonaId(testDimonaId)).thenReturn(null);

        // When
        StatusHistoryDto result = statusHistoryService.getLatestDimonaStatusChange(testDimonaId);

        // Then
        assertNull(result);
        verify(statusHistoryRepository).findLatestByDimonaId(testDimonaId);
    }

    @Test
    void testCountDimonaStatusChanges() {
        // Given
        long expectedCount = 5L;
        when(statusHistoryRepository.countByDimonaId(testDimonaId)).thenReturn(expectedCount);

        // When
        long result = statusHistoryService.countDimonaStatusChanges(testDimonaId);

        // Then
        assertEquals(expectedCount, result);
        verify(statusHistoryRepository).countByDimonaId(testDimonaId);
    }
}
