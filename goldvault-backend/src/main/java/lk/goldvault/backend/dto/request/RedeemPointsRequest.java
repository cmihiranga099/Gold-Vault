package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedeemPointsRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Points to redeem is required")
    @Min(value = 1, message = "Must redeem at least 1 point")
    private Integer points;
}