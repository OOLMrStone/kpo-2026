package studying.vyshkat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import studying.vyshkat.console.ConsoleApplication;
import studying.vyshkat.service.FleetService;
import studying.vyshkat.service.TechnicalServiceCenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Консольные сценарии")
class ConsoleApplicationTest {
    private String run(String script, FleetService fleet) {
        var text = new StringWriter();
        new ConsoleApplication(fleet, new StringReader(script), new PrintWriter(text)).run();
        return text.toString();
    }

    @Test
    @DisplayName("Демо покрывает все типы, отказы, ошибки, отчёты и выход")
    void demoCoversAllFeatures() {
        var fleet = new FleetService(new TechnicalServiceCenter());
        String text = run(Main.demoInput(), fleet);
        for (String expected : List.of("Список пуст.", "Транспорт принят: S-1", "Техосмотр: отказ.",
                "Инвентарный номер уже занят: S-1", "Простота должна быть от 1 до 10.",
                "Расход энергии должен быть неотрицательным числом.", "Неизвестная команда.",
                "Транспорт: 3", "Вещи: 2", "Всего на балансе: 5", "Энергия за сутки: 4.0 кВт*ч",
                "S-1 | Самокат", "H-1 | Шлем", "До встречи!")) {
            assertTrue(text.contains(expected), expected);
        }
        assertFalse(text.contains("S-2 |"));
        assertEquals(5, fleet.inventory().size());
        assertEquals(2, fleet.beginnerTransport().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1\n", "1\n1\nName\n", "2\n2\nStation\nC\n"})
    void eofDoesNotRegisterPartialObject(String script) {
        var fleet = new FleetService(transport -> true);
        assertTrue(run(script, fleet).contains("Ввод завершён."));
        assertTrue(fleet.inventory().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1\n9\n", "2\n9\n", "1\n2\nBike\nB\nabc\n",
        "1\n2\nBike\nB\n6\nyes\n", "2\n2\nStation\nC\nNaN\n",
        "2\n1\n \nH\n"
    })
    void invalidInputReturnsToMenu(String script) {
        var fleet = new FleetService(transport -> true);
        String text = run(script + "3\n0\n", fleet);
        assertTrue(text.contains("Ошибка ввода:"));
        assertTrue(text.contains("Всего на балансе: 0"));
        assertTrue(text.contains("До встречи!"));
    }

    @Test
    void decimalCommaIsAccepted() {
        var fleet = new FleetService(transport -> true);
        String text = run("2\n2\nStation\nC\n1,5\n3\n0\n", fleet);
        assertTrue(text.contains("Энергия за сутки: 1.5 кВт*ч"));
    }

    @Test
    @DisplayName("Точка входа запускает демо через настоящий контейнер")
    void mainDemoStarts() {
        var original = System.out;
        var bytes = new ByteArrayOutputStream();
        try (var output = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(output);
            Main.main(new String[]{"--demo"});
        } finally {
            System.setOut(original);
        }
        assertTrue(bytes.toString(StandardCharsets.UTF_8).contains("Всего на балансе: 5"));
    }

    @Test
    void invalidArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Main.main(new String[]{"unknown"}));
        assertThrows(IllegalArgumentException.class, () -> Main.main(new String[]{"--demo", "extra"}));
    }
}
