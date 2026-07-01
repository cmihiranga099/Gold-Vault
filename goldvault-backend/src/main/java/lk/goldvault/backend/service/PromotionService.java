package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.PromotionRequest;
import lk.goldvault.backend.dto.response.PromotionResponse;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.entity.Promotion;
import lk.goldvault.backend.repository.PawnShopRepository;
import lk.goldvault.backend.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PawnShopRepository  pawnShopRepository;

    // ── Shop: create promotion ─────────────────────────────────────────────────

    @Transactional
    public PromotionResponse create(Long shopId, PromotionRequest request) {
        PawnShop shop = pawnShopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + shopId));

        if (request.getEndsAt().isBefore(request.getStartsAt())) {
            throw new RuntimeException("End date must be after start date.");
        }

        Promotion promo = Promotion.builder()
                .shop(shop)
                .title(request.getTitle())
                .description(request.getDescription())
                .promoType(request.getPromoType())
                .promoValue(request.getPromoValue())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .status("ACTIVE")
                .build();

        return toResponse(promotionRepository.save(promo));
    }

    // ── Shop: cancel a promotion ───────────────────────────────────────────────

    @Transactional
    public PromotionResponse cancel(Long promoId, Long shopId) {
        Promotion promo = promotionRepository.findById(promoId)
                .orElseThrow(() -> new RuntimeException("Promotion not found: " + promoId));

        if (!promo.getShop().getId().equals(shopId)) {
            throw new RuntimeException("This promotion does not belong to your shop.");
        }

        promo.setStatus("CANCELLED");
        return toResponse(promotionRepository.save(promo));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<PromotionResponse> getByShop(Long shopId) {
        return promotionRepository.findByShopIdOrderByCreatedAtDesc(shopId)
                .stream().map(this::toResponse).toList();
    }

    public List<PromotionResponse> getActiveByShop(Long shopId) {
        return promotionRepository.findActiveByShop(shopId, LocalDateTime.now())
                .stream().map(this::toResponse).toList();
    }

    public List<PromotionResponse> getAllActivePromotions() {
        return promotionRepository.findAllActive(LocalDateTime.now())
                .stream().map(this::toResponse).toList();
    }

    // ── Scheduler: auto-expire ─────────────────────────────────────────────────

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Colombo") // every hour
    @Transactional
    public void autoExpirePromotions() {
        List<Promotion> expired = promotionRepository.findExpired(LocalDateTime.now());
        for (Promotion p : expired) {
            p.setStatus("EXPIRED");
            promotionRepository.save(p);
        }
        if (!expired.isEmpty()) {
            log.info("[Promotions] Auto-expired {} promotion(s).", expired.size());
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private PromotionResponse toResponse(Promotion p) {
        long daysRemaining = 0;
        if ("ACTIVE".equals(p.getStatus()) && LocalDateTime.now().isBefore(p.getEndsAt())) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), p.getEndsAt());
        }

        return PromotionResponse.builder()
                .id(p.getId())
                .shopId(p.getShop().getId())
                .shopName(p.getShop().getName())
                .title(p.getTitle())
                .description(p.getDescription())
                .promoType(p.getPromoType())
                .promoValue(p.getPromoValue())
                .startsAt(p.getStartsAt())
                .endsAt(p.getEndsAt())
                .status(p.getStatus())
                .currentlyActive(p.isCurrentlyActive())
                .daysRemaining(daysRemaining)
                .build();
    }
}