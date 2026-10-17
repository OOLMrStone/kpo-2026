package studying.vyshkat.model;

import java.math.BigDecimal;

public interface EnergyConsumer {
    /** Суточный расход в кВт*ч. Значение не может быть null или отрицательным. */
    BigDecimal dailyEnergyKwh();
}
