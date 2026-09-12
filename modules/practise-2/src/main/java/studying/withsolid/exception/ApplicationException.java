package studying.withsolid.exception;

import java.util.Objects;

/**
 * Application-level exception with a stable machine-readable error code.
 */
public final class ApplicationException extends RuntimeException {
    private final ApplicationErrorCode code;

    /**
     * Creates an application exception without an underlying cause.
     *
     * @param code stable error code
     * @param message human-readable error description
     */
    public ApplicationException(ApplicationErrorCode code, String message) {
        super(message);
        this.code = Objects.requireNonNull(code, "Error code must not be null");
    }

    /**
     * Creates an application exception and preserves its underlying cause.
     *
     * @param code stable error code
     * @param message human-readable error description
     * @param cause original failure
     */
    public ApplicationException(ApplicationErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = Objects.requireNonNull(code, "Error code must not be null");
    }

    /**
     * Returns the stable application error code.
     *
     * @return error code
     */
    public ApplicationErrorCode getCode() {
        return code;
    }
}
