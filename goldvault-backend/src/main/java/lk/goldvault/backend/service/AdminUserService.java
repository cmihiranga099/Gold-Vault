 package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.response.AdminUserResponse;
import lk.goldvault.backend.entity.AppUser;
import lk.goldvault.backend.enums.UserRole;
import lk.goldvault.backend.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AppUserRepository appUserRepository;

    public List<AdminUserResponse> getAll() {
        return appUserRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AdminUserResponse getById(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toResponse(user);
    }

    public AdminUserResponse disable(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setEnabled(false);
        user = appUserRepository.save(user);
        return toResponse(user);
    }

    public AdminUserResponse enable(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setEnabled(true);
        user = appUserRepository.save(user);
        return toResponse(user);
    }

    /** Promotes a customer-role user to staff for a given shop. */
    public AdminUserResponse assignAsStaff(Long userId, Long shopId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setRole(UserRole.ROLE_STAFF);
        user.setShopId(shopId);
        user = appUserRepository.save(user);
        return toResponse(user);
    }

    private AdminUserResponse toResponse(AppUser user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .shopId(user.getShopId())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}