package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class DailyCollectionResponse {
    private LocalDate date;
    private BigDecimal totalCollected;
    private long paymentCount;
    private BigDecimal totalInterest;
    private BigDecimal totalPartial;
    private BigDecimal totalFullRedemption;
    private List<PaymentResponse> payments;
}