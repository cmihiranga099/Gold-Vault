package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class MarketRateResponse {
    private Long       id;
    private String     purity;
    private BigDecimal ratePerGram;
    private String     source;
    private LocalDate  effectiveDate;
}