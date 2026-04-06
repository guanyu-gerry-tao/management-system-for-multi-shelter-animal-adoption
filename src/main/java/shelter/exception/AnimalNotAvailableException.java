package shelter.exception;

/**
 * Thrown when an operation requires an animal to be available for adoption,
 * but the animal has already been claimed by an adopter.
 * This exception extends {@link IllegalStateException} so that callers catching
 * the broader state-violation category still handle it correctly.
 */
public class AnimalNotAvailableException extends IllegalStateException {

    /**
     * Constructs a new {@code AnimalNotAvailableException} with the given detail message.
     * The message should identify the animal and explain why it is unavailable,
     * for example: {@code "Animal \"Rex\" is not available for adoption."}.
     *
     * @param message a description of why the animal is unavailable
     */
    public AnimalNotAvailableException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code AnimalNotAvailableException} with the given detail message and cause.
     *
     * @param message a description of why the animal is unavailable
     * @param cause   the underlying exception that triggered this one
     */
    public AnimalNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
