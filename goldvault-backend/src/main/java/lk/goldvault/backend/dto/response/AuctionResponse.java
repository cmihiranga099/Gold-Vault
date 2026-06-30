package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class AuctionResponse {
    private Long          id;
    private Long          ticketId;
    private String        ticketNumber;
    private Long          shopId;
    private String        shopName;
    private BigDecimal    startingPrice;
    private BigDecimal    currentBid;
    private int           bidCount;
    private String        status;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private boolean       ended;
    private List<GoldItemResponse> goldItems;
    private String        winningBidderName; // only populated once CLOSED
}