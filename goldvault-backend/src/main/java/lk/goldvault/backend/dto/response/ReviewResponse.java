package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReviewResponse {
    private Long          id;
    private Long          shopId;
    private String        shopName;
    private Long          customerId;
    private String        customerName;
    private Long          ticketId;
    private String        ticketNumber;
    private int           rating;
    private String        comment;
    private String        status;
    private LocalDateTime createdAt;
}