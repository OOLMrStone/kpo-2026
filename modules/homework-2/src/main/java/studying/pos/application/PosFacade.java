package studying.pos.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import studying.pos.domain.Criterion;
import studying.pos.domain.FinalScore;
import studying.pos.domain.FinalScoreService;
import studying.pos.domain.Gradebook;
import studying.pos.domain.GradebookRepository;
import studying.pos.domain.Id;
import studying.pos.domain.Student;

public final class PosFacade {
    private final GradebookRepository repository;
    private final FinalScoreService scores;
    private final ResultExporter exporter;
    private final Id gradebookId;

    public PosFacade(GradebookRepository repository, FinalScoreService scores,
            ResultExporter exporter, Id gradebookId) {
        this.repository = repository;
        this.scores = scores;
        this.exporter = exporter;
        this.gradebookId = gradebookId;
    }

    public void addStudent(String id, String name) {
        change(book -> book.addStudent(new Student(new Id(id), name)));
    }

    public void addCriterion(String id, String name, BigDecimal maximum, BigDecimal weight) {
        change(book -> book.addCriterion(new Criterion(new Id(id), name, maximum, weight)));
    }

    public void evaluate(String student, String criterion, BigDecimal score) {
        change(book -> book.evaluate(new Id(student), new Id(criterion), score));
    }

    public void cancel(String student, String criterion) {
        change(book -> book.cancel(new Id(student), new Id(criterion)));
    }

    public List<Student> students() {
        return book().students();
    }

    public List<Criterion> criteria() {
        return book().rubric().criteria();
    }

    public Map<Id, BigDecimal> evaluations(String student) {
        return book().evaluations(new Id(student)).entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().score()));
    }

    public FinalScore result(String student) {
        return scores.calculate(book(), new Id(student));
    }

    public List<ResultRow> results() {
        Gradebook snapshot = book();
        return snapshot.students().stream()
                .map(student -> new ResultRow(student, scores.calculate(snapshot, student.id()))).toList();
    }

    public int export(String destination) {
        List<ResultRow> rows = results();
        exporter.export(rows, destination);
        return rows.size();
    }

    private Gradebook book() {
        return repository.find(gradebookId).orElseGet(() -> new Gradebook(gradebookId));
    }

    private void change(Consumer<Gradebook> operation) {
        Gradebook book = book();
        operation.accept(book);
        repository.save(book);
    }
}
