package studying.pos.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Criterion(Id id, String name, BigDecimal maxScore, BigDecimal weight) {
    public Criterion {
        Objects.requireNonNull(id, "id");
        name = Text.required(name, "Название критерия");
        maxScore = positive(maxScore, "Максимум");
        weight = positive(weight, "Вес");
    }

    private static BigDecimal positive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new DomainException(field + " должен быть положительным.");
        }
        return value.stripTrailingZeros();
    }
}
