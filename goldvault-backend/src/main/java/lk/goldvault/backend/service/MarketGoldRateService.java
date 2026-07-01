package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.request.MarketRateRequest;
import lk.goldvault.backend.dto.response.GoldRateResponse;
import lk.goldvault.backend.dto.response.MarketRateResponse;
import lk.goldvault.backend.dto.response.RateComparisonResponse;
import lk.goldvault.backend.entity.MarketGoldRate;
import lk.goldvault.backend.repository.GoldRateRepository;
import lk.goldvault.backend.repository.MarketGoldRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketGoldRateService {

    private final MarketGoldRateRepository marketGoldRateRepository;
    private final GoldRateRepository       goldRateRepository;

    // ── Admin: publish a market reference rate ────────────────────────────────────

    @Transactional
    public MarketRateResponse publishMarketRate(MarketRateRequest request) {
        LocalDate date = request.getEffectiveDate() != null
                ? request.getEffectiveDate() : LocalDate.now();

        // Upsert: if a rate for this purity+date already exists, update it
        MarketGoldRate existing = marketGoldRateRepository
                .findFirstByPurityAndEffectiveDateOrderByCreatedAtDesc(request.getPurity(), date)
                .orElse(null);

        MarketGoldRate rate;
        if (existing != null) {
            existing.setRatePerGram(request.getRatePerGram());
            existing.setSource("MANUAL");
            rate = marketGoldRateRepository.save(existing);
        } else {
            rate = marketGoldRateRepository.save(MarketGoldRate.builder()
                    .purity(request.getPurity())
                    .ratePerGram(request.getRatePerGram())
                    .source("MANUAL")
                    .effectiveDate(date)
                    .build());
        }
        return toResponse(rate);
    }

    // ── Public: latest market rates (all purities) ────────────────────────────────

    public List<MarketRateResponse> getLatestMarketRates() {
        return marketGoldRateRepository.findLatestForAllPurities()
                .stream().map(this::toResponse).toList();
    }

    // ── Public: 30-day history for a purity (for chart) ──────────────────────────

    public List<MarketRateResponse> getRateHistory(String purity) {
        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusDays(29);
        return marketGoldRateRepository
                .findByPurityAndEffectiveDateBetweenOrderByEffectiveDateAsc(purity, from, to)
                .stream().map(this::toResponse).toList();
    }

    // ── Public: shop-vs-market comparison for a purity ───────────────────────────

    public RateComparisonResponse getComparisonForPurity(String purity) {
        BigDecimal marketRate = marketGoldRateRepository.findLatestForAllPurities()
                .stream()
                .filter(r -> r.getPurity().equalsIgnoreCase(purity))
                .findFirst()
                .map(MarketGoldRate::getRatePerGram)
                .orElse(null);

        List<GoldRateResponse> shopRates = goldRateRepository.findByIsActiveTrue()
                .stream()
                .filter(r -> r.getPurity().name().equalsIgnoreCase(purity))
                .map(r -> GoldRateResponse.builder()
                        .id(r.getId())
                        .shopId(r.getShop().getId())
                        .shopName(r.getShop().getName())
                        .purity(r.getPurity())
                        .ratePerGram(r.getRatePerGram())
                        .effectiveDate(r.getEffectiveDate())
                        .active(r.isActive())
                        .build())
                .toList();

        List<RateComparisonResponse.ShopRateEntry> entries = new ArrayList<>();
        for (GoldRateResponse sr : shopRates) {
            boolean above = false;
            double diffPct = 0;
            if (marketRate != null && marketRate.compareTo(BigDecimal.ZERO) > 0) {
                above = sr.getRatePerGram().compareTo(marketRate) > 0;
                diffPct = sr.getRatePerGram()
                        .subtract(marketRate)
                        .divide(marketRate, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }
            entries.add(RateComparisonResponse.ShopRateEntry.builder()
                    .shopId(sr.getShopId())
                    .shopName(sr.getShopName())
                    .ratePerGram(sr.getRatePerGram())
                    .aboveMarket(above)
                    .diffPercent(Math.round(diffPct * 10.0) / 10.0)
                    .build());
        }

        // Sort: highest rate first (best offer for sellers)
        entries.sort((a, b) -> b.getRatePerGram().compareTo(a.getRatePerGram()));

        return RateComparisonResponse.builder()
                .purity(purity)
                .marketRate(marketRate)
                .shopRates(entries)
                .build();
    }

    private MarketRateResponse toResponse(MarketGoldRate r) {
        return MarketRateResponse.builder()
                .id(r.getId())
                .purity(r.getPurity())
                .ratePerGram(r.getRatePerGram())
                .source(r.getSource())
                .effectiveDate(r.getEffectiveDate())
                .build();
    }
}