package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.response.NotificationFeedResponse;
import lk.goldvault.backend.dto.response.NotificationItemResponse;
import lk.goldvault.backend.entity.AmlFlag;
import lk.goldvault.backend.entity.Notification;
import lk.goldvault.backend.entity.Promotion;
import lk.goldvault.backend.repository.AmlFlagRepository;
import lk.goldvault.backend.repository.NotificationRepository;
import lk.goldvault.backend.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Builds the feed shown in the topbar notification bell.
 *
 * Ticket-expiry reminders are real per-customer rows (notification table)
 * with a proper read/unread flag. AML flags and promotions are surfaced
 * live from their own tables instead of being duplicated into notification
 * rows:
 *   - AML flags: an OPEN flag counts as unread; once an admin reviews or
 *     dismisses it (existing AML screen), it naturally drops off the badge.
 *   - Promotions: there's no per-customer "seen" concept for a shop-wide
 *     promo, so a promotion is flagged "new" for the first 3 days after
 *     it's created. This keeps the bell honest without adding a fan-out
 *     read table for a broadcast announcement.
 */
@Service
@RequiredArgsConstructor
public class NotificationFeedService {

    private static final int PROMOTION_NEW_WINDOW_DAYS = 3;
    private static final int MAX_ITEMS = 30;

    private final NotificationRepository notificationRepository;
    private final PromotionRepository    promotionRepository;
    private final AmlFlagRepository      amlFlagRepository;
    private final AmlDetectionService    amlDetectionService;

    // ── Customer feed: ticket reminders + promotions ────────────────────────

    public NotificationFeedResponse getCustomerFeed(Long customerId) {
        List<NotificationItemResponse> reminders = notificationRepository
                .findTop30ByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toReminderItem)
                .toList();

        LocalDateTime now = LocalDateTime.now();
        List<NotificationItemResponse> promos = promotionRepository
                .findAllActive(now)
                .stream()
                .map(p -> toPromotionItem(p, now))
                .toList();

        List<NotificationItemResponse> items = Stream.concat(reminders.stream(), promos.stream())
                .sorted(Comparator.comparing(NotificationItemResponse::getCreatedAt).reversed())
                .limit(MAX_ITEMS)
                .toList();

        long unread = notificationRepository.countByCustomerIdAndReadFalse(customerId)
                + promos.stream().filter(p -> !p.isRead()).count();

        return NotificationFeedResponse.builder().items(items).unreadCount(unread).build();
    }

    @Transactional
    public void markAllCustomerRead(Long customerId) {
        notificationRepository.markAllReadForCustomer(customerId);
        // Promotions have no persisted read state (see class javadoc) — their
        // "new" badge simply fades out after PROMOTION_NEW_WINDOW_DAYS.
    }

    @Transactional
    public void markReminderRead(Long customerId, Long reminderId) {
        Notification n = notificationRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + reminderId));
        if (n.getCustomer() == null || !n.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("This notification does not belong to this customer.");
        }
        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
    }

    // ── Admin feed: AML flags ────────────────────────────────────────────────

    public NotificationFeedResponse getAdminFeed() {
        List<NotificationItemResponse> items = amlFlagRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(MAX_ITEMS)
                .map(this::toAmlItem)
                .toList();

        long unread = amlFlagRepository.countByStatus("OPEN");

        return NotificationFeedResponse.builder().items(items).unreadCount(unread).build();
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private NotificationItemResponse toReminderItem(Notification n) {
        return NotificationItemResponse.builder()
                .id("reminder-" + n.getId())
                .type(n.getType().name())
                .title(titleFor(n.getType()))
                .message(n.getMessage())
                .link("/customer/dashboard")
                .createdAt(n.getCreatedAt())
                .read(n.isRead())
                .build();
    }

    private String titleFor(lk.goldvault.backend.enums.NotificationType type) {
        return switch (type) {
            case DUE_REMINDER -> "Ticket Expiry Reminder";
            case PAYMENT_CONFIRM -> "Payment Update";
            case OFFER_RECEIVED -> "New Offer";
            case AUCTION_NOTICE -> "Auction Notice";
        };
    }

    private NotificationItemResponse toPromotionItem(Promotion p, LocalDateTime now) {
        boolean isNew = p.getCreatedAt() != null
                && p.getCreatedAt().isAfter(now.minusDays(PROMOTION_NEW_WINDOW_DAYS));
        return NotificationItemResponse.builder()
                .id("promo-" + p.getId())
                .type("PROMOTION")
                .title(p.getTitle())
                .message(p.getShop().getName() + ": " + p.getDescription())
                .link("/customer/marketplace")
                .createdAt(p.getCreatedAt())
                .read(!isNew)
                .build();
    }

    private NotificationItemResponse toAmlItem(AmlFlag f) {
        boolean open = "OPEN".equals(f.getStatus());
        return NotificationItemResponse.builder()
                .id("aml-" + f.getId())
                .type("AML_ALERT")
                .title(amlDetectionService.flagTypeLabel(f.getFlagType()))
                .message(f.getDescription())
                .link("/admin/aml")
                .createdAt(f.getCreatedAt())
                .read(!open)
                .build();
    }
}