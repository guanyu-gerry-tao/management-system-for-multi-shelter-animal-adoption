package shelter.exception;

/**
 * Thrown when a read or write operation on the underlying data store fails unexpectedly.
 * This exception wraps low-level I/O errors (such as {@link java.io.IOException}) so that
 * the repository layer can surface storage failures as unchecked exceptions without leaking
 * implementation details to the service or application layers.
 */
public class DataPersistenceException extends RuntimeException {

    /**
     * Constructs a new {@code DataPersistenceException} with the given detail message.
     * Use this form when the failure can be described without an underlying cause.
     *
     * @param message a description of the persistence operation that failed
     */
    public DataPersistenceException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code DataPersistenceException} with the given detail message and cause.
     * Use this form when wrapping a caught {@link java.io.IOException} or similar low-level error.
     *
     * @param message a description of the persistence operation that failed
     * @param cause   the underlying I/O exception that triggered this failure
     */
    public DataPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
