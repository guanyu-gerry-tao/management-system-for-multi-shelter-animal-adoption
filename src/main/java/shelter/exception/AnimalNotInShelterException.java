package shelter.exception;

/**
 * Thrown when an operation expects an animal to be present in a specific shelter,
 * but the animal is not found there. Typical use cases include attempting to transfer
 * an animal from a shelter it does not belong to, or removing an animal that has
 * already been moved. This exception extends {@link IllegalArgumentException} because
 * the caller supplied an animal-shelter combination that is logically invalid.
 */
public class AnimalNotInShelterException extends IllegalArgumentException {

    /**
     * Constructs a new {@code AnimalNotInShelterException} with the given detail message.
     * The message should identify both the animal and the shelter,
     * for example: {@code "Animal \"Rex\" is not in shelter \"Happy Paws\"."}.
     *
     * @param message a description identifying the animal and shelter involved
     */
    public AnimalNotInShelterException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code AnimalNotInShelterException} with the given detail message and cause.
     *
     * @param message a description identifying the animal and shelter involved
     * @param cause   the underlying exception that triggered this one
     */
    public AnimalNotInShelterException(String message, Throwable cause) {
        super(message, cause);
    }
}
