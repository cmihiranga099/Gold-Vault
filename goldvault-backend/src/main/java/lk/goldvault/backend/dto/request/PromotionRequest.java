package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PromotionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Promotion type is required")
    private String promoType; // REDUCED_INTEREST, BONUS_POINTS, FREE_RENEWAL, CUSTOM

    private BigDecimal promoValue; // e.g. 1.5 for 1.5% interest

    @NotNull(message = "Start date is required")
    private LocalDateTime startsAt;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endsAt;
}