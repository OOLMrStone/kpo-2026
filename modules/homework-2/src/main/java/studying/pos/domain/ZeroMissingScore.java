package studying.pos.domain;

import java.math.BigDecimal;

public final class ZeroMissingScore implements MissingScorePolicy {
    @Override
    public BigDecimal missingScore(Criterion criterion) {
        return BigDecimal.ZERO;
    }
}
