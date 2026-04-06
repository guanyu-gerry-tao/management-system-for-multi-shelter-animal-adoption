package shelter.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a request to transfer an animal from one shelter to another.
 * A transfer request begins in {@link RequestStatus#PENDING} and transitions to
 * {@link RequestStatus#APPROVED}, {@link RequestStatus#REJECTED}, or
 * {@link RequestStatus#CANCELLED} based on administrative review.
 */
public class TransferRequest {

    private final String id;
    private final Animal animal;
    private final Shelter from;
    private final Shelter to;
    private final LocalDateTime requestedAt;
    private RequestStatus status;

    /**
     * Reconstruction constructor for deserializing a TransferRequest from persistent storage.
     * This constructor accepts an explicit {@code id}, pre-existing status, and request timestamp
     * so that the full transfer state can be restored from CSV data without modification.
     *
     * @param id          the pre-existing unique identifier; must not be null or blank
     * @param animal      the animal to be transferred; must not be null
     * @param from        the source shelter; must not be null
     * @param to          the destination shelter; must not be null and must differ from {@code from}
     * @param status      the current status of the request; must not be null
     * @param requestedAt the original timestamp when the request was submitted; must not be null
     * @throws IllegalArgumentException if any parameter is null, {@code id} is blank, or shelters
     *                                  are the same
     */
    public TransferRequest(String id, Animal animal, Shelter from, Shelter to,
                           RequestStatus status, LocalDateTime requestedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("TransferRequest ID must not be null or blank.");
        }
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        if (from == null) {
            throw new IllegalArgumentException("Source shelter must not be null.");
        }
        if (to == null) {
            throw new IllegalArgumentException("Destination shelter must not be null.");
        }
        if (from.getId().equals(to.getId())) {
            throw new IllegalArgumentException("Source and destination shelters must be different.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        if (requestedAt == null) {
            throw new IllegalArgumentException("RequestedAt must not be null.");
        }
        this.id = id;
        this.animal = animal;
        this.from = from;
        this.to = to;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    /**
     * Constructs a new TransferRequest in {@link RequestStatus#PENDING} status.
     * All parameters are required, and the source and destination shelters must differ.
     *
     * @param animal the animal to be transferred; must not be null
     * @param from   the source shelter; must not be null
     * @param to     the destination shelter; must not be null and must differ from {@code from}
     * @throws IllegalArgumentException if any parameter is null, or if source and destination
     *                                  are the same shelter
     */
    public TransferRequest(Animal animal, Shelter from, Shelter to) {
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        if (from == null) {
            throw new IllegalArgumentException("Source shelter must not be null.");
        }
        if (to == null) {
            throw new IllegalArgumentException("Destination shelter must not be null.");
        }
        if (from.getId().equals(to.getId())) {
            throw new IllegalArgumentException(
                    "Source and destination shelters must be different.");
        }
        this.id = UUID.randomUUID().toString();
        this.animal = animal;
        this.from = from;
        this.to = to;
        this.status = RequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    /**
     * Approves this transfer request, transitioning its status to {@link RequestStatus#APPROVED}.
     * This method may only be called while the request is in {@link RequestStatus#PENDING} status.
     *
     * @throws IllegalStateException if the request is not currently {@link RequestStatus#PENDING}
     */
    public void approve() {
        requirePending("approve");
        this.status = RequestStatus.APPROVED;
    }

    /**
     * Rejects this transfer request, transitioning its status to {@link RequestStatus#REJECTED}.
     * This method may only be called while the request is in {@link RequestStatus#PENDING} status.
     *
     * @throws IllegalStateException if the request is not currently {@link RequestStatus#PENDING}
     */
    public void reject() {
        requirePending("reject");
        this.status = RequestStatus.REJECTED;
    }

    /**
     * Cancels this transfer request, transitioning its status to {@link RequestStatus#CANCELLED}.
     * This method may only be called while the request is in {@link RequestStatus#PENDING} status.
     *
     * @throws IllegalStateException if the request is not currently {@link RequestStatus#PENDING}
     */
    public void cancel() {
        requirePending("cancel");
        this.status = RequestStatus.CANCELLED;
    }

    /**
     * Asserts that the request is currently in {@link RequestStatus#PENDING} status.
     * Throws an {@link IllegalStateException} with a descriptive message otherwise.
     *
     * @param action the name of the attempted action, used in the error message
     */
    private void requirePending(String action) {
        if (status != RequestStatus.PENDING) {
            throw new shelter.exception.InvalidRequestStatusException(
                    "Cannot " + action + " a transfer request that is already " + status + ".");
        }
    }

    /**
     * Returns the unique identifier of this transfer request.
     *
     * @return the UUID string identifying this request
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the animal to be transferred.
     *
     * @return the {@link Animal} associated with this request
     */
    public Animal getAnimal() {
        return animal;
    }

    /**
     * Returns the source shelter from which the animal is being transferred.
     *
     * @return the source {@link Shelter}
     */
    public Shelter getFrom() {
        return from;
    }

    /**
     * Returns the destination shelter to which the animal is being transferred.
     *
     * @return the destination {@link Shelter}
     */
    public Shelter getTo() {
        return to;
    }

    /**
     * Returns the current status of this transfer request.
     *
     * @return the {@link RequestStatus} of this request
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Returns the timestamp at which this transfer request was created.
     *
     * @return the creation {@link LocalDateTime}
     */
    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
