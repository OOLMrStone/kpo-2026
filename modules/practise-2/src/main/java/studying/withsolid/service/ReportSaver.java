package studying.withsolid.service;

import studying.withsolid.model.Report;

/**
 * Persistence protocol for reports.
 */
@FunctionalInterface
public interface ReportSaver {
    /**
     * Persists the supplied report.
     *
     * @param report report to persist
     */
    void save(Report report);
}
