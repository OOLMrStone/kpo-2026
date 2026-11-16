package studying.pos.domain;

import java.util.Optional;

public interface GradebookRepository {
    Optional<Gradebook> find(Id id);

    void save(Gradebook gradebook);
}
