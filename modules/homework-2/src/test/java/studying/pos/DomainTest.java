package studying.pos;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import studying.pos.domain.Criterion;
import studying.pos.domain.DomainException;
import studying.pos.domain.FinalScore;
import studying.pos.domain.FinalScoreService;
import studying.pos.domain.Gradebook;
import studying.pos.domain.Id;
import studying.pos.domain.RequireCompleteScores;
import studying.pos.domain.Rubric;
import studying.pos.domain.Student;
import studying.pos.domain.ZeroMissingScore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainTest {
    static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    static Criterion criterion(String id, String maximum, String weight) {
        return new Criterion(new Id(id), id, decimal(maximum), decimal(weight));
    }

    static Gradebook book() {
        Gradebook book = new Gradebook(new Id("POS"));
        book.addStudent(new Student(new Id("S"), "Анна"));
        book.addCriterion(criterion("HW", "20", "40"));
        book.addCriterion(criterion("EXAM", "10", "60"));
        return book;
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("Имена и идентификаторы не могут быть пустыми")
    void requiredText(String value) {
        assertThrows(DomainException.class, () -> new Id(value));
        assertThrows(DomainException.class, () -> new Student(new Id("S"), value));
        assertThrows(DomainException.class, () -> new Criterion(new Id("C"), value, BigDecimal.ONE, BigDecimal.ONE));
    }

    @Test
    @DisplayName("Студент сохраняет идентичность по коду, значения нормализуются")
    void identityAndValues() {
        Student first = new Student(new Id(" S "), " Анна ");
        Student renamed = new Student(new Id("S"), "Другое имя");
        assertEquals(first, renamed);
        assertEquals(first.hashCode(), renamed.hashCode());
        assertEquals("Анна", first.name());
        assertEquals("S", first.id().toString());
        assertEquals(criterion("C", "10.0", "1.00"), criterion("C", "10", "1"));
    }

    @Test
    @DisplayName("Максимум и вес строго положительны")
    void positiveParameters() {
        for (BigDecimal value : new BigDecimal[]{null, BigDecimal.ZERO, decimal("-1")}) {
            assertThrows(DomainException.class, () -> new Criterion(new Id("C"), "C", value, BigDecimal.ONE));
            assertThrows(DomainException.class, () -> new Criterion(new Id("C"), "C", BigDecimal.ONE, value));
        }
    }

    @Test
    @DisplayName("Builder собирает независимые рубрикаторы и отклоняет повторный код")
    void builderSnapshots() {
        Rubric.Builder builder = new Rubric.Builder().add(criterion("A", "10", "1"));
        Rubric first = builder.build();
        builder.add(criterion("B", "20", "2"));
        assertEquals(1, first.criteria().size());
        assertEquals(2, builder.build().criteria().size());
        assertThrows(DomainException.class, () -> builder.add(criterion("A", "30", "3")));
        assertThrows(UnsupportedOperationException.class, () -> first.criteria().clear());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "10", "4.25"})
    @DisplayName("Допустимы ноль, максимум и дробный балл")
    void allowedScores(String score) {
        Gradebook book = book();
        book.evaluate(new Id("S"), new Id("EXAM"), decimal(score));
        assertEquals(0, decimal(score).compareTo(book.evaluations(new Id("S")).get(new Id("EXAM")).score()));
    }

    @Test
    @DisplayName("Ошибки оценивания не меняют ведомость")
    void failedChangesAreAtomic() {
        Gradebook book = book();
        book.evaluate(new Id("S"), new Id("EXAM"), decimal("9"));
        for (BigDecimal score : new BigDecimal[]{null, decimal("-0.01"), decimal("10.01")}) {
            assertThrows(DomainException.class, () -> book.evaluate(new Id("S"), new Id("EXAM"), score));
        }
        assertThrows(DomainException.class, () -> book.evaluate(new Id("S"), new Id("MISSING"), BigDecimal.ONE));
        assertThrows(DomainException.class, () -> book.evaluate(new Id("MISSING"), new Id("EXAM"), BigDecimal.ONE));
        assertThrows(DomainException.class, () -> book.addStudent(new Student(new Id("S"), "Дубликат")));
        assertThrows(DomainException.class, () -> book.addCriterion(criterion("EXAM", "100", "1")));
        assertEquals(decimal("9"), book.evaluations(new Id("S")).get(new Id("EXAM")).score());
        assertEquals(1, book.students().size());
        assertEquals(2, book.rubric().criteria().size());
    }

    @Test
    @DisplayName("Итог учитывает максимумы и веса, исправление и отмену")
    void weightedResultTracksChanges() {
        Gradebook book = book();
        FinalScoreService service = new FinalScoreService(new ZeroMissingScore());
        book.evaluate(new Id("S"), new Id("HW"), decimal("15"));
        book.evaluate(new Id("S"), new Id("EXAM"), decimal("9"));
        assertEquals(new FinalScore(decimal("8.40"), 2, 2), service.calculate(book, new Id("S")));
        book.evaluate(new Id("S"), new Id("EXAM"), decimal("7"));
        assertEquals(decimal("7.20"), service.calculate(book, new Id("S")).value());
        book.cancel(new Id("S"), new Id("HW"));
        assertEquals(new FinalScore(decimal("4.20"), 1, 2), service.calculate(book, new Id("S")));
        assertThrows(DomainException.class, () -> book.cancel(new Id("S"), new Id("HW")));
        book.addCriterion(criterion("NEW", "5", "100"));
        assertEquals(decimal("2.10"), service.calculate(book, new Id("S")).value());
    }

    @Test
    @DisplayName("Строгая стратегия запрещает неполный итог, нулевая считает пропуски нулём")
    void missingPolicies() {
        Gradebook book = book();
        FinalScoreService strict = new FinalScoreService(new RequireCompleteScores());
        assertThrows(DomainException.class, () -> strict.calculate(book, new Id("S")));
        assertEquals(decimal("0.00"),
                new FinalScoreService(new ZeroMissingScore()).calculate(book, new Id("S")).value());
        book.evaluate(new Id("S"), new Id("HW"), decimal("20"));
        book.evaluate(new Id("S"), new Id("EXAM"), decimal("10"));
        assertEquals(decimal("10.00"), strict.calculate(book, new Id("S")).value());
        Gradebook empty = new Gradebook(new Id("EMPTY"));
        empty.addStudent(new Student(new Id("S"), "Анна"));
        assertThrows(DomainException.class, () -> strict.calculate(empty, new Id("S")));
    }

    @Test
    @DisplayName("Итог округляется один раз до сотых")
    void rounding() {
        Gradebook book = new Gradebook(new Id("P"));
        book.addStudent(new Student(new Id("S"), "Студент"));
        book.addCriterion(criterion("C", "3", "7"));
        book.evaluate(new Id("S"), new Id("C"), decimal("2"));
        assertEquals(decimal("6.67"),
                new FinalScoreService(new ZeroMissingScore()).calculate(book, new Id("S")).value());
    }

    @ParameterizedTest
    @CsvSource({
        "0.999999, 3, 3, 13, 0.62",
        "1, 3, 3, 13, 0.63",
        "1.000001, 3, 3, 13, 0.63",
        "1, 3, 0.3, 1.3, 0.63",
        "10, 30, 30, 130, 0.63",
        "0, 3, 3, 13, 0.00"
    })
    @DisplayName("Точная граница половины сотой и соседние значения округляются математически")
    void exactRoundingBoundary(String score, String maximum, String weight, String otherWeight, String expected) {
        Gradebook book = new Gradebook(new Id("P"));
        book.addStudent(new Student(new Id("S"), "Студент"));
        book.addCriterion(criterion("A", maximum, weight));
        book.addCriterion(criterion("B", "10", otherWeight));
        book.evaluate(new Id("S"), new Id("A"), decimal(score));
        book.evaluate(new Id("S"), new Id("B"), BigDecimal.ZERO);
        assertEquals(decimal(expected),
                new FinalScoreService(new ZeroMissingScore()).calculate(book, new Id("S")).value());
    }

    @Test
    @DisplayName("Несколько периодических дробей складываются без промежуточного округления")
    void exactSumOfFractions() {
        Gradebook book = new Gradebook(new Id("P"));
        book.addStudent(new Student(new Id("S"), "Студент"));
        book.addCriterion(criterion("A", "3", "1"));
        book.addCriterion(criterion("B", "6", "1"));
        book.addCriterion(criterion("C", "10", "6"));
        book.evaluate(new Id("S"), new Id("A"), BigDecimal.ONE);
        book.evaluate(new Id("S"), new Id("B"), BigDecimal.ONE);
        assertEquals(decimal("0.63"),
                new FinalScoreService(new ZeroMissingScore()).calculate(book, new Id("S")).value());
    }

    @Test
    @DisplayName("Округление у верхней границы шкалы даёт десять")
    void roundingToTen() {
        Gradebook book = new Gradebook(new Id("P"));
        book.addStudent(new Student(new Id("S"), "Студент"));
        book.addCriterion(criterion("A", "2000", "0.01"));
        book.evaluate(new Id("S"), new Id("A"), decimal("1999"));
        assertEquals(decimal("10.00"),
                new FinalScoreService(new ZeroMissingScore()).calculate(book, new Id("S")).value());
    }

    @Test
    @DisplayName("Списки и снимки ведомости нельзя использовать для обхода инвариантов")
    void snapshots() {
        Gradebook book = book();
        List<Student> students = book.students();
        Gradebook copy = book.copy();
        book.evaluate(new Id("S"), new Id("HW"), decimal("20"));
        book.addStudent(new Student(new Id("B"), "Борис"));
        assertEquals(1, students.size());
        assertEquals(0, copy.evaluations(new Id("S")).size());
        assertThrows(UnsupportedOperationException.class, () -> students.clear());
        assertThrows(UnsupportedOperationException.class, () -> book.evaluations(new Id("S")).clear());
    }

    @Test
    @DisplayName("Объект итога не допускает некорректные значения")
    void finalScoreValidation() {
        assertThrows(DomainException.class, () -> new FinalScore(null, 0, 1));
        assertThrows(DomainException.class, () -> new FinalScore(decimal("-1"), 0, 1));
        assertThrows(DomainException.class, () -> new FinalScore(decimal("11"), 0, 1));
        assertThrows(DomainException.class, () -> new FinalScore(BigDecimal.ZERO, 0, 0));
        assertThrows(DomainException.class, () -> new FinalScore(BigDecimal.ZERO, -1, 1));
        assertThrows(DomainException.class, () -> new FinalScore(BigDecimal.ZERO, 2, 1));
    }
}
