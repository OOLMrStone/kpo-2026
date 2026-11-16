package studying.pos.adapters;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import studying.pos.application.ExportException;
import studying.pos.application.PosFacade;
import studying.pos.domain.Criterion;
import studying.pos.domain.FinalScore;
import studying.pos.domain.Id;

public final class ConsoleApplication {
    private final PosFacade facade;
    private final Scanner input;
    private final PrintWriter output;

    public ConsoleApplication(PosFacade facade, Readable input, PrintWriter output) {
        this.facade = facade;
        this.input = new Scanner(input);
        this.output = output;
    }

    public void run() {
        output.println("ПОС. Данные хранятся в памяти до завершения программы.");
        while (true) {
            output.println("1 Студент | 2 Критерий | 3 Списки | 4 Оценка/исправление | 5 Отмена");
            output.println("6 Оценки студента | 7 Итог | 8 Ведомость | 9 Экспорт CSV | 0 Выход");
            try {
                String command = read("Команда");
                if (command.equals("0")) {
                    output.println("До встречи!");
                    output.flush();
                    return;
                }
                execute(command);
            } catch (NoSuchElementException exception) {
                output.println("Ввод завершён. Незаконченная команда не сохранена.");
                output.flush();
                return;
            } catch (IllegalArgumentException | ExportException exception) {
                output.println("Ошибка: " + exception.getMessage());
            }
            output.flush();
        }
    }

    private void execute(String command) {
        switch (command) {
            case "1" -> {
                facade.addStudent(read("Код студента"), read("Имя"));
                output.println("Студент добавлен.");
            }
            case "2" -> {
                facade.addCriterion(read("Код критерия"), read("Название"), number("Максимальный балл"), number("Вес"));
                output.println("Критерий добавлен.");
            }
            case "3" -> lists();
            case "4" -> {
                facade.evaluate(read("Код студента"), read("Код критерия"), number("Балл"));
                output.println("Оценка сохранена.");
            }
            case "5" -> {
                facade.cancel(read("Код студента"), read("Код критерия"));
                output.println("Оценка отменена.");
            }
            case "6" -> evaluations();
            case "7" -> printScore(read("Код студента"));
            case "8" -> facade.results().forEach(row -> output.println(row.student().id() + " | "
                    + row.student().name() + " | " + format(row.score())));
            case "9" -> output.println("Экспортировано студентов: " + facade.export(read("Путь к новому CSV-файлу")));
            default -> output.println("Неизвестная команда.");
        }
    }

    private void lists() {
        output.println("Студенты: " + facade.students().size());
        facade.students().forEach(student -> output.println(student.id() + " | " + student.name()));
        output.println("Критерии: " + facade.criteria().size());
        facade.criteria().forEach(criterion -> output.println(criterion.id() + " | " + criterion.name()
                + " | максимум " + criterion.maxScore().toPlainString()
                + " | вес " + criterion.weight().toPlainString()));
    }

    private void evaluations() {
        Map<Id, BigDecimal> grades = facade.evaluations(read("Код студента"));
        if (facade.criteria().isEmpty()) {
            output.println("Рубрикатор пуст.");
        }
        for (Criterion criterion : facade.criteria()) {
            BigDecimal score = grades.get(criterion.id());
            output.println(criterion.id() + " | " + criterion.name() + " | "
                    + (score == null ? "не выставлена" : score.toPlainString()));
        }
    }

    private void printScore(String student) {
        output.println(student + " | " + format(facade.result(student)));
    }

    private String format(FinalScore score) {
        return "Итог: " + score.value().toPlainString() + " / 10; оценено " + score.evaluated() + "/" + score.total();
    }

    private String read(String prompt) {
        output.print(prompt + ": ");
        output.flush();
        return input.nextLine().strip();
    }

    private BigDecimal number(String prompt) {
        String value = read(prompt);
        // Ограничение ввода защищает консоль от чрезмерно больших чисел и экспонент.
        if (!value.matches("[+-]?[0-9]{1,9}([.,][0-9]{1,6})?")) {
            throw new IllegalArgumentException("Введите число: до 9 цифр до запятой и до 6 после.");
        }
        return new BigDecimal(value.replace(',', '.'));
    }
}
