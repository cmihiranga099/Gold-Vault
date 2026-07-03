package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AmlFlagResponse {
    private Long          id;
    private Long          customerId;
    private String        customerName;
    private String        customerNic;
    private Long          shopId;
    private String        shopName;
    private Long          ticketId;
    private String        ticketNumber;
    private String        flagType;
    private String        flagTypeLabel;
    private String        description;
    private BigDecimal    amount;
    private String        status;
    private String        reviewedBy;
    private LocalDateTime reviewedAt;
    private String        reviewNote;
    private LocalDateTime createdAt;
}