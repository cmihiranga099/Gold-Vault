package lk.goldvault.backend.service;

import lk.goldvault.backend.dto.response.RevenueReportResponse;
import lk.goldvault.backend.entity.PawnShop;
import lk.goldvault.backend.entity.Payment;
import lk.goldvault.backend.repository.PawnShopRepository;
import lk.goldvault.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueReportService {

    private final PaymentRepository paymentRepository;
    private final PawnShopRepository pawnShopRepository;

    @Value("${app.commission.rate-percent}")
    private BigDecimal commissionRatePercent;

    public RevenueReportResponse generateReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);

        List<Payment> payments = paymentRepository.findByPaymentDateBetween(start, end);

        BigDecimal totalVolume = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommission = calculateCommission(totalVolume);

        Map<Long, List<Payment>> byShop = payments.stream()
                .collect(Collectors.groupingBy(p -> p.getTicket().getShop().getId()));

        List<RevenueReportResponse.ShopRevenueBreakdown> breakdowns = byShop.entrySet().stream()
                .map(entry -> {
                    Long shopId = entry.getKey();
                    List<Payment> shopPayments = entry.getValue();
                    BigDecimal shopVolume = shopPayments.stream()
                            .map(Payment::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    PawnShop shop = shopPayments.get(0).getTicket().getShop();

                    return RevenueReportResponse.ShopRevenueBreakdown.builder()
                            .shopId(shopId)
                            .shopName(shop.getName())
                            .paymentVolume(shopVolume)
                            .commissionOwed(calculateCommission(shopVolume))
                            .paymentCount(shopPayments.size())
                            .build();
                })
                .sorted((a, b) -> b.getPaymentVolume().compareTo(a.getPaymentVolume()))
                .toList();

        long activeShopCount = pawnShopRepository.findByStatus(
                lk.goldvault.backend.enums.ShopStatus.ACTIVE).size();

        return RevenueReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalPaymentVolume(totalVolume)
                .totalCommission(totalCommission)
                .totalPaymentCount(payments.size())
                .activeShopCount(activeShopCount)
                .byShop(breakdowns)
                .build();
    }

    private BigDecimal calculateCommission(BigDecimal volume) {
        return volume
                .multiply(commissionRatePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}