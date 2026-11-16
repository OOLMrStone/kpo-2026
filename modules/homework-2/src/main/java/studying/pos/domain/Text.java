package studying.pos.domain;

final class Text {
    private Text() { }

    static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException(field + " не может быть пустым.");
        }
        return value.strip();
    }
}
