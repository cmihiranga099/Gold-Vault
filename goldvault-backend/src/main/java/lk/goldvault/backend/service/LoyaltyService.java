package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.RedeemPointsRequest;
import lk.goldvault.backend.dto.response.LoyaltySummaryResponse;
import lk.goldvault.backend.dto.response.LoyaltyTransactionResponse;
import lk.goldvault.backend.entity.Customer;
import lk.goldvault.backend.entity.LoyaltyTransaction;
import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.repository.CustomerRepository;
import lk.goldvault.backend.repository.LoyaltyTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final CustomerRepository            customerRepository;
    private final LoyaltyTransactionRepository   loyaltyTransactionRepository;

    // ── Earning rules ────────────────────────────────────────────────────────────

    private static final int POINTS_PER_ON_TIME_REDEMPTION = 50;
    private static final int POINTS_PER_PAYMENT            = 5;
    private static final int POINTS_PER_REVIEW             = 10;
    private static final int POINTS_FOR_NEXT_REWARD         = 100; // milestone shown to customer

    /** 1% interest discount per 100 points redeemed, capped at 2% off */
    public static final double POINT_VALUE_PERCENT = 0.01;
    private static final int MAX_REDEEMABLE_POINTS  = 200;

    // ── Earning triggers ─────────────────────────────────────────────────────────

    /** Call when a ticket is redeemed. Awards bonus points only if redeemed before/on the due date. */
    @Transactional
    public void awardForRedemption(PawnTicket ticket) {
        boolean onTime = !ticket.getExpiryDate().isBefore(java.time.LocalDate.now());
        if (!onTime) return; // no points for late/overdue redemptions

        addPoints(
            ticket.getCustomer(),
            ticket,
            POINTS_PER_ON_TIME_REDEMPTION,
            "ON_TIME_REDEMPTION",
            "On-time redemption of ticket " + ticket.getTicketNumber()
        );
    }

    /** Call after any payment is recorded. */
    @Transactional
    public void awardForPayment(PawnTicket ticket, BigDecimal amount) {
        addPoints(
            ticket.getCustomer(),
            ticket,
            POINTS_PER_PAYMENT,
            "PAYMENT",
            "Payment of LKR " + amount + " on ticket " + ticket.getTicketNumber()
        );
    }

    /** Call after a customer submits a shop review. */
    @Transactional
    public void awardForReview(Customer customer, PawnTicket ticket) {
        addPoints(
            customer,
            ticket,
            POINTS_PER_REVIEW,
            "REVIEW",
            "Left a review for ticket " + (ticket != null ? ticket.getTicketNumber() : "")
        );
    }

    // ── Redemption ────────────────────────────────────────────────────────────────

    /**
     * Redeems points for an interest-rate discount. Returns the discount percentage to apply
     * to a new ticket's interest rate (e.g. 1.5 means subtract 1.5% from the rate).
     */
    @Transactional
    public double redeemPoints(RedeemPointsRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        int pointsToRedeem = request.getPoints();

        if (pointsToRedeem > MAX_REDEEMABLE_POINTS) {
            throw new RuntimeException("Cannot redeem more than " + MAX_REDEEMABLE_POINTS + " points at once.");
        }
        if (customer.getLoyaltyPoints() < pointsToRedeem) {
            throw new RuntimeException(
                    "Insufficient points. Available: " + customer.getLoyaltyPoints() + ", requested: " + pointsToRedeem);
        }

        double discountPercent = pointsToRedeem * POINT_VALUE_PERCENT;

        addPoints(
            customer,
            null,
            -pointsToRedeem,
            "REDEEMED_FOR_DISCOUNT",
            "Redeemed " + pointsToRedeem + " points for " + discountPercent + "% interest discount"
        );

        return discountPercent;
    }

    // ── Read ─────────────────────────────────────────────────────────────────────

    public LoyaltySummaryResponse getSummary(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        List<LoyaltyTransactionResponse> history = loyaltyTransactionRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();

        int current = customer.getLoyaltyPoints();
        int toNext = current >= POINTS_FOR_NEXT_REWARD
                ? 0
                : POINTS_FOR_NEXT_REWARD - (current % POINTS_FOR_NEXT_REWARD);

        return LoyaltySummaryResponse.builder()
                .currentPoints(current)
                .pointValuePercent(POINT_VALUE_PERCENT)
                .pointsToNextReward(toNext)
                .history(history)
                .build();
    }

    // ── Internal helper ──────────────────────────────────────────────────────────

    private void addPoints(Customer customer, PawnTicket ticket, int points, String reason, String description) {
        customer.setLoyaltyPoints(Math.max(0, customer.getLoyaltyPoints() + points));
        customerRepository.save(customer);

        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .customer(customer)
                .ticket(ticket)
                .points(points)
                .reason(reason)
                .description(description)
                .build();

        loyaltyTransactionRepository.save(tx);

        log.info("[Loyalty] Customer {} {}{} points ({}). New balance: {}",
                customer.getId(), points >= 0 ? "+" : "", points, reason, customer.getLoyaltyPoints());
    }

    private LoyaltyTransactionResponse toResponse(LoyaltyTransaction tx) {
        return LoyaltyTransactionResponse.builder()
                .id(tx.getId())
                .points(tx.getPoints())
                .reason(tx.getReason())
                .description(tx.getDescription())
                .ticketNumber(tx.getTicket() != null ? tx.getTicket().getTicketNumber() : null)
                .createdAt(tx.getCreatedAt())
                .build();
    }
}