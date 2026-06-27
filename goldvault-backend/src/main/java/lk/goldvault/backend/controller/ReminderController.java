package lk.goldvault.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lk.goldvault.backend.dto.response.ApiResponse;
import lk.goldvault.backend.service.ExpiryReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reminders")
@RequiredArgsConstructor
@Tag(name = "Reminders", description = "Manually trigger reminder jobs")
public class ReminderController {

    private final ExpiryReminderService expiryReminderService;

    /**
     * POST /api/admin/reminders/run
     * Manually trigger the expiry reminder job — useful for testing without
     * waiting for the scheduled time. Only accessible to ROLE_ADMIN.
     */
    @PostMapping("/run")
    @Operation(summary = "Manually trigger expiry reminder job (admin only)")
    public ResponseEntity<ApiResponse<String>> triggerReminders() {
        expiryReminderService.sendExpiryReminders();
        return ResponseEntity.ok(ApiResponse.success("Reminder job executed successfully."));
    }
}