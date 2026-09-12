package studying;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Customer {
    @Getter
    private final String fullName;

    @Getter
    @Setter
    private Car car;

    public Customer(String fullName) {
        this.fullName = fullName;
    }
}
