package lk.goldvault.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ExpiryReminderService expiryReminderService;

    /**
     * Runs every day at 8:00 AM Sri Lanka time (Asia/Colombo = UTC+5:30).
     * Cron is configurable via app.reminders.cron in application.properties.
     *
     * Cron format: second minute hour day month weekday
     *   0 0 8 * * *  →  every day at 08:00:00
     */
    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * *}",
               zone  = "Asia/Colombo")
    public void runExpiryReminders() {
        log.info("[Scheduler] Expiry reminder job triggered.");
        try {
            expiryReminderService.sendExpiryReminders();
        } catch (Exception e) {
            log.error("[Scheduler] Expiry reminder job failed: {}", e.getMessage(), e);
        }
    }
}