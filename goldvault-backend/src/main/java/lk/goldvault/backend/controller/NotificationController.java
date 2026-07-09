package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.dto.response.NotificationFeedResponse;
import lk.goldvault.backend.service.NotificationFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Topbar notification bell feed")
public class NotificationController {

    private final NotificationFeedService notificationFeedService;

    // ── Customer: ticket-expiry reminders + promotions ──────────────────────

    @GetMapping("/api/customer/notifications/{customerId}")
    @Operation(summary = "Get the customer's notification bell feed")
    public ResponseEntity<ApiResponse<NotificationFeedResponse>> getCustomerFeed(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success(notificationFeedService.getCustomerFeed(customerId)));
    }

    @PostMapping("/api/customer/notifications/{customerId}/read-all")
    @Operation(summary = "Mark all of the customer's ticket-expiry alerts as read")
    public ResponseEntity<ApiResponse<Void>> markAllCustomerRead(@PathVariable Long customerId) {
        notificationFeedService.markAllCustomerRead(customerId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read.", null));
    }

    @PostMapping("/api/customer/notifications/{customerId}/{notificationId}/read")
    @Operation(summary = "Mark a single ticket-expiry alert as read")
    public ResponseEntity<ApiResponse<Void>> markReminderRead(
            @PathVariable Long customerId, @PathVariable Long notificationId) {
        notificationFeedService.markReminderRead(customerId, notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read.", null));
    }

    // ── Admin: AML flags ──────────────────────────────────────────────────────

    @GetMapping("/api/admin/notifications")
    @Operation(summary = "Get the admin's notification bell feed (AML flags)")
    public ResponseEntity<ApiResponse<NotificationFeedResponse>> getAdminFeed() {
        return ResponseEntity.ok(ApiResponse.success(notificationFeedService.getAdminFeed()));
    }
}