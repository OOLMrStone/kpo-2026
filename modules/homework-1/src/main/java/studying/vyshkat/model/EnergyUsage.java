package studying.vyshkat.model;

import java.math.BigDecimal;

final class EnergyUsage {
    private EnergyUsage() {
    }

    static BigDecimal validate(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException("Расход энергии должен быть неотрицательным числом.");
        }
        return value;
    }
}
