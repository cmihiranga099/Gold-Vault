package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LoyaltyTransactionResponse {
    private Long id;
    private int points;
    private String reason;
    private String description;
    private String ticketNumber;
    private LocalDateTime createdAt;
}