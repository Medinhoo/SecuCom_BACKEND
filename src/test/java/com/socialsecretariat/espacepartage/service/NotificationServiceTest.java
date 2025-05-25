package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.NotificationDto;
import com.socialsecretariat.espacepartage.model.Notification;
import com.socialsecretariat.espacepartage.model.Notification.NotificationType;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.NotificationRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private Notification testNotification;
    private UUID testUserId;
    private UUID testEntityId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEntityId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testNotification = new Notification();
        testNotification.setId(UUID.randomUUID());
        testNotification.setMessage("Test notification");
        testNotification.setType(NotificationType.COLLABORATOR_CREATED);
        testNotification.setRecipient(testUser);
        testNotification.setEntityId(testEntityId);
        testNotification.setRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createNotification_ShouldCreateAndReturnNotificationDto() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        NotificationDto result = notificationService.createNotification(
                testUserId, 
                "Test notification", 
                NotificationType.COLLABORATOR_CREATED, 
                testEntityId
        );

        // Then
        assertNotNull(result);
        assertEquals("Test notification", result.getMessage());
        assertEquals(NotificationType.COLLABORATOR_CREATED, result.getType());
        assertEquals(testUserId, result.getRecipientId());
        assertEquals(testEntityId, result.getEntityId());
        assertFalse(result.isRead());

        verify(userRepository).findById(testUserId);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_WithInvalidUserId_ShouldThrowException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.createNotification(
                    testUserId, 
                    "Test notification", 
                    NotificationType.COLLABORATOR_CREATED, 
                    testEntityId
            );
        });

        assertEquals("User not found with ID: " + testUserId, exception.getMessage());
        verify(userRepository).findById(testUserId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void getNotificationsForUser_ShouldReturnNotificationsList() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByRecipientOrderByCreatedAtDesc(testUser)).thenReturn(notifications);

        // When
        List<NotificationDto> result = notificationService.getNotificationsForUser(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test notification", result.get(0).getMessage());

        verify(userRepository).findById(testUserId);
        verify(notificationRepository).findByRecipientOrderByCreatedAtDesc(testUser);
    }

    @Test
    void getUnreadNotificationsForUser_ShouldReturnUnreadNotifications() {
        // Given
        List<Notification> unreadNotifications = Arrays.asList(testNotification);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(testUser))
                .thenReturn(unreadNotifications);

        // When
        List<NotificationDto> result = notificationService.getUnreadNotificationsForUser(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());

        verify(userRepository).findById(testUserId);
        verify(notificationRepository).findByRecipientAndReadFalseOrderByCreatedAtDesc(testUser);
    }

    @Test
    void countUnreadNotificationsForUser_ShouldReturnCount() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.countByRecipientAndReadFalse(testUser)).thenReturn(3L);

        // When
        long result = notificationService.countUnreadNotificationsForUser(testUserId);

        // Then
        assertEquals(3L, result);

        verify(userRepository).findById(testUserId);
        verify(notificationRepository).countByRecipientAndReadFalse(testUser);
    }

    @Test
    void markAsRead_ShouldMarkNotificationAsRead() {
        // Given
        UUID notificationId = testNotification.getId();
        testNotification.setRead(false);
        
        Notification readNotification = new Notification();
        readNotification.setId(notificationId);
        readNotification.setMessage(testNotification.getMessage());
        readNotification.setType(testNotification.getType());
        readNotification.setRecipient(testNotification.getRecipient());
        readNotification.setEntityId(testNotification.getEntityId());
        readNotification.setRead(true);
        readNotification.setCreatedAt(testNotification.getCreatedAt());

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(readNotification);

        // When
        NotificationDto result = notificationService.markAsRead(notificationId);

        // Then
        assertNotNull(result);
        assertTrue(result.isRead());

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void markAllAsReadForUser_ShouldMarkAllNotificationsAsRead() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        notificationService.markAllAsReadForUser(testUserId);

        // Then
        verify(userRepository).findById(testUserId);
        verify(notificationRepository).markAllAsReadForUser(testUser);
    }

    @Test
    void deleteNotification_ShouldDeleteNotification() {
        // Given
        UUID notificationId = testNotification.getId();
        when(notificationRepository.existsById(notificationId)).thenReturn(true);

        // When
        notificationService.deleteNotification(notificationId);

        // Then
        verify(notificationRepository).existsById(notificationId);
        verify(notificationRepository).deleteById(notificationId);
    }

    @Test
    void deleteNotification_WithInvalidId_ShouldThrowException() {
        // Given
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.existsById(notificationId)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.deleteNotification(notificationId);
        });

        assertEquals("Notification not found with ID: " + notificationId, exception.getMessage());
        verify(notificationRepository).existsById(notificationId);
        verify(notificationRepository, never()).deleteById(notificationId);
    }
}
