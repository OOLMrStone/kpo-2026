package studying.vyshkat;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import studying.vyshkat.console.ConsoleApplication;
import studying.vyshkat.service.FleetService;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        if (args.length > 1 || (args.length == 1 && !args[0].equals("--demo"))) {
            throw new IllegalArgumentException("Запуск без аргументов или с --demo.");
        }
        try (var context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class)) {
            Readable input = args.length == 1 ? new StringReader(demoInput())
                    : new InputStreamReader(System.in, StandardCharsets.UTF_8);
            var output = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
            new ConsoleApplication(context.getBean(FleetService.class), input, output).run();
        }
    }

    /** Готовый ввод для демо: добавление объектов, ошибки, отчёты и выход. */
    public static String demoInput() {
        return String.join("\n",
                "3", "4", "5",
                "1", "1", "Самокат", "S-1", "6", "1", "1.2",
                "1", "2", "Велосипед", "B-1", "10", "1",
                "1", "3", "Электровелосипед", "E-1", "5", "1", "0.8",
                "1", "1", "Неисправный самокат", "S-2", "9", "0", "9.0",
                "2", "1", "Шлем", "H-1",
                "2", "2", "Зарядная станция", "C-1", "2.0",
                "2", "1", "Дубликат", "S-1",
                "1", "2", "Неверная простота", "B-2", "11", "1",
                "2", "2", "Неверный расход", "C-2", "-1",
                "неизвестная команда", "3", "4", "5", "0") + "\n";
    }
}
