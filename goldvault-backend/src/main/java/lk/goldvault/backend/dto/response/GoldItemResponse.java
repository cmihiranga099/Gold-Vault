package lk.goldvault.backend.dto.response;

import lk.goldvault.backend.enums.GoldPurity;
import lk.goldvault.backend.enums.GoldType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class GoldItemResponse {
    private Long id;
    private String description;
    private GoldType goldType;
    private BigDecimal weightGrams;
    private GoldPurity purity;
    private BigDecimal estimatedValue;
    private String photoUrl;
}