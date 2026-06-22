package lk.goldvault.backend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BranchResponse {
    private Long id;
    private Long shopId;
    private String shopName;
    private String name;
    private String address;
    private String phone;
    private boolean isMain;
    private LocalDateTime createdAt;
}