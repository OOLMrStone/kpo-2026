package studying.withsolid.service;

import studying.withsolid.model.Report;

/**
 * Delivery protocol for reports.
 */
@FunctionalInterface
public interface ReportSender {
    /**
     * Delivers the supplied report to a recipient.
     *
     * @param report report to deliver
     * @param recipient recipient address understood by the implementation
     */
    void send(Report report, String recipient);
}
