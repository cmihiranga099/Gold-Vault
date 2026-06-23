package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private String fullName;
    private String role;
    private Long shopId;
    private Long customerId;
}