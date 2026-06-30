package lk.goldvault.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ShopRegistrationRequest {

    @NotBlank(message = "Shop name is required")
    private String name;

    @NotBlank(message = "Registration number is required")
    private String regNumber;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;
}