package shelter.exception;

/**
 * Thrown when a requested entity cannot be found in the repository by its identifier.
 * This exception is used across all service and application layers whenever a lookup
 * by ID returns no result, allowing callers to handle the missing-entity case explicitly
 * rather than inspecting an empty {@link java.util.Optional}.
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code EntityNotFoundException} with the given detail message.
     * The message should identify both the entity type and the ID that was not found,
     * for example: {@code "Animal not found: abc-123"}.
     *
     * @param message a description of which entity was not found
     */
    public EntityNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code EntityNotFoundException} with the given detail message and cause.
     * Use this form when the not-found condition is detected by catching another exception.
     *
     * @param message a description of which entity was not found
     * @param cause   the underlying exception that triggered this one
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
