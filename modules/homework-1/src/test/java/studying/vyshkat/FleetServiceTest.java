package studying.vyshkat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import studying.vyshkat.model.Bicycle;
import studying.vyshkat.model.ChargingStation;
import studying.vyshkat.model.ElectricBicycle;
import studying.vyshkat.model.Helmet;
import studying.vyshkat.model.Scooter;
import studying.vyshkat.model.Transport;
import studying.vyshkat.service.FleetService;
import studying.vyshkat.service.TechnicalServiceCenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Учёт парка и инварианты приёмки")
class FleetServiceTest {
    @Test
    @DisplayName("Пустой парк: нулевой отчёт и пустые списки")
    void emptyFleet() {
        var fleet = new FleetService(transport -> true);
        assertEquals(0, fleet.transportCount());
        assertEquals(BigDecimal.ZERO, fleet.totalDailyEnergyKwh());
        assertTrue(fleet.inventory().isEmpty());
        assertTrue(fleet.beginnerTransport().isEmpty());
    }

    @Test
    @DisplayName("Техосмотр получает тот же объект ровно один раз до добавления")
    void inspectionRunsBeforeRegistration() {
        List<Transport> inspected = new ArrayList<>();
        FleetService[] reference = new FleetService[1];
        var fleet = new FleetService(transport -> {
            assertTrue(reference[0].inventory().isEmpty());
            inspected.add(transport);
            return true;
        });
        reference[0] = fleet;
        var bicycle = new Bicycle("Bike", "B-1", 6, true);
        assertTrue(fleet.admit(bicycle));
        assertEquals(List.of(bicycle), inspected);
        assertEquals(List.of(bicycle), fleet.inventory());
    }

    @Test
    @DisplayName("Отказ заглушки не меняет парк и не резервирует номер")
    void rejectedTransportDoesNotEnterAnyReport() {
        var fleet = new FleetService(transport -> false);
        assertFalse(fleet.admit(new Scooter("Scooter", "S-1", 10, true, BigDecimal.TEN)));
        assertEquals(0, fleet.transportCount());
        assertEquals(BigDecimal.ZERO, fleet.totalDailyEnergyKwh());
        assertTrue(fleet.beginnerTransport().isEmpty());
        fleet.addThing(new Helmet("Helmet", "S-1"));
        assertEquals(1, fleet.inventory().size());
    }

    @Test
    @DisplayName("Сбой техосмотра не оставляет частично принятую технику")
    void inspectionFailureIsAtomic() {
        var failure = new IllegalStateException("Service unavailable");
        var fleet = new FleetService(transport -> { throw failure; });
        var bike = new Bicycle("Bike", "B-1", 6, true);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> fleet.admit(bike)));
        assertTrue(fleet.inventory().isEmpty());
    }

    @ParameterizedTest(name = "Простота {0}: для новичка = {1}")
    @CsvSource({"1,false", "5,false", "6,true", "10,true"})
    void beginnerBoundary(int score, boolean expected) {
        var fleet = new FleetService(transport -> true);
        fleet.admit(new Bicycle("Bike", "B-1", score, true));
        fleet.addThing(new Helmet("Helmet", "H-1"));
        assertEquals(expected ? 1 : 0, fleet.beginnerTransport().size());
    }

    @Test
    @DisplayName("Сумма включает электровещи, но не отклонённую технику")
    void mixedFleetEnergyAndCounts() {
        var fleet = new FleetService(new TechnicalServiceCenter());
        fleet.admit(new Scooter("Scooter", "S", 6, true, new BigDecimal("0.1")));
        fleet.admit(new ElectricBicycle("E-bike", "E", 5, true, new BigDecimal("0.2")));
        fleet.admit(new Bicycle("Bike", "B", 10, true));
        fleet.admit(new Scooter("Broken", "X", 10, false, BigDecimal.TEN));
        fleet.addThing(new Helmet("Helmet", "H"));
        fleet.addThing(new ChargingStation("Station", "C", new BigDecimal("0.3")));
        assertEquals(3, fleet.transportCount());
        assertEquals(5, fleet.inventory().size());
        assertEquals(new BigDecimal("0.6"), fleet.totalDailyEnergyKwh());
        assertEquals(List.of("S", "B"), fleet.beginnerTransport().stream()
                .map(Transport::inventoryNumber).toList());
    }

    @Test
    @DisplayName("Номер уникален между вещами и транспортом в обоих направлениях")
    void duplicateNumbersAreRejectedBeforeInspection() {
        List<Transport> inspected = new ArrayList<>();
        var fleet = new FleetService(transport -> {
            inspected.add(transport);
            return true;
        });
        var bike = new Bicycle("Bike", "B", 6, true);
        fleet.admit(bike);
        assertThrows(IllegalArgumentException.class, () -> fleet.admit(bike));
        assertThrows(IllegalArgumentException.class, () -> fleet.addThing(new Helmet("Helmet", " B ")));
        fleet.addThing(new Helmet("Helmet", "H"));
        assertThrows(IllegalArgumentException.class,
                () -> fleet.admit(new Bicycle("Bike", "H", 6, true)));
        assertThrows(IllegalArgumentException.class, () -> fleet.addThing(new Helmet("Another", "H")));
        assertEquals(List.of(bike), inspected);
        assertEquals(2, fleet.inventory().size());
    }

    @Test
    @DisplayName("Вещи не отправляются в сервис техосмотра транспорта")
    void thingsDoNotRequireTransportInspection() {
        var fleet = new FleetService(transport -> { throw new AssertionError("Unexpected inspection"); });
        fleet.addThing(new Helmet("Helmet", "H"));
        assertEquals(1, fleet.inventory().size());
    }

    @Test
    @DisplayName("Выданные снимки коллекций нельзя изменить и они не обновляются задним числом")
    void collectionsAreImmutableSnapshots() {
        var fleet = new FleetService(transport -> true);
        var bike = new Bicycle("Bike", "B", 6, true);
        fleet.admit(bike);
        var snapshot = fleet.inventory();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class, () -> fleet.beginnerTransport().clear());
        fleet.addThing(new Helmet("Helmet", "H"));
        assertEquals(1, snapshot.size());
    }

    @Test
    @DisplayName("Новый вид транспорта работает без изменения сервиса")
    void extensionUsesExistingContracts() {
        var fleet = new FleetService(new TechnicalServiceCenter());
        Transport tricycle = new Transport("Tricycle", "T", 8, true) { };
        assertTrue(fleet.admit(tricycle));
        assertEquals(List.of(tricycle), fleet.beginnerTransport());
        assertEquals(1, fleet.transportCount());
    }

    @Test
    @DisplayName("Отсутствующие зависимости и объекты отклоняются")
    void nullInputs() {
        assertThrows(NullPointerException.class, () -> new FleetService(null));
        var fleet = new FleetService(transport -> true);
        assertThrows(NullPointerException.class, () -> fleet.admit(null));
        assertThrows(NullPointerException.class, () -> fleet.addThing(null));
        assertTrue(fleet.inventory().isEmpty());
    }
}
