package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "NIC is required")
    @Pattern(
            regexp = "^([0-9]{9}[vVxX]|[0-9]{12})$",
            message = "NIC must be 9 digits + V/X, or 12 digits"
    )
    private String nic;

    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String address;

    private LocalDate dob;

    private String nicPhotoUrl;
}