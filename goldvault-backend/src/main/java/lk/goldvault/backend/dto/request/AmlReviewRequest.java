package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AmlReviewRequest {

    @NotBlank(message = "Status is required")
    private String status; // REVIEWED or DISMISSED

    private String reviewNote;

    @NotBlank(message = "Reviewer name is required")
    private String reviewedBy;
}