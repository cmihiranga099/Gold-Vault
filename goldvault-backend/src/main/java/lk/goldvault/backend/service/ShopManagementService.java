package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.ShopRegistrationRequest;
import lk.goldvault.backend.dto.response.ShopResponse;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.enums.ShopStatus;
import lk.goldvault.backend.repository.PawnShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopManagementService {

    private final PawnShopRepository pawnShopRepository;

    public ShopResponse register(ShopRegistrationRequest request) {
        if (pawnShopRepository.existsByRegNumber(request.getRegNumber())) {
            throw new RuntimeException(
                    "A shop with registration number " + request.getRegNumber() + " already exists");
        }

        PawnShop shop = PawnShop.builder()
                .name(request.getName())
                .regNumber(request.getRegNumber())
                .ownerName(request.getOwnerName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .status(ShopStatus.PENDING)
                .build();

        shop = pawnShopRepository.save(shop);
        return toResponse(shop);
    }

    public List<ShopResponse> getAll() {
        return pawnShopRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ShopResponse> getByStatus(ShopStatus status) {
        return pawnShopRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ShopResponse getById(Long id) {
        PawnShop shop = pawnShopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + id));
        return toResponse(shop);
    }

    public ShopResponse approve(Long id) {
        PawnShop shop = pawnShopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + id));

        if (shop.getStatus() != ShopStatus.PENDING) {
            throw new RuntimeException("Only PENDING shops can be approved. Current status: " + shop.getStatus());
        }

        shop.setStatus(ShopStatus.ACTIVE);
        shop = pawnShopRepository.save(shop);
        return toResponse(shop);
    }

    public ShopResponse suspend(Long id) {
        PawnShop shop = pawnShopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + id));

        if (shop.getStatus() == ShopStatus.SUSPENDED) {
            throw new RuntimeException("Shop is already suspended");
        }

        shop.setStatus(ShopStatus.SUSPENDED);
        shop = pawnShopRepository.save(shop);
        return toResponse(shop);
    }

    public ShopResponse reactivate(Long id) {
        PawnShop shop = pawnShopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + id));

        if (shop.getStatus() != ShopStatus.SUSPENDED) {
            throw new RuntimeException("Only SUSPENDED shops can be reactivated. Current status: " + shop.getStatus());
        }

        shop.setStatus(ShopStatus.ACTIVE);
        shop = pawnShopRepository.save(shop);
        return toResponse(shop);
    }

    private ShopResponse toResponse(PawnShop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .regNumber(shop.getRegNumber())
                .ownerName(shop.getOwnerName())
                .phone(shop.getPhone())
                .email(shop.getEmail())
                .address(shop.getAddress())
                .status(shop.getStatus())
                .createdAt(shop.getCreatedAt())
                .build();
    }
}