package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AuctionBidResponse {
    private Long          id;
    private String        bidderName;
    private BigDecimal    amount;
    private LocalDateTime createdAt;
}