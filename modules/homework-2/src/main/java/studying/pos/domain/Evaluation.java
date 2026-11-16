package studying.pos.domain;

import java.math.BigDecimal;

public final class Evaluation {
    private final Id criterionId;
    private final BigDecimal score;

    Evaluation(Criterion criterion, BigDecimal score) {
        if (score == null || score.signum() < 0 || score.compareTo(criterion.maxScore()) > 0) {
            throw new DomainException("Балл должен быть от 0 до " + criterion.maxScore().toPlainString() + ".");
        }
        this.criterionId = criterion.id();
        this.score = score.stripTrailingZeros();
    }

    public Id criterionId() {
        return criterionId;
    }

    public BigDecimal score() {
        return score;
    }
}
