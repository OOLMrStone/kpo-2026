package studying.pos.domain;

import java.math.BigDecimal;

public final class RequireCompleteScores implements MissingScorePolicy {
    @Override
    public BigDecimal missingScore(Criterion criterion) {
        throw new DomainException("Итог не готов: нет оценки по критерию " + criterion.id() + ".");
    }
}
