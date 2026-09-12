package studying.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studying.withsolid.exception.ApplicationErrorCode;
import studying.withsolid.exception.ApplicationException;
import studying.withsolid.model.Report;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportTest {
    @Test
    @DisplayName("Не позволяет создать отчёт с отрицательным показателем продаж")
    void rejectsNegativeSalesValue() {
        var exception = assertThrows(ApplicationException.class, () -> Report.builder()
                .title("Продажи")
                .date(LocalDate.of(2026, 9, 11))
                .time(LocalTime.NOON)
                .carsSold(-1)
                .motorcyclesSold(50)
                .build());

        assertEquals(ApplicationErrorCode.VALIDATION_ERROR, exception.getCode());
    }
}
