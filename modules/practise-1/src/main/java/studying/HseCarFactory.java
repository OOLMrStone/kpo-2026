package studying;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ToString
@RequiredArgsConstructor
public class HseCarFactory {
    private int carNumber = 0;
    private final List<Car> cars = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void addCar(int engineSize) {
        cars.add(new Car(carNumber++, engineSize));
    }

    /**
     * Assigns available cars to waiting customers and liquidates unsold stock.
     */
    public void saleCar() {
        customers.stream()
                .filter(customer -> Objects.isNull(customer.getCar()))
                .forEach(customer -> {
                    if (!cars.isEmpty()) {
                        customer.setCar(cars.removeFirst());
                    }
                });
        cars.clear();
    }

    public void printCars() {
        cars.forEach(System.out::println);
    }

    public void printCustomers() {
        customers.forEach(System.out::println);
    }
}
