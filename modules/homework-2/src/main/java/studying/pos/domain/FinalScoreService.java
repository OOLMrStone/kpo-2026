package studying.pos.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
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
        ExactFraction weighted = new ExactFraction(BigInteger.ZERO, BigInteger.ONE);
        BigDecimal weights = BigDecimal.ZERO;
        for (Criterion criterion : gradebook.rubric().criteria()) {
            Evaluation evaluation = evaluations.get(criterion.id());
            BigDecimal score = evaluation == null ? missingScorePolicy.missingScore(criterion) : evaluation.score();
            weighted = weighted.add(ExactFraction.ratio(score.multiply(criterion.weight()), criterion.maxScore()));
            weights = weights.add(criterion.weight());
        }
        BigDecimal numerator = new BigDecimal(weighted.numerator()).multiply(BigDecimal.TEN);
        BigDecimal denominator = new BigDecimal(weighted.denominator()).multiply(weights);
        BigDecimal result = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        return new FinalScore(result, evaluations.size(), gradebook.rubric().criteria().size());
    }
}
