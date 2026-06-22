package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.GoldRateRequest;
import lk.goldvault.backend.dto.response.GoldRateResponse;
import lk.goldvault.backend.entity.GoldRate;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.enums.GoldPurity;
import lk.goldvault.backend.repository.GoldRateRepository;
import lk.goldvault.backend.repository.PawnShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoldRateService {

    private final GoldRateRepository goldRateRepository;
    private final PawnShopRepository pawnShopRepository;

    /**
     * Publishes a new rate for a shop+purity combination.
     * Any previously active rate for the same shop+purity is deactivated first,
     * so there is always exactly one active rate per purity per shop.
     */
    @Transactional
    public GoldRateResponse publishRate(Long shopId, GoldRateRequest request) {
        PawnShop shop = pawnShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));

        goldRateRepository.findFirstByShopIdAndPurityAndIsActiveTrueOrderByEffectiveDateDesc(
                        shopId, request.getPurity())
                .ifPresent(existing -> {
                    existing.setActive(false);
                    goldRateRepository.save(existing);
                });

        GoldRate rate = GoldRate.builder()
                .shop(shop)
                .purity(request.getPurity())
                .ratePerGram(request.getRatePerGram())
                .effectiveDate(LocalDate.now())
                .isActive(true)
                .build();

        rate = goldRateRepository.save(rate);
        return toResponse(rate);
    }

    public List<GoldRateResponse> getActiveRatesByShop(Long shopId) {
        return goldRateRepository.findByShopIdAndIsActiveTrue(shopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Public marketplace view — every shop's active rates, for comparison. */
    public List<GoldRateResponse> getAllActiveRates() {
        return goldRateRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GoldRateResponse getActiveRate(Long shopId, GoldPurity purity) {
        GoldRate rate = goldRateRepository
                .findFirstByShopIdAndPurityAndIsActiveTrueOrderByEffectiveDateDesc(shopId, purity)
                .orElseThrow(() -> new RuntimeException(
                        "No active rate found for shop " + shopId + " and purity " + purity));
        return toResponse(rate);
    }

    private GoldRateResponse toResponse(GoldRate rate) {
        return GoldRateResponse.builder()
                .id(rate.getId())
                .shopId(rate.getShop().getId())
                .shopName(rate.getShop().getName())
                .purity(rate.getPurity())
                .ratePerGram(rate.getRatePerGram())
                .effectiveDate(rate.getEffectiveDate())
                .active(rate.isActive())
                .build();
    }
}