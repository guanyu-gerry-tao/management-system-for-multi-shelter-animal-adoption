package shelter.exception;

/**
 * Thrown when a vaccine type is applied to an animal of a different species than the
 * vaccine is approved for. For example, administering a dog-specific vaccine to a cat
 * is a business rule violation that should be caught early in the vaccination workflow.
 * This exception extends {@link IllegalArgumentException} because the caller supplied
 * an animal-vaccine combination that is logically invalid.
 */
public class SpeciesMismatchException extends IllegalArgumentException {

    /**
     * Constructs a new {@code SpeciesMismatchException} with the given detail message.
     * The message should identify both the vaccine type and the animal's species,
     * for example: {@code "Vaccine \"Rabies\" applies to DOG, but animal is CAT."}.
     *
     * @param message a description of the species mismatch
     */
    public SpeciesMismatchException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code SpeciesMismatchException} with the given detail message and cause.
     *
     * @param message a description of the species mismatch
     * @param cause   the underlying exception that triggered this one
     */
    public SpeciesMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
