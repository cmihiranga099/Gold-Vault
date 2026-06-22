package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchRequest {

    @NotBlank(message = "Branch name is required")
    private String name;

    private String address;

    private String phone;

    private boolean isMain;
}