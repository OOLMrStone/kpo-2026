package studying.pos.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

public final class FinalScoreService {
    private final MissingScorePolicy missingScorePolicy;

    public FinalScoreService(MissingScorePolicy missingScorePolicy) {
        this.missingScorePolicy = Objects.requireNonNull(missingScorePolicy, "missingScorePolicy");
    }

    public FinalScore calculate(Gradebook gradebook, Id studentId) {
        Map<Id, Evaluation> evaluations = gradebook.evaluations(studentId);
        if (gradebook.rubric().criteria().isEmpty()) {
            throw new DomainException("Добавьте хотя бы один критерий перед расчётом итога.");
        }
        BigDecimal weighted = BigDecimal.ZERO;
        BigDecimal weights = BigDecimal.ZERO;
        for (Criterion criterion : gradebook.rubric().criteria()) {
            Evaluation evaluation = evaluations.get(criterion.id());
            BigDecimal score = evaluation == null ? missingScorePolicy.missingScore(criterion) : evaluation.score();
            weighted = weighted.add(score.divide(criterion.maxScore(), MathContext.DECIMAL128)
                    .multiply(criterion.weight()));
            weights = weights.add(criterion.weight());
        }
        BigDecimal result = weighted.multiply(BigDecimal.TEN).divide(weights, 2, RoundingMode.HALF_UP);
        return new FinalScore(result, evaluations.size(), gradebook.rubric().criteria().size());
    }
}
