package studying.pos;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import studying.pos.adapters.ConsoleApplication;
import studying.pos.application.PosFacade;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        boolean strict = false;
        String demoPath = null;
        for (int index = 0; index < args.length; index++) {
            if (args[index].equals("--strict")) {
                strict = true;
            } else if (args[index].equals("--demo") && index + 1 < args.length) {
                demoPath = args[++index];
            } else {
                System.err.println("Запуск: homework-2 [--strict] [--demo путь-к-новому.csv]");
                return;
            }
        }
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources()
                    .addFirst(new MapPropertySource("cli", Map.of("pos.strict", strict)));
            context.register(ApplicationConfiguration.class);
            context.refresh();
            Readable input = demoPath == null ? new InputStreamReader(System.in, StandardCharsets.UTF_8)
                    : new StringReader(Demo.input(demoPath));
            PrintWriter output = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
            output.println(strict ? "Режим: все оценки обязательны." : "Режим: пропущенная оценка равна нулю.");
            new ConsoleApplication(context.getBean(PosFacade.class), input, output).run();
        }
    }
}
