package studying.pos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import studying.pos.adapters.InMemoryGradebookRepository;
import studying.pos.application.PosFacade;
import studying.pos.application.ResultExporter;
import studying.pos.application.ResultRow;
import studying.pos.domain.DomainException;
import studying.pos.domain.FinalScoreService;
import studying.pos.domain.Gradebook;
import studying.pos.domain.Id;
import studying.pos.domain.ZeroMissingScore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTest {
    static PosFacade facade(ResultExporter exporter) {
        return new PosFacade(new InMemoryGradebookRepository(), new FinalScoreService(new ZeroMissingScore()),
                exporter, new Id("POS"));
    }

    @Test
    @DisplayName("Фасад выполняет полный сценарий и экспортирует свежий итог")
    void useCases() {
        List<ResultRow> exported = new ArrayList<>();
        PosFacade facade = facade((rows, destination) -> exported.addAll(rows));
        assertTrue(facade.students().isEmpty());
        facade.addStudent("S", "Анна");
        facade.addCriterion("C", "Работа", BigDecimal.TEN, BigDecimal.ONE);
        facade.evaluate("S", "C", BigDecimal.TEN);
        assertThrows(DomainException.class, () -> facade.evaluate("S", "C", new BigDecimal("12")));
        assertEquals(BigDecimal.TEN, facade.evaluations("S").get(new Id("C")).setScale(0));
        assertEquals(1, facade.export("ignored"));
        assertEquals(new BigDecimal("10.00"), exported.getFirst().score().value());
        facade.cancel("S", "C");
        assertEquals(new BigDecimal("0.00"), facade.result("S").value());
        assertEquals(1, facade.criteria().size());
    }

    @Test
    @DisplayName("Репозиторий хранит копии: изменения без save не сохраняются")
    void repositoryCopies() {
        InMemoryGradebookRepository repository = new InMemoryGradebookRepository();
        Gradebook book = DomainTest.book();
        repository.save(book);
        book.evaluate(new Id("S"), new Id("HW"), BigDecimal.TEN);
        Gradebook loaded = repository.find(book.id()).orElseThrow();
        assertTrue(loaded.evaluations(new Id("S")).isEmpty());
        loaded.evaluate(new Id("S"), new Id("HW"), BigDecimal.ONE);
        assertTrue(repository.find(book.id()).orElseThrow().evaluations(new Id("S")).isEmpty());
        assertTrue(repository.find(new Id("UNKNOWN")).isEmpty());
    }

    @Test
    @DisplayName("Spring собирает приложение и выбирает стратегию на краю")
    void springComposition() {
        for (boolean strict : new boolean[]{false, true}) {
            try (var context = new AnnotationConfigApplicationContext()) {
                context.getEnvironment().getPropertySources()
                        .addFirst(new MapPropertySource("test", Map.of("pos.strict", strict)));
                context.register(ApplicationConfiguration.class);
                context.refresh();
                PosFacade facade = context.getBean(PosFacade.class);
                facade.addStudent("S", "Анна");
                facade.addCriterion("C", "Работа", BigDecimal.TEN, BigDecimal.ONE);
                if (strict) {
                    assertThrows(DomainException.class, () -> facade.result("S"));
                } else {
                    assertEquals(new BigDecimal("0.00"), facade.result("S").value());
                }
            }
        }
    }
}
