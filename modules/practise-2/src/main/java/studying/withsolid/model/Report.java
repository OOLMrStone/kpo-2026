package studying.withsolid.model;

import lombok.Builder;
import studying.withsolid.exception.ApplicationException;

import java.time.LocalDate;
import java.time.LocalTime;

import static studying.withsolid.exception.ApplicationErrorCode.VALIDATION_ERROR;

/**
 * Immutable sales report data independent of persistence and delivery details.
 *
 * @param title report title
 * @param date report date
 * @param time report time
 * @param carsSold number of cars sold
 * @param motorcyclesSold number of motorcycles sold
 */
@Builder
public record Report(
        String title,
        LocalDate date,
        LocalTime time,
        int carsSold,
        int motorcyclesSold
) {
    private static final String MISSING_TITLE_MESSAGE = "Report title must not be blank";
    private static final String MISSING_DATE_MESSAGE = "Report date must not be null";
    private static final String MISSING_TIME_MESSAGE = "Report time must not be null";
    private static final String NEGATIVE_SALES_MESSAGE = "Sales values must not be negative";

    /**
     * Validates report data when an immutable report is created.
     */
    public Report {
        if (title == null || title.isBlank()) {
            throw new ApplicationException(VALIDATION_ERROR, MISSING_TITLE_MESSAGE);
        }
        if (date == null) {
            throw new ApplicationException(VALIDATION_ERROR, MISSING_DATE_MESSAGE);
        }
        if (time == null) {
            throw new ApplicationException(VALIDATION_ERROR, MISSING_TIME_MESSAGE);
        }
        if (carsSold < 0 || motorcyclesSold < 0) {
            throw new ApplicationException(VALIDATION_ERROR, NEGATIVE_SALES_MESSAGE);
        }
    }

    /**
     * Formats this report as human-readable text.
     *
     * @return textual report representation
     */
    @Override
    public String toString() {
        return """
                %s
                Date: %s
                Time: %s
                --------------------------------
                Cars sold: %d
                Motorcycles sold: %d
                --------------------------------
                """.formatted(title, date, time, carsSold, motorcyclesSold);
    }
}
