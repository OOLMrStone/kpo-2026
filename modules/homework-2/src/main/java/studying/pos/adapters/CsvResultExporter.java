package studying.pos.adapters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import studying.pos.application.ExportException;
import studying.pos.application.ResultExporter;
import studying.pos.application.ResultRow;

public final class CsvResultExporter implements ResultExporter {
    @Override
    public void export(List<ResultRow> rows, String destination) {
        if (destination == null || destination.isBlank()) {
            throw new ExportException("Укажите путь к CSV-файлу.", null);
        }
        StringBuilder csv = new StringBuilder("student_id,name,score,evaluated,total\r\n");
        for (ResultRow row : rows) {
            csv.append(cell(row.student().id().value())).append(',')
                    .append(cell(row.student().name())).append(',')
                    .append(row.score().value().toPlainString()).append(',')
                    .append(row.score().evaluated()).append(',').append(row.score().total()).append("\r\n");
        }
        try {
            Files.writeString(Path.of(destination), csv, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE_NEW);
        } catch (IOException | InvalidPathException | SecurityException exception) {
            throw new ExportException("Не удалось создать CSV: проверьте путь. Файл не должен существовать.",
                    exception);
        }
    }

    private String cell(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
