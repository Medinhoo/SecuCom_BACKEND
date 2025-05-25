package com.socialsecretariat.espacepartage.controller;

import com.socialsecretariat.espacepartage.dto.NotificationDto;
import com.socialsecretariat.espacepartage.dto.auth.MessageResponse;
import com.socialsecretariat.espacepartage.model.Notification.NotificationType;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.service.NotificationService;
import com.socialsecretariat.espacepartage.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * Get all notifications for the current user
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECRETARIAT') or hasRole('COMPANY')")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        UUID currentUserId = getCurrentUserId();
        log.debug("Getting notifications for user: {}", currentUserId);
        
        List<NotificationDto> notifications = notificationService.getNotificationsForUser(currentUserId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications for the current user with pagination
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECRETARIAT') or hasRole('COMPANY')")
    public ResponseEntity<Page<NotificationDto>> getNotificationsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID currentUserId = getCurrentUserId();
        log.debug("Getting paginated notifications for user: {} (page: {}, size: {})", currentUserId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getNotificationsForUser(currentUserId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications for the current user
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECRETARIAT') or hasRole('COMPANY')")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications() {
        UUID currentUserId = getCurrentUserId();
        log.debug("Getting unread notifications for user: {}", currentUserId);
        
        List<NotificationDto> notifications = notificationService.getUnreadNotificationsForUser(currentUserId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Count unread notifications for the current user
     */
    @GetMapping("/unread/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECRETARIAT') or hasRole('COMPANY')")
    public ResponseEntity<Long> countUnreadNotifications() {
        UUID currentUserId = getCurrentUserId();
        log.debug("Counting unread notifications for user: {}", currentUserId);
        
        long count = notificationService.countUnreadNotificationsForUser(currentUserId);
        return ResponseEntity.ok(count);
    }

    /**
     * Mark a notification as read
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECRETARIAT') or hasRole('COMPANY')")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable UUID notificationId) {
        log.debug("Marking notification as read: {}", notificationId);
        
        NotificationDto notification = notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(notification);
    }

    /**
     * Mark all notifications as read for the current user
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECRETARIAT') or hasRole('COMPANY')")
    public ResponseEntity<MessageResponse> markAllAsRead() {
        UUID currentUserId = getCurrentUserId();
        log.debug("Marking all notifications as read for user: {}", currentUserId);
        
        notificationService.markAllAsReadForUser(currentUserId);
        return ResponseEntity.ok(new MessageResponse("Toutes les notifications ont été marquées comme lues."));
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECRETARIAT') or hasRole('COMPANY')")
    public ResponseEntity<MessageResponse> deleteNotification(@PathVariable UUID notificationId) {
        log.debug("Deleting notification: {}", notificationId);
        
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(new MessageResponse("Notification supprimée avec succès."));
    }

    /**
     * Admin endpoint to clean up old notifications
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> cleanupOldNotifications() {
        log.debug("Cleaning up old notifications");
        
        notificationService.cleanupOldNotifications();
        return ResponseEntity.ok(new MessageResponse("Anciennes notifications supprimées avec succès."));
    }

    /**
     * Get notifications for a specific user (Admin only)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getNotificationsForUser(@PathVariable UUID userId) {
        log.debug("Admin getting notifications for user: {}", userId);
        
        List<NotificationDto> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Create a notification (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> createNotification(
            @RequestParam UUID recipientId,
            @RequestParam String message,
            @RequestParam String type,
            @RequestParam(required = false) UUID entityId) {
        log.debug("Admin creating notification for user: {} with message: {}", recipientId, message);
        
        try {
            NotificationType notificationType = NotificationType.valueOf(type);
            NotificationDto notification = notificationService.createNotification(recipientId, message, notificationType, entityId);
            return ResponseEntity.ok(notification);
        } catch (IllegalArgumentException e) {
            log.error("Invalid notification type: {}", type);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current user ID from security context
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.debug("Getting user ID for username: {}", username);
        
        // Get the user by username and return their ID
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        return user.getId();
    }
}
