package lk.goldvault.backend.repository;

import lk.goldvault.backend.entity.Notification;
import lk.goldvault.backend.enums.NotificationChannel;
import lk.goldvault.backend.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCustomerId(Long customerId);

    List<Notification> findBySentFalse();

    /** Most recent notifications for the customer's in-app notification bell. */
    List<Notification> findTop30ByCustomerIdOrderByCreatedAtDesc(Long customerId);

    long countByCustomerIdAndReadFalse(Long customerId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP
        WHERE n.customer.id = :customerId AND n.read = false
    """)
    int markAllReadForCustomer(@Param("customerId") Long customerId);

    /** Check if we already sent a reminder of this type+channel for this customer today,
     *  so we never send duplicates even if the scheduler somehow runs twice. */
    @Query("""
        SELECT COUNT(n) > 0 FROM Notification n
        WHERE n.customer.id = :customerId
          AND n.type        = :type
          AND n.channel     = :channel
          AND n.message     LIKE :messageSnippet
          AND n.sent        = true
    """)
    boolean alreadySent(
        @Param("customerId") Long customerId,
        @Param("type")        NotificationType type,
        @Param("channel")     NotificationChannel channel,
        @Param("messageSnippet") String messageSnippet
    );
}