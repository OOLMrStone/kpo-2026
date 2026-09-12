package studying;

import lombok.Getter;
import lombok.ToString;

@ToString
public class Car {
    private final Engine engine;

    @Getter
    private final int VIN;

    public Car(int VIN, int engineSize) {
        this.VIN = VIN;
        engine = new Engine(engineSize);
    }
}
