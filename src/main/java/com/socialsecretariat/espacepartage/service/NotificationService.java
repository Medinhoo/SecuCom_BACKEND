package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.NotificationDto;
import com.socialsecretariat.espacepartage.model.Notification;
import com.socialsecretariat.espacepartage.model.Notification.NotificationType;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.NotificationRepository;
import com.socialsecretariat.espacepartage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Create a new notification
     */
    public NotificationDto createNotification(UUID recipientId, String message, NotificationType type, UUID entityId) {
        log.debug("Creating notification for user {} with message: {}", recipientId, message);
        
        Optional<User> recipient = userRepository.findById(recipientId);
        if (recipient.isEmpty()) {
            log.error("User not found with ID: {}", recipientId);
            throw new RuntimeException("User not found with ID: " + recipientId);
        }

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setType(type);
        notification.setRecipient(recipient.get());
        notification.setEntityId(entityId);
        notification.setRead(false);

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created with ID: {} for user: {}", savedNotification.getId(), recipientId);
        
        return convertToDto(savedNotification);
    }

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsForUser(UUID userId) {
        log.debug("Fetching notifications for user: {}", userId);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found with ID: " + userId);
        }

        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user.get());
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get notifications for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsForUser(UUID userId, Pageable pageable) {
        log.debug("Fetching paginated notifications for user: {}", userId);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Page<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user.get(), pageable);
        return notifications.map(this::convertToDto);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotificationsForUser(UUID userId) {
        log.debug("Fetching unread notifications for user: {}", userId);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found with ID: " + userId);
        }

        List<Notification> notifications = notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(user.get());
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Count unread notifications for a user
     */
    @Transactional(readOnly = true)
    public long countUnreadNotificationsForUser(UUID userId) {
        log.debug("Counting unread notifications for user: {}", userId);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found with ID: " + userId);
        }

        return notificationRepository.countByRecipientAndReadFalse(user.get());
    }

    /**
     * Mark a notification as read
     */
    public NotificationDto markAsRead(UUID notificationId) {
        log.debug("Marking notification as read: {}", notificationId);
        
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if (notification.isEmpty()) {
            log.error("Notification not found with ID: {}", notificationId);
            throw new RuntimeException("Notification not found with ID: " + notificationId);
        }

        Notification notif = notification.get();
        notif.markAsRead();
        Notification savedNotification = notificationRepository.save(notif);
        
        log.info("Notification marked as read: {}", notificationId);
        return convertToDto(savedNotification);
    }

    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsReadForUser(UUID userId) {
        log.debug("Marking all notifications as read for user: {}", userId);
        
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            log.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found with ID: " + userId);
        }

        notificationRepository.markAllAsReadForUser(user.get());
        log.info("All notifications marked as read for user: {}", userId);
    }

    /**
     * Delete a notification
     */
    public void deleteNotification(UUID notificationId) {
        log.debug("Deleting notification: {}", notificationId);
        
        if (!notificationRepository.existsById(notificationId)) {
            log.error("Notification not found with ID: {}", notificationId);
            throw new RuntimeException("Notification not found with ID: " + notificationId);
        }

        notificationRepository.deleteById(notificationId);
        log.info("Notification deleted: {}", notificationId);
    }

    /**
     * Clean up old notifications (older than 30 days)
     */
    public void cleanupOldNotifications() {
        log.debug("Cleaning up old notifications");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldNotifications(cutoffDate);
        
        log.info("Old notifications cleaned up (older than {})", cutoffDate);
    }

    /**
     * Create notification for collaborator creation
     */
    public void notifyCollaboratorCreated(UUID collaboratorId, String collaboratorName, UUID companyId, UUID createdByUserId) {
        log.debug("Creating collaborator notification for collaborator: {} in company: {}", collaboratorName, companyId);
        
        String message = String.format("Un nouveau collaborateur '%s' a été ajouté.", collaboratorName);
        
        // Notify secretariat users
        List<User> secretariatUsers = userRepository.findByRolesContaining(User.Role.ROLE_SECRETARIAT);
        for (User user : secretariatUsers) {
            if (!user.getId().equals(createdByUserId)) { // Don't notify the creator
                createNotification(user.getId(), message, NotificationType.COLLABORATOR_CREATED, collaboratorId);
            }
        }

        // Notify company contacts of the same company
        List<User> companyContacts = userRepository.findCompanyContactsByCompanyId(companyId);
        for (User contact : companyContacts) {
            if (!contact.getId().equals(createdByUserId)) { // Don't notify the creator
                createNotification(contact.getId(), message, NotificationType.COLLABORATOR_CREATED, collaboratorId);
            }
        }
    }

    /**
     * Create notification for DIMONA creation
     */
    public void notifyDimonaCreated(UUID dimonaId, String collaboratorName, UUID companyId, UUID createdByUserId) {
        log.debug("Creating DIMONA notification for collaborator: {} in company: {}", collaboratorName, companyId);
        
        String message = String.format("Une nouvelle déclaration DIMONA a été créée pour '%s'.", collaboratorName);
        
        // Notify secretariat users
        List<User> secretariatUsers = userRepository.findByRolesContaining(User.Role.ROLE_SECRETARIAT);
        for (User user : secretariatUsers) {
            if (!user.getId().equals(createdByUserId)) { // Don't notify the creator
                createNotification(user.getId(), message, NotificationType.DIMONA_CREATED, dimonaId);
            }
        }

        // Notify company contacts of the same company
        List<User> companyContacts = userRepository.findCompanyContactsByCompanyId(companyId);
        for (User contact : companyContacts) {
            if (!contact.getId().equals(createdByUserId)) { // Don't notify the creator
                createNotification(contact.getId(), message, NotificationType.DIMONA_CREATED, dimonaId);
            }
        }
    }

    /**
     * Create notification for DIMONA status change
     */
    public void notifyDimonaStatusChanged(UUID dimonaId, String collaboratorName, String newStatus, UUID companyId) {
        log.debug("Creating DIMONA status change notification for collaborator: {} with status: {}", collaboratorName, newStatus);
        
        String message = String.format("Le statut de la déclaration DIMONA pour '%s' a été mis à jour: %s.", collaboratorName, newStatus);
        
        // Notify all company contacts of the same company
        List<User> companyContacts = userRepository.findCompanyContactsByCompanyId(companyId);
        for (User contact : companyContacts) {
            createNotification(contact.getId(), message, NotificationType.DIMONA_STATUS_CHANGED, dimonaId);
        }
    }

    /**
     * Create notification for company completion
     */
    public void notifyCompanyCompleted(UUID companyId, String companyName, UUID updatedByUserId) {
        log.debug("Creating company completion notification for company: {}", companyName);
        
        String message = String.format("L'entreprise '%s' a été complétée avec toutes les informations requises.", companyName);
        
        // Notify secretariat users
        List<User> secretariatUsers = userRepository.findByRolesContaining(User.Role.ROLE_SECRETARIAT);
        for (User user : secretariatUsers) {
            if (!user.getId().equals(updatedByUserId)) { // Don't notify the user who made the update
                createNotification(user.getId(), message, NotificationType.COMPANY_COMPLETED, companyId);
            }
        }

        // Notify admin users
        List<User> adminUsers = userRepository.findByRolesContaining(User.Role.ROLE_ADMIN);
        for (User user : adminUsers) {
            if (!user.getId().equals(updatedByUserId)) { // Don't notify the user who made the update
                createNotification(user.getId(), message, NotificationType.COMPANY_COMPLETED, companyId);
            }
        }
    }

    /**
     * Create notification for company data confirmation by company contact
     */
    public void notifySecretariatOfCompanyConfirmation(UUID companyId, String companyName) {
        log.debug("Creating company confirmation notification for company: {}", companyName);
        
        String message = String.format("L'entreprise '%s' a confirmé ses données.", companyName);
        
        // Notify secretariat users
        List<User> secretariatUsers = userRepository.findByRolesContaining(User.Role.ROLE_SECRETARIAT);
        for (User user : secretariatUsers) {
            createNotification(user.getId(), message, NotificationType.COMPANY_DATA_CONFIRMED, companyId);
        }

        // Notify admin users
        List<User> adminUsers = userRepository.findByRolesContaining(User.Role.ROLE_ADMIN);
        for (User user : adminUsers) {
            createNotification(user.getId(), message, NotificationType.COMPANY_DATA_CONFIRMED, companyId);
        }
    }

    /**
     * Create notification for company contact to reconfirm data after secretariat modification
     */
    public void notifyCompanyContactForReconfirmation(UUID contactId, UUID companyId, String companyName) {
        log.debug("Creating reconfirmation notification for company contact: {} for company: {}", contactId, companyName);
        
        String message = String.format("Les données de votre entreprise '%s' ont été modifiées par le secrétariat social. Veuillez confirmer les nouvelles informations.", companyName);
        
        createNotification(contactId, message, NotificationType.COMPANY_DATA_RECONFIRMATION_REQUIRED, companyId);
    }

    /**
     * Convert Notification entity to DTO
     */
    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setRecipientId(notification.getRecipient().getId());
        dto.setEntityId(notification.getEntityId());
        return dto;
    }
}
