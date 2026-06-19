package lk.goldvault.backend.service;

import lk.goldvault.backend.entity.PawnTicket;
import lk.goldvault.backend.entity.Payment;
import lk.goldvault.backend.enums.InterestType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class InterestCalculatorService {

    private static final int DAYS_IN_YEAR = 365;

    /**
     * Calculates the total interest accrued on a ticket from pawnDate to the given asOfDate.
     * FLAT: interest = principal * (rate/100) * (days/365)
     * REDUCING: simplified daily-reducing model based on outstanding principal after partial payments.
     */
    public BigDecimal calculateAccruedInterest(PawnTicket ticket, LocalDate asOfDate) {
        long daysElapsed = ChronoUnit.DAYS.between(ticket.getPawnDate(), asOfDate);
        if (daysElapsed < 0) {
            daysElapsed = 0;
        }

        BigDecimal principal = ticket.getLoanAmount();
        BigDecimal annualRate = ticket.getInterestRate().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        if (ticket.getInterestType() == InterestType.FLAT) {
            return principal
                    .multiply(annualRate)
                    .multiply(BigDecimal.valueOf(daysElapsed))
                    .divide(BigDecimal.valueOf(DAYS_IN_YEAR), 2, RoundingMode.HALF_UP);
        }

        // REDUCING balance: reduce principal by any PARTIAL/FULL_REDEMPTION payments made so far,
        // then apply daily interest on the remaining balance for the elapsed period.
        BigDecimal totalPrincipalPaid = ticket.getPayments() == null ? BigDecimal.ZERO :
                ticket.getPayments().stream()
                        .filter(p -> p.getPaymentType() != lk.goldvault.backend.enums.PaymentType.INTEREST)
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingPrincipal = principal.subtract(totalPrincipalPaid).max(BigDecimal.ZERO);

        return remainingPrincipal
                .multiply(annualRate)
                .multiply(BigDecimal.valueOf(daysElapsed))
                .divide(BigDecimal.valueOf(DAYS_IN_YEAR), 2, RoundingMode.HALF_UP);
    }

    /** Total amount paid so far across all payment records on this ticket. */
    public BigDecimal totalPaid(PawnTicket ticket) {
        if (ticket.getPayments() == null) return BigDecimal.ZERO;
        return ticket.getPayments().stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Outstanding balance = principal + accrued interest - total paid.
     * This is what the customer owes if redeeming today.
     */
    public BigDecimal calculateOutstandingBalance(PawnTicket ticket, LocalDate asOfDate) {
        BigDecimal interest = calculateAccruedInterest(ticket, asOfDate);
        BigDecimal totalDue = ticket.getLoanAmount().add(interest);
        BigDecimal paid = totalPaid(ticket);
        return totalDue.subtract(paid).max(BigDecimal.ZERO);
    }

    public long daysElapsed(PawnTicket ticket, LocalDate asOfDate) {
        long days = ChronoUnit.DAYS.between(ticket.getPawnDate(), asOfDate);
        return Math.max(days, 0);
    }

    public boolean isOverdue(PawnTicket ticket, LocalDate asOfDate) {
        return asOfDate.isAfter(ticket.getExpiryDate());
    }
}