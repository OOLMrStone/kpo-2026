package studying.vyshkat.model;

import java.math.BigDecimal;

public final class Scooter extends ElectricTransport {
    public Scooter(String name, String number, int score, boolean operational, BigDecimal energy) {
        super(name, number, score, operational, energy);
    }
}
