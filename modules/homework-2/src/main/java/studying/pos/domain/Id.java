package studying.pos.domain;

public record Id(String value) {
    public Id {
        value = Text.required(value, "Идентификатор");
    }

    @Override
    public String toString() {
        return value;
    }
}
