package studying.vyshkat.console;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Scanner;
import studying.vyshkat.model.Bicycle;
import studying.vyshkat.model.ChargingStation;
import studying.vyshkat.model.ElectricBicycle;
import studying.vyshkat.model.Helmet;
import studying.vyshkat.model.InventoryItem;
import studying.vyshkat.model.Scooter;
import studying.vyshkat.model.Thing;
import studying.vyshkat.model.Transport;
import studying.vyshkat.service.FleetService;

/** После ошибки ввода программа возвращается в меню. */
public final class ConsoleApplication {
    private final FleetService fleet;
    private final Scanner input;
    private final PrintWriter output;

    /** Ввод и вывод передаются извне, чтобы консоль можно было проверить в тестах. */
    public ConsoleApplication(FleetService fleet, Readable input, PrintWriter output) {
        this.fleet = fleet;
        this.input = new Scanner(input);
        this.output = output;
    }

    public void run() {
        output.println("ВышКат. Доедем до дедлайна.");
        while (true) {
            output.println("1: принять транспорт | 2: добавить вещь | 3: отчёт");
            output.println("4: для новичков | 5: весь инвентарь | 0: выход");
            try {
                switch (read("Команда")) {
                    case "0" -> {
                        output.println("До встречи!");
                        output.flush();
                        return;
                    }
                    case "1" -> addTransport();
                    case "2" -> addThing();
                    case "3" -> report();
                    case "4" -> printItems(fleet.beginnerTransport());
                    case "5" -> printItems(fleet.inventory());
                    default -> output.println("Неизвестная команда.");
                }
            } catch (NoSuchElementException exception) {
                output.println("Ввод завершён.");
                output.flush();
                return;
            } catch (IllegalArgumentException exception) {
                output.println("Ошибка ввода: " + exception.getMessage());
            }
            output.flush();
        }
    }

    private void addTransport() {
        String type = read("Тип: 1 самокат, 2 велосипед, 3 электровелосипед");
        if (!type.equals("1") && !type.equals("2") && !type.equals("3")) {
            throw new IllegalArgumentException("Неизвестный тип транспорта.");
        }
        String name = read("Название");
        String number = read("Инвентарный номер");
        int score = Integer.parseInt(read("Простота для новичка (1-10)"));
        boolean operational = readCondition();
        Transport transport = switch (type) {
            case "1" -> new Scooter(name, number, score, operational, readEnergy());
            case "2" -> new Bicycle(name, number, score, operational);
            case "3" -> new ElectricBicycle(name, number, score, operational, readEnergy());
            default -> throw new IllegalStateException("Тип уже проверен.");
        };
        output.println(fleet.admit(transport) ? "Транспорт принят: " + number : "Техосмотр: отказ.");
    }

    private void addThing() {
        String type = read("Тип: 1 шлем, 2 зарядная станция");
        if (!type.equals("1") && !type.equals("2")) {
            throw new IllegalArgumentException("Неизвестный тип вещи.");
        }
        String name = read("Название");
        String number = read("Инвентарный номер");
        Thing thing = type.equals("1") ? new Helmet(name, number)
                : new ChargingStation(name, number, readEnergy());
        fleet.addThing(thing);
        output.println("Вещь добавлена: " + number);
    }

    private boolean readCondition() {
        return switch (read("Технически исправен? 1 да, 0 нет")) {
            case "1" -> true;
            case "0" -> false;
            default -> throw new IllegalArgumentException("Введите 1 или 0.");
        };
    }

    private BigDecimal readEnergy() {
        return new BigDecimal(read("Суточный расход, кВт*ч (например, 1.5)").replace(',', '.'));
    }

    private String read(String prompt) {
        output.println(prompt + ":");
        output.flush();
        return input.nextLine().strip();
    }

    private void report() {
        long transports = fleet.transportCount();
        output.println("Транспорт: " + transports);
        output.println("Вещи: " + (fleet.inventory().size() - transports));
        output.println("Всего на балансе: " + fleet.inventory().size());
        output.println("Энергия за сутки: " + fleet.totalDailyEnergyKwh().toPlainString() + " кВт*ч");
    }

    private void printItems(Iterable<? extends InventoryItem> items) {
        int count = 0;
        for (InventoryItem item : items) {
            output.println(item.inventoryNumber() + " | " + item.name());
            count++;
        }
        if (count == 0) {
            output.println("Список пуст.");
        }
    }
}
