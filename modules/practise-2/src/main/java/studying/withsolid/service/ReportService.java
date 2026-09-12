package studying.withsolid.service;

import studying.withsolid.model.Report;

import java.util.Objects;

/**
 * Facade for the report processing use case.
 *
 * <p>The concrete persistence and delivery mechanisms are selected when this
 * service is constructed, so the use case stays unchanged when new mechanisms
 * are introduced.</p>
 */
public final class ReportService {
    private final ReportSaver reportSaver;
    private final ReportSender reportSender;

    /**
     * Creates the report-processing facade.
     *
     * @param reportSaver persistence protocol implementation
     * @param reportSender delivery protocol implementation
     */
    public ReportService(ReportSaver reportSaver, ReportSender reportSender) {
        this.reportSaver = Objects.requireNonNull(reportSaver, "Report saver must not be null");
        this.reportSender = Objects.requireNonNull(reportSender, "Report sender must not be null");
    }

    /**
     * Saves a report and only then sends it to the recipient.
     *
     * @param report report to process
     * @param recipient delivery address
     */
    public void process(Report report, String recipient) {
        reportSaver.save(report);
        reportSender.send(report, recipient);
    }
}
