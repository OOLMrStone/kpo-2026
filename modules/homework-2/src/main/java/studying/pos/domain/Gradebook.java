package studying.pos.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Gradebook {
    private final Id id;
    private final Map<Id, Student> students = new LinkedHashMap<>();
    private final Map<Id, Map<Id, Evaluation>> evaluations = new LinkedHashMap<>();
    private Rubric rubric = new Rubric.Builder().build();

    public Gradebook(Id id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public Id id() {
        return id;
    }

    public Rubric rubric() {
        return rubric;
    }

    public void addStudent(Student student) {
        Objects.requireNonNull(student, "student");
        if (students.putIfAbsent(student.id(), student) != null) {
            throw new DomainException("Повторный код студента: " + student.id());
        }
        evaluations.put(student.id(), new LinkedHashMap<>());
    }

    public void addCriterion(Criterion criterion) {
        rubric = rubric.toBuilder().add(criterion).build();
    }

    public Student requireStudent(Id studentId) {
        Student student = students.get(studentId);
        if (student == null) {
            throw new DomainException("Студент не найден: " + studentId);
        }
        return student;
    }

    public List<Student> students() {
        return List.copyOf(students.values());
    }

    public Map<Id, Evaluation> evaluations(Id studentId) {
        requireStudent(studentId);
        return Map.copyOf(evaluations.get(studentId));
    }

    public void evaluate(Id studentId, Id criterionId, BigDecimal score) {
        requireStudent(studentId);
        Criterion criterion = rubric.require(criterionId);
        Evaluation evaluation = new Evaluation(criterion, score);
        evaluations.get(studentId).put(criterionId, evaluation);
    }

    public void cancel(Id studentId, Id criterionId) {
        requireStudent(studentId);
        rubric.require(criterionId);
        if (evaluations.get(studentId).remove(criterionId) == null) {
            throw new DomainException("По этому критерию оценка ещё не выставлена.");
        }
    }

    public Gradebook copy() {
        Gradebook copy = new Gradebook(id);
        copy.rubric = rubric;
        copy.students.putAll(students);
        evaluations.forEach((student, grades) -> copy.evaluations.put(student, new LinkedHashMap<>(grades)));
        return copy;
    }
}
