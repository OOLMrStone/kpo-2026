package studying.vyshkat.model;

public interface InventoryItem {
    String name();

    /** Номер должен быть уникальным среди всех вещей и транспорта. */
    String inventoryNumber();
}
