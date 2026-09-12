package studying.withsolid.service.impl;

import studying.withsolid.exception.ApplicationException;
import studying.withsolid.model.Report;
import studying.withsolid.service.ReportSender;

import static studying.withsolid.exception.ApplicationErrorCode.VALIDATION_ERROR;

/**
 * Console-based imitation of report delivery by email.
 */
public final class EmailReportSender implements ReportSender {
    private static final String MISSING_REPORT_MESSAGE = "Report must not be null";
    private static final String MISSING_EMAIL_MESSAGE = "Recipient email must not be blank";
    private static final String DELIVERY_MESSAGE = "Sending report '%s' to email: %s%n";

    /**
     * Imitates delivery by printing the report title and recipient email.
     *
     * @param report report to deliver
     * @param email recipient email address
     * @throws ApplicationException when the report or email is missing
     */
    @Override
    public void send(Report report, String email) {
        if (report == null) {
            throw new ApplicationException(VALIDATION_ERROR, MISSING_REPORT_MESSAGE);
        }
        if (email == null || email.isBlank()) {
            throw new ApplicationException(VALIDATION_ERROR, MISSING_EMAIL_MESSAGE);
        }

        System.out.printf(DELIVERY_MESSAGE, report.title(), email);
    }
}
