package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.InterestType;
import lk.goldvault.backend.enums.TicketStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class PawnTicketResponse {
    private Long id;
    private String ticketNumber;
    private Long customerId;
    private String customerName;
    private String customerNic;
    private Long shopId;
    private Long branchId;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private InterestType interestType;
    private LocalDate pawnDate;
    private LocalDate expiryDate;
    private TicketStatus status;
    private String qrCode;
    private String notes;
    private List<GoldItemResponse> goldItems;

    /** Calculated fields — not stored, computed at response time */
    private BigDecimal totalPaid;
    private BigDecimal outstandingBalance;
    private long daysElapsed;
    private boolean overdue;
}