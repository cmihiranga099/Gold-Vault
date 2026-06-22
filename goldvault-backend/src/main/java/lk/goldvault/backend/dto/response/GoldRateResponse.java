package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.GoldPurity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class GoldRateResponse {
    private Long id;
    private Long shopId;
    private String shopName;
    private GoldPurity purity;
    private BigDecimal ratePerGram;
    private LocalDate effectiveDate;
    private boolean active;
}