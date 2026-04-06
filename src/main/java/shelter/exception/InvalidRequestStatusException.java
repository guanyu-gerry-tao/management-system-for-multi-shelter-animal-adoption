package shelter.exception;

/**
 * Thrown when a lifecycle operation is attempted on a request that is not in the
 * required status. For example, approving, rejecting, or cancelling a request that
 * has already been processed will trigger this exception. It extends
 * {@link IllegalStateException} so that callers catching the broader state-violation
 * category still handle it correctly.
 */
public class InvalidRequestStatusException extends IllegalStateException {

    /**
     * Constructs a new {@code InvalidRequestStatusException} with the given detail message.
     * The message should identify the attempted action and the current status,
     * for example: {@code "Cannot approve a request that is already APPROVED."}.
     *
     * @param message a description of the invalid status transition that was attempted
     */
    public InvalidRequestStatusException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code InvalidRequestStatusException} with the given detail message and cause.
     *
     * @param message a description of the invalid status transition that was attempted
     * @param cause   the underlying exception that triggered this one
     */
    public InvalidRequestStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
