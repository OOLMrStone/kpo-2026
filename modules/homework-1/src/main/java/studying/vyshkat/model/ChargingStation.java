package studying.vyshkat.model;

import java.math.BigDecimal;

public final class ChargingStation extends Thing implements EnergyConsumer {
    private final BigDecimal dailyEnergyKwh;

    /** Расход станции учитывается отдельно от расхода транспорта. */
    public ChargingStation(String name, String number, BigDecimal energy) {
        super(name, number);
        dailyEnergyKwh = EnergyUsage.validate(energy);
    }

    @Override
    public BigDecimal dailyEnergyKwh() {
        return dailyEnergyKwh;
    }
}
