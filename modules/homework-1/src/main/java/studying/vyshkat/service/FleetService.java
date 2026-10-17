package studying.vyshkat.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import studying.vyshkat.model.EnergyConsumer;
import studying.vyshkat.model.InventoryItem;
import studying.vyshkat.model.Thing;
import studying.vyshkat.model.Transport;

/** Хранит транспорт и вещи в памяти. Номера уникальны для всего реестра. */
public final class FleetService {
    private static final int BEGINNER_THRESHOLD = 6;
    private final ServiceCenter serviceCenter;
    private final Map<String, InventoryItem> inventory = new LinkedHashMap<>();

    public FleetService(ServiceCenter serviceCenter) {
        this.serviceCenter = Objects.requireNonNull(serviceCenter, "Нужен сервис техосмотра.");
    }

    /** При отказе или ошибке техосмотра транспорт не добавляется в реестр. */
    public boolean admit(Transport transport) {
        requireAvailableNumber(transport);
        if (!serviceCenter.inspect(transport)) {
            return false;
        }
        inventory.put(transport.inventoryNumber(), transport);
        return true;
    }

    /** Вещи добавляются без техосмотра. */
    public void addThing(Thing thing) {
        requireAvailableNumber(thing);
        inventory.put(thing.inventoryNumber(), thing);
    }

    private void requireAvailableNumber(InventoryItem item) {
        Objects.requireNonNull(item, "Нужен объект учёта.");
        if (inventory.containsKey(item.inventoryNumber())) {
            throw new IllegalArgumentException("Инвентарный номер уже занят: " + item.inventoryNumber());
        }
    }

    /** Возвращает копию списка: её нельзя изменить, новые объекты в ней не появятся. */
    public List<InventoryItem> inventory() {
        return List.copyOf(inventory.values());
    }

    public long transportCount() {
        return inventory.values().stream().filter(Transport.class::isInstance).count();
    }

    /** Считает расход всех потребителей энергии, включая зарядные станции. */
    public BigDecimal totalDailyEnergyKwh() {
        return inventory.values().stream()
                .filter(EnergyConsumer.class::isInstance)
                .map(EnergyConsumer.class::cast)
                .map(EnergyConsumer::dailyEnergyKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Transport> beginnerTransport() {
        return inventory.values().stream()
                .filter(Transport.class::isInstance)
                .map(Transport.class::cast)
                .filter(transport -> transport.beginnerScore() >= BEGINNER_THRESHOLD)
                .toList();
    }
}
