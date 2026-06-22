package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AdminUserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private Long shopId;
    private boolean enabled;
    private LocalDateTime createdAt;
}