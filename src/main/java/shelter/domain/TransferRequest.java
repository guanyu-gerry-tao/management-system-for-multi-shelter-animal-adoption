package shelter.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a request to transfer an animal from one shelter to another.
 * A transfer request begins in {@link RequestStatus#PENDING} and transitions to
 * {@link RequestStatus#APPROVED}, {@link RequestStatus#REJECTED}, or
 * {@link RequestStatus#CANCELLED} based on administrative review.
 */
public class TransferRequest implements Comparable<TransferRequest> {

    private final String id;
    private final Animal animal;
    private final Shelter from;
    private final Shelter to;
    private final LocalDateTime requestedAt;
    private RequestStatus status;

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
     * Constructs a copy of the given transfer request, preserving the same ID and all field values.
     * This copy constructor creates an independent snapshot of an existing transfer request.
     *
     * @param other the transfer request to copy; must not be null
     */
    public TransferRequest(TransferRequest other) {
        this.id = other.id;
        this.animal = other.animal;
        this.from = other.from;
        this.to = other.to;
        this.status = other.status;
        this.requestedAt = other.requestedAt;
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
            throw new IllegalStateException(
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

    /**
     * Returns a string representation of this transfer request including its ID, status, and shelters.
     *
     * @return a human-readable description of this transfer request
     */
    @Override
    public String toString() {
        return "TransferRequest[id=" + id + ", animal=" + animal.getName()
                + ", from=" + from.getName() + ", to=" + to.getName()
                + ", status=" + status + ", requestedAt=" + requestedAt + "]";
    }

    /**
     * Returns true if the given object is a TransferRequest with the same unique ID.
     * Request identity is determined solely by its UUID, consistent with entity semantics.
     *
     * @param o the object to compare
     * @return true if {@code o} is a TransferRequest with the same ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransferRequest)) return false;
        TransferRequest other = (TransferRequest) o;
        return Objects.equals(id, other.id);
    }

    /**
     * Returns a hash code based on this request's unique ID.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Compares this transfer request to another by request timestamp in ascending order.
     * Earlier requests are ordered first, reflecting a first-come-first-served processing order.
     *
     * @param other the other transfer request to compare to
     * @return a negative number if this was requested earlier, positive if later, zero if same time
     */
    @Override
    public int compareTo(TransferRequest other) {
        return this.requestedAt.compareTo(other.requestedAt);
    }
}
