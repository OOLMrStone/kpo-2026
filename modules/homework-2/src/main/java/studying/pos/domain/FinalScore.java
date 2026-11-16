package studying.pos.domain;

import java.math.BigDecimal;

public record FinalScore(BigDecimal value, int evaluated, int total) {
    public FinalScore {
        if (value == null || value.signum() < 0 || value.compareTo(BigDecimal.TEN) > 0
                || total <= 0 || evaluated < 0 || evaluated > total) {
            throw new DomainException("Некорректный итог.");
        }
    }
}
