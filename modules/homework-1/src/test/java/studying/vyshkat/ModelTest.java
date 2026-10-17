package studying.vyshkat;

import java.math.BigDecimal;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import studying.vyshkat.model.Bicycle;
import studying.vyshkat.model.ChargingStation;
import studying.vyshkat.model.Helmet;
import studying.vyshkat.model.Scooter;
import studying.vyshkat.service.TechnicalServiceCenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Валидация модели и настоящий техосмотр")
class ModelTest {
    static IntStream validScores() {
        return IntStream.rangeClosed(1, 10);
    }

    @ParameterizedTest
    @MethodSource("validScores")
    void acceptsEveryValidScore(int score) {
        var bike = new Bicycle(" Bike ", " B-1 ", score, true);
        assertEquals(score, bike.beginnerScore());
        assertEquals("Bike", bike.name());
        assertEquals("B-1", bike.inventoryNumber());
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 11, Integer.MAX_VALUE})
    void rejectsInvalidScores(int score) {
        assertThrows(IllegalArgumentException.class, () -> new Bicycle("Bike", "B", score, true));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankIdentity(String value) {
        assertThrows(IllegalArgumentException.class, () -> new Helmet(value, "H"));
        assertThrows(IllegalArgumentException.class, () -> new Helmet("Helmet", value));
    }

    static Stream<BigDecimal> invalidEnergy() {
        return Stream.of(null, new BigDecimal("-0.01"), BigDecimal.valueOf(-100));
    }

    @ParameterizedTest
    @MethodSource("invalidEnergy")
    void rejectsInvalidEnergy(BigDecimal energy) {
        assertThrows(IllegalArgumentException.class, () -> new Scooter("S", "S", 6, true, energy));
        assertThrows(IllegalArgumentException.class, () -> new ChargingStation("C", "C", energy));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.001", "10.25"})
    void acceptsNonnegativeEnergy(String value) {
        var energy = new BigDecimal(value);
        assertEquals(energy, new Scooter("S", "S", 6, true, energy).dailyEnergyKwh());
        assertEquals(energy, new ChargingStation("C", "C", energy).dailyEnergyKwh());
    }

    @Test
    @DisplayName("Настоящий техосмотр различает исправность, а не простоту")
    void inspectionUsesCondition() {
        var center = new TechnicalServiceCenter();
        assertTrue(center.inspect(new Bicycle("Bike", "B", 1, true)));
        assertFalse(center.inspect(new Bicycle("Broken", "X", 10, false)));
    }
}
