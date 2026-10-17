package studying.vyshkat.model;

import java.math.BigDecimal;

public abstract class ElectricTransport extends Transport implements EnergyConsumer {
    private final BigDecimal dailyEnergyKwh;

    protected ElectricTransport(String name, String number, int score, boolean operational, BigDecimal energy) {
        super(name, number, score, operational);
        dailyEnergyKwh = EnergyUsage.validate(energy);
    }

    @Override
    public final BigDecimal dailyEnergyKwh() {
        return dailyEnergyKwh;
    }
}
