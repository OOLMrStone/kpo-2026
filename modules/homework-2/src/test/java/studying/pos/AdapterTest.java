package studying.pos;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import studying.pos.adapters.ConsoleApplication;
import studying.pos.adapters.CsvResultExporter;
import studying.pos.application.ExportException;
import studying.pos.application.PosFacade;
import studying.pos.application.ResultRow;
import studying.pos.domain.FinalScore;
import studying.pos.domain.Id;
import studying.pos.domain.Student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterTest {
    @TempDir
    Path directory;

    private String run(PosFacade facade, String input) {
        StringWriter output = new StringWriter();
        new ConsoleApplication(facade, new StringReader(input), new PrintWriter(output)).run();
        return output.toString();
    }

    @Test
    @DisplayName("CSV сохраняет кириллицу, запятые, кавычки и переводы строк")
    void csvEscaping() throws Exception {
        Path output = directory.resolve("result.csv");
        ResultRow row = new ResultRow(new Student(new Id("S,1"), "Анна, \"А\"\nПетрова"),
                new FinalScore(new BigDecimal("8.40"), 1, 2));
        new CsvResultExporter().export(List.of(row), output.toString());
        assertEquals("student_id,name,score,evaluated,total\r\n"
                + "\"S,1\",\"Анна, \"\"А\"\"\nПетрова\",8.40,1,2\r\n", Files.readString(output));
    }

    @Test
    @DisplayName("Ошибка экспорта не перезаписывает существующий файл")
    void exportErrors() throws Exception {
        CsvResultExporter exporter = new CsvResultExporter();
        Path existing = directory.resolve("existing.csv");
        Files.writeString(existing, "original");
        for (String destination : new String[]{null, " ", "\0", existing.toString(), directory.toString(),
                directory.resolve("missing/result.csv").toString()}) {
            assertThrows(ExportException.class, () -> exporter.export(List.of(), destination));
        }
        assertEquals("original", Files.readString(existing));
        Path empty = directory.resolve("empty.csv");
        exporter.export(List.of(), empty.toString());
        assertEquals("student_id,name,score,evaluated,total\r\n", Files.readString(empty));
    }

    @Test
    @DisplayName("Демо проходит через меню и экспортирует рассчитанную ведомость")
    void demo() throws Exception {
        Path csv = directory.resolve("demo.csv");
        String output = run(ApplicationTest.facade(new CsvResultExporter()), Demo.input(csv.toString()));
        for (String expected : new String[]{"Итог: 8.40", "Итог: 8.20", "не выставлена",
                "Балл должен быть от 0 до 10", "Критерия нет в рубрикаторе", "Экспортировано студентов: 2"}) {
            assertTrue(output.contains(expected), expected);
        }
        assertTrue(Files.readString(csv).contains("\"S1\",\"Анна\",8.40,2,2"));
        assertTrue(Files.readString(csv).contains("\"S2\",\"Борис\",8.20,2,2"));
    }

    @Test
    @DisplayName("Меню восстанавливается после ошибок ввода и принимает десятичную запятую")
    void consoleRecovery() {
        PosFacade facade = ApplicationTest.facade(new CsvResultExporter());
        String input = String.join("\n", "42", "1", "S", "Анна", "2", "BAD", "Ошибка", "NaN",
                "2", "C", "Работа", "10", "1", "4", "S", "C", "2,5", "7", "S",
                "9", directory.resolve("missing/x.csv").toString(), "0", "");
        String output = run(facade, input);
        assertTrue(output.contains("Неизвестная команда"));
        assertTrue(output.contains("Введите число"));
        assertTrue(output.contains("Итог: 2.50"));
        assertTrue(output.contains("Не удалось создать CSV"));
        assertEquals(1, facade.criteria().size());
    }

    @Test
    @DisplayName("Обрыв ввода в каждом поле команды не сохраняет частичные данные")
    void unfinishedInput() {
        for (String input : new String[]{"", "1\n", "1\nS\n", "2\n", "2\nC\n", "2\nC\nРабота\n",
                "2\nC\nРабота\n10\n"}) {
            PosFacade facade = ApplicationTest.facade((rows, destination) -> { });
            assertTrue(run(facade, input).contains("Ввод завершён"));
            assertTrue(facade.students().isEmpty());
            assertTrue(facade.criteria().isEmpty());
        }
    }

    @Test
    @DisplayName("Пустой рубрикатор виден в меню, обрыв ввода оценки сохраняет старое значение")
    void emptyAndIncompleteGrade() {
        PosFacade facade = ApplicationTest.facade((rows, destination) -> { });
        facade.addStudent("S", "Анна");
        assertTrue(run(facade, "6\nS\n0\n").contains("Рубрикатор пуст"));
        facade.addCriterion("C", "Работа", BigDecimal.TEN, BigDecimal.ONE);
        facade.evaluate("S", "C", BigDecimal.ONE);
        run(facade, "4\nS\nC\n");
        assertEquals(BigDecimal.ONE, facade.evaluations("S").get(new Id("C")));
        assertFalse(facade.results().isEmpty());
    }
}
