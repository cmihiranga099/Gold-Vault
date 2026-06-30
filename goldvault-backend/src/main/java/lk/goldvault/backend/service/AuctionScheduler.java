package lk.goldvault.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final AuctionService auctionService;

    /** Runs daily at 09:00 Colombo time — creates auctions for tickets 14+ days overdue. */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Colombo")
    public void createAuctions() {
        try {
            int created = auctionService.createAuctionsForOverdueTickets();
            log.info("[AuctionScheduler] Created {} new auction(s).", created);
        } catch (Exception e) {
            log.error("[AuctionScheduler] Failed to create auctions: {}", e.getMessage(), e);
        }
    }

    /** Runs every hour — auto-closes auctions whose end time has passed. */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Colombo")
    public void closeExpiredAuctions() {
        try {
            int closed = auctionService.autoCloseExpiredAuctions();
            if (closed > 0) log.info("[AuctionScheduler] Auto-closed {} auction(s).", closed);
        } catch (Exception e) {
            log.error("[AuctionScheduler] Failed to auto-close auctions: {}", e.getMessage(), e);
        }
    }
}