package shelter.domain;

/**
 * Represents the lifecycle status of a request, such as an adoption or transfer request.
 * A request begins in {@link #PENDING} and transitions to one of the terminal states:
 * {@link #APPROVED}, {@link #REJECTED}, or {@link #CANCELLED}.
 */
public enum RequestStatus {

    /** The request has been submitted and is awaiting review. */
    PENDING,

    /** The request has been reviewed and approved. */
    APPROVED,

    /** The request has been reviewed and denied. */
    REJECTED,

    /** The request was withdrawn before a decision was made. */
    CANCELLED
}
