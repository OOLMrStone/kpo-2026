package studying.withsolid.service.impl;

import studying.withsolid.exception.ApplicationException;
import studying.withsolid.model.Report;
import studying.withsolid.service.ReportSaver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static studying.withsolid.exception.ApplicationErrorCode.FILE_WRITE_ERROR;
import static studying.withsolid.exception.ApplicationErrorCode.VALIDATION_ERROR;

/**
 * Saves reports as UTF-8 text files.
 */
public final class TextReportSaver implements ReportSaver {
    private static final Path DEFAULT_REPORT_DIRECTORY = Path.of("reports");
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("HH-mm-ss");
    private static final String FILE_NAME_TEMPLATE = "report-%s-%s.txt";
    private static final String MISSING_REPORT_MESSAGE = "Report must not be null";
    private static final String FILE_WRITE_MESSAGE = "Could not write report to file: ";

    private final Path reportDirectory;

    /**
     * Creates a saver that writes files to the default {@code reports} directory.
     */
    public TextReportSaver() {
        this(DEFAULT_REPORT_DIRECTORY);
    }

    /**
     * Creates a saver that writes files to the supplied directory.
     *
     * @param reportDirectory destination directory
     */
    public TextReportSaver(Path reportDirectory) {
        this.reportDirectory = Objects.requireNonNull(reportDirectory, "Report directory must not be null");
    }

    /**
     * Saves the report using its date and time in the generated file name.
     *
     * @param report report to persist
     * @throws ApplicationException when the report is missing or the file cannot be written
     */
    @Override
    public void save(Report report) {
        if (report == null) {
            throw new ApplicationException(VALIDATION_ERROR, MISSING_REPORT_MESSAGE);
        }

        var destination = destinationFor(report);
        try {
            Files.createDirectories(reportDirectory);
            Files.writeString(destination, report.toString());
        } catch (IOException exception) {
            throw new ApplicationException(FILE_WRITE_ERROR, FILE_WRITE_MESSAGE + destination, exception);
        }
    }

    private Path destinationFor(Report report) {
        var fileName = FILE_NAME_TEMPLATE.formatted(
                report.date(),
                report.time().format(FILE_TIME_FORMAT)
        );
        return reportDirectory.resolve(fileName);
    }
}
