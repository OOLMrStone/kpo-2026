package studying.pos.domain;

import java.math.BigDecimal;

public interface MissingScorePolicy {
    BigDecimal missingScore(Criterion criterion);
}
