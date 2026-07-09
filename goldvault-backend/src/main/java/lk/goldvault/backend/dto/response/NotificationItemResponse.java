package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A single entry in the topbar notification bell, unified across the
 * different sources that feed it (ticket-expiry reminders, AML flags,
 * promotions). {@code id} is prefixed by source (e.g. "reminder-42",
 * "aml-7", "promo-3") so the frontend can tell them apart and so
 * only reminder items are ever passed to the mark-as-read endpoint.
 */
@Getter
@Setter
@Builder
public class NotificationItemResponse {
    private String id;
    private String type;      // DUE_REMINDER | AML_ALERT | PROMOTION
    private String title;
    private String message;
    private String link;
    private LocalDateTime createdAt;
    private boolean read;
}