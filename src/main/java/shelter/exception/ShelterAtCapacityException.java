package shelter.exception;

/**
 * Thrown when an animal cannot be added to a shelter because the shelter has already
 * reached its maximum capacity. This exception extends {@link IllegalStateException}
 * so that callers catching the broader state-violation category still handle it correctly.
 */
public class ShelterAtCapacityException extends IllegalStateException {

    /**
     * Constructs a new {@code ShelterAtCapacityException} with the given detail message.
     * The message should identify the shelter and its capacity limit,
     * for example: {@code "Shelter \"Happy Paws\" is at full capacity (20)."}.
     *
     * @param message a description of the capacity constraint that was violated
     */
    public ShelterAtCapacityException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ShelterAtCapacityException} with the given detail message and cause.
     *
     * @param message a description of the capacity constraint that was violated
     * @param cause   the underlying exception that triggered this one
     */
    public ShelterAtCapacityException(String message, Throwable cause) {
        super(message, cause);
    }
}
