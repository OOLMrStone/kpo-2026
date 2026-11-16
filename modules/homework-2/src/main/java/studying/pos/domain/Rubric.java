package studying.pos.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Rubric {
    private final Map<Id, Criterion> criteria;

    private Rubric(Map<Id, Criterion> criteria) {
        this.criteria = new LinkedHashMap<>(criteria);
    }

    public List<Criterion> criteria() {
        return List.copyOf(criteria.values());
    }

    public Criterion require(Id id) {
        Criterion criterion = criteria.get(id);
        if (criterion == null) {
            throw new DomainException("Критерия нет в рубрикаторе: " + id);
        }
        return criterion;
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        criteria.values().forEach(builder::add);
        return builder;
    }

    public static final class Builder {
        private final Map<Id, Criterion> criteria = new LinkedHashMap<>();

        public Builder add(Criterion criterion) {
            Objects.requireNonNull(criterion, "criterion");
            if (criteria.putIfAbsent(criterion.id(), criterion) != null) {
                throw new DomainException("Повторный код критерия: " + criterion.id());
            }
            return this;
        }

        public Rubric build() {
            return new Rubric(criteria);
        }
    }
}
