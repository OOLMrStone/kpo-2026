package studying.vyshkat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import studying.vyshkat.model.Bicycle;
import studying.vyshkat.service.FleetService;
import studying.vyshkat.service.ServiceCenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Сборка зависимостей контейнером Spring")
class ContainerTest {
    @Test
    @DisplayName("Рабочая конфигурация собирает сервис и применяет настоящий техосмотр")
    void productionContext() {
        try (var context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class)) {
            var fleet = context.getBean(FleetService.class);
            assertFalse(fleet.admit(new Bicycle("Broken", "X", 10, false)));
            assertTrue(fleet.admit(new Bicycle("Bike", "B", 6, true)));
            assertEquals(1, fleet.transportCount());
        }
    }

    @Test
    @DisplayName("Контейнер внедряет тестовый отказ вместо рабочего техосмотра")
    void containerInjectsFakeInspection() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean("testInspection", ServiceCenter.class,
                    () -> transport -> false, definition -> definition.setPrimary(true));
            context.register(ApplicationConfiguration.class);
            context.refresh();
            var fleet = context.getBean(FleetService.class);
            assertFalse(fleet.admit(new Bicycle("Healthy", "B", 10, true)));
            assertTrue(fleet.inventory().isEmpty());
        }
    }
}
