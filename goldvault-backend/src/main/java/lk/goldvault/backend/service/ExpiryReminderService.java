package lk.goldvault.backend.service;

import lk.goldvault.backend.entity.Customer;
import lk.goldvault.backend.entity.Notification;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.enums.NotificationChannel;
import lk.goldvault.backend.enums.NotificationType;
import lk.goldvault.backend.enums.TicketStatus;
import lk.goldvault.backend.repository.NotificationRepository;
import lk.goldvault.backend.repository.PawnTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpiryReminderService {

    private final PawnTicketRepository  pawnTicketRepository;
    private final NotificationRepository notificationRepository;
    private final SmsService             smsService;

    @Value("${app.reminders.days-before:7,3,1}")
    private String daysBeforeConfig;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    /**
     * Main entry point — called by the scheduler.
     * Scans for ACTIVE tickets expiring in 7, 3, and 1 day(s) and sends SMS reminders.
     */
    @Transactional
    public void sendExpiryReminders() {
        LocalDate today = LocalDate.now();
        int[] daysBefore = parseDaysBefore();

        log.info("[Reminders] Running expiry reminder job — date: {}", today);

        int totalSent = 0;

        for (int days : daysBefore) {
            LocalDate targetDate = today.plusDays(days);

            List<PawnTicket> tickets = pawnTicketRepository
                    .findByStatusAndExpiryDateBetween(
                            TicketStatus.ACTIVE,
                            targetDate,
                            targetDate
                    );

            log.info("[Reminders] {} ticket(s) expiring in {} day(s) on {}",
                    tickets.size(), days, targetDate);

            for (PawnTicket ticket : tickets) {
                if (processTicket(ticket, days)) {
                    totalSent++;
                }
            }
        }

        // Also mark already-expired ACTIVE tickets (grace period missed)
        markOverdueTickets(today);

        log.info("[Reminders] Job complete. {} reminder(s) sent.", totalSent);
    }

    // ── Per-ticket processing ────────────────────────────────────────────────────

    private boolean processTicket(PawnTicket ticket, int daysLeft) {
        Customer customer = ticket.getCustomer();

        // Build message
        String message = buildSmsMessage(ticket, daysLeft);

        // Deduplication check — don't send same reminder twice
        String snippet = "%" + ticket.getTicketNumber() + "%" + daysLeft + " day%";
        boolean alreadySent = notificationRepository.alreadySent(
                customer.getId(),
                NotificationType.DUE_REMINDER,
                NotificationChannel.SMS,
                snippet
        );

        if (alreadySent) {
            log.debug("[Reminders] Skipping duplicate for ticket {} customer {}",
                    ticket.getTicketNumber(), customer.getId());
            return false;
        }

        // Try SMS
        boolean smsSent = false;
        if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
            smsSent = smsService.send(customer.getPhone(), message);
        } else {
            log.warn("[Reminders] Customer {} has no phone number — skipping SMS for ticket {}",
                    customer.getId(), ticket.getTicketNumber());
        }

        // Always save the notification record (even if SMS failed — for audit trail)
        Notification notification = Notification.builder()
                .customer(customer)
                .type(NotificationType.DUE_REMINDER)
                .channel(NotificationChannel.SMS)
                .message(message)
                .sent(smsSent)
                .sentAt(smsSent ? LocalDateTime.now() : null)
                .build();

        notificationRepository.save(notification);

        log.info("[Reminders] Ticket {} | Customer {} | {}d left | SMS sent: {}",
                ticket.getTicketNumber(), customer.getId(), daysLeft, smsSent);

        return smsSent;
    }

    // ── Overdue tickets ──────────────────────────────────────────────────────────

    private void markOverdueTickets(LocalDate today) {
        List<PawnTicket> overdueTickets = pawnTicketRepository
                .findByStatusAndExpiryDateBefore(TicketStatus.ACTIVE, today);

        if (overdueTickets.isEmpty()) return;

        log.info("[Reminders] {} overdue ticket(s) found — sending overdue alert", overdueTickets.size());

        for (PawnTicket ticket : overdueTickets) {
            Customer customer = ticket.getCustomer();
            String message = buildOverdueSmsMessage(ticket);

            String snippet = "%" + ticket.getTicketNumber() + "%OVERDUE%";
            boolean alreadySent = notificationRepository.alreadySent(
                    customer.getId(),
                    NotificationType.DUE_REMINDER,
                    NotificationChannel.SMS,
                    snippet
            );

            if (alreadySent) continue;

            boolean smsSent = false;
            if (customer.getPhone() != null && !customer.getPhone().isBlank()) {
                smsSent = smsService.send(customer.getPhone(), message);
            }

            Notification notification = Notification.builder()
                    .customer(customer)
                    .type(NotificationType.DUE_REMINDER)
                    .channel(NotificationChannel.SMS)
                    .message(message)
                    .sent(smsSent)
                    .sentAt(smsSent ? LocalDateTime.now() : null)
                    .build();

            notificationRepository.save(notification);
        }
    }

    // ── Message builders ─────────────────────────────────────────────────────────

    private String buildSmsMessage(PawnTicket ticket, int daysLeft) {
        String dayWord = daysLeft == 1 ? "day" : "days";
        return String.format(
            "GoldVault Alert: Dear %s, your pawn ticket %s expires in %d %s on %s. " +
            "Please visit %s to redeem your items. Call: %s",
            ticket.getCustomer().getFullName(),
            ticket.getTicketNumber(),
            daysLeft,
            dayWord,
            ticket.getExpiryDate().format(DATE_FMT),
            ticket.getShop().getName(),
            ticket.getShop().getPhone() != null ? ticket.getShop().getPhone() : "your shop"
        );
    }

    private String buildOverdueSmsMessage(PawnTicket ticket) {
        return String.format(
            "GoldVault OVERDUE: Dear %s, your pawn ticket %s expired on %s. " +
            "Contact %s immediately to avoid auction. Call: %s",
            ticket.getCustomer().getFullName(),
            ticket.getTicketNumber(),
            ticket.getExpiryDate().format(DATE_FMT),
            ticket.getShop().getName(),
            ticket.getShop().getPhone() != null ? ticket.getShop().getPhone() : "your shop"
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private int[] parseDaysBefore() {
        try {
            String[] parts = daysBeforeConfig.split(",");
            int[] result = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Integer.parseInt(parts[i].trim());
            }
            return result;
        } catch (Exception e) {
            log.warn("[Reminders] Could not parse app.reminders.days-before — using default 7,3,1");
            return new int[]{7, 3, 1};
        }
    }
}