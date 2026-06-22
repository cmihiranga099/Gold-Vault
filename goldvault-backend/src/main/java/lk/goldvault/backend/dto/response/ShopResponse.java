package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.ShopStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ShopResponse {
    private Long id;
    private String name;
    private String regNumber;
    private String ownerName;
    private String phone;
    private String email;
    private String address;
    private ShopStatus status;
    private LocalDateTime createdAt;
}