package studying.pos.domain;

import java.util.Objects;

public record Student(Id id, String name) {
    public Student {
        Objects.requireNonNull(id, "id");
        name = Text.required(name, "Имя");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Student student && id.equals(student.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
