package studying.pos.adapters;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import studying.pos.domain.Gradebook;
import studying.pos.domain.GradebookRepository;
import studying.pos.domain.Id;

public final class InMemoryGradebookRepository implements GradebookRepository {
    private final Map<Id, Gradebook> books = new HashMap<>();

    @Override
    public Optional<Gradebook> find(Id id) {
        return Optional.ofNullable(books.get(id)).map(Gradebook::copy);
    }

    @Override
    public void save(Gradebook gradebook) {
        books.put(gradebook.id(), gradebook.copy());
    }
}
