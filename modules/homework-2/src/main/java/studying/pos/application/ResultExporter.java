package studying.pos.application;

import java.util.List;

public interface ResultExporter {
    void export(List<ResultRow> rows, String destination);
}
