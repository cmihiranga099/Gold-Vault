package lk.goldvault.backend.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lk.goldvault.backend.enums.GoldPurity;

@Converter(autoApply = true)
public class GoldPurityConverter implements AttributeConverter<GoldPurity, String> {

    @Override
    public String convertToDatabaseColumn(GoldPurity attribute) {
        return attribute == null ? null : attribute.getLabel();
    }

    @Override
    public GoldPurity convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (GoldPurity p : GoldPurity.values()) {
            if (p.getLabel().equals(dbData)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown gold purity: " + dbData);
    }
}