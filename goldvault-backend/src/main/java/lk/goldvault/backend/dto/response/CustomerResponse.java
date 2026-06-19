package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.KycStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String nic;
    private String phone;
    private String email;
    private String address;
    private LocalDate dob;
    private KycStatus kycStatus;
    private String nicPhotoUrl;
    private Long shopId;
    private LocalDateTime createdAt;
}