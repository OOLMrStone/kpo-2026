package studying.withsolid.exception;

/**
 * Codes of errors that can occur while a report is processed.
 */
public enum ApplicationErrorCode {
    /** Invalid input supplied by a caller. */
    VALIDATION_ERROR,

    /** A report could not be written to the configured storage. */
    FILE_WRITE_ERROR
}
