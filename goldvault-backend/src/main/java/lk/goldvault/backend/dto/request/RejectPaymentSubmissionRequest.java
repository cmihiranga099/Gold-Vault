package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectPaymentSubmissionRequest {

    @NotBlank(message = "A reason is required when rejecting a payment submission")
    private String reason;
}