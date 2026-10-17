package studying.vyshkat.model;

/** Название и номер задаются при создании и потом не меняются. */
public abstract class Asset implements InventoryItem {
    private final String name;
    private final String inventoryNumber;

    protected Asset(String name, String inventoryNumber) {
        this.name = requiredText(name, "Название");
        this.inventoryNumber = requiredText(inventoryNumber, "Инвентарный номер");
    }

    private static String requiredText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " не может быть пустым.");
        }
        return value.strip();
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final String inventoryNumber() {
        return inventoryNumber;
    }
}
