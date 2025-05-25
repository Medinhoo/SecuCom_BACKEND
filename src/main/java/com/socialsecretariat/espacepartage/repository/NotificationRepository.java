package com.socialsecretariat.espacepartage.repository;

import com.socialsecretariat.espacepartage.model.Notification;
import com.socialsecretariat.espacepartage.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for a specific user, ordered by creation date (newest first)
     */
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    /**
     * Find all notifications for a specific user with pagination
     */
    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    /**
     * Find unread notifications for a specific user
     */
    List<Notification> findByRecipientAndReadFalseOrderByCreatedAtDesc(User recipient);

    /**
     * Count unread notifications for a specific user
     */
    long countByRecipientAndReadFalse(User recipient);

    /**
     * Mark all notifications as read for a specific user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient = :recipient AND n.read = false")
    void markAllAsReadForUser(@Param("recipient") User recipient);

    /**
     * Delete old notifications (older than specified days)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    /**
     * Find notifications by recipient ID
     */
    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(UUID recipientId);
}
