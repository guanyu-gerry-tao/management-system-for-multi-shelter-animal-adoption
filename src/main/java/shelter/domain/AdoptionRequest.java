package shelter.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a request submitted by an adopter to adopt a specific animal.
 * An adoption request starts in {@link RequestStatus#PENDING} and transitions to
 * {@link RequestStatus#APPROVED}, {@link RequestStatus#REJECTED}, or
 * {@link RequestStatus#CANCELLED} through the corresponding lifecycle methods.
 */
public class AdoptionRequest implements Comparable<AdoptionRequest> {

    private final String id;
    private final Adopter adopter;
    private final Animal animal;
    private final LocalDateTime submittedAt;
    private RequestStatus status;

    /**
     * Reconstruction constructor for deserializing an AdoptionRequest from persistent storage.
     * This constructor accepts an explicit {@code id}, pre-existing status, and submission timestamp
     * so that the full request state can be restored from CSV data without modification.
     *
     * @param id          the pre-existing unique identifier; must not be null or blank
     * @param adopter     the adopter who submitted the request; must not be null
     * @param animal      the animal the adopter wishes to adopt; must not be null
     * @param status      the current status of the request; must not be null
     * @param submittedAt the original timestamp when the request was submitted; must not be null
     * @throws IllegalArgumentException if any parameter is null or {@code id} is blank
     */
    public AdoptionRequest(String id, Adopter adopter, Animal animal,
                           RequestStatus status, LocalDateTime submittedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("AdoptionRequest ID must not be null or blank.");
        }
        if (adopter == null) {
            throw new IllegalArgumentException("Adopter must not be null.");
        }
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        if (submittedAt == null) {
            throw new IllegalArgumentException("SubmittedAt must not be null.");
        }
        this.id = id;
        this.adopter = adopter;
        this.animal = animal;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    /**
     * Constructs a new AdoptionRequest in {@link RequestStatus#PENDING} status.
     * Both adopter and animal are required and must not be null.
     *
     * @param adopter the adopter submitting the request; must not be null
     * @param animal  the animal the adopter wishes to adopt; must not be null
     * @throws IllegalArgumentException if {@code adopter} or {@code animal} is null
     */
    public AdoptionRequest(Adopter adopter, Animal animal) {
        if (adopter == null) {
            throw new IllegalArgumentException("Adopter must not be null.");
        }
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        this.id = UUID.randomUUID().toString();
        this.adopter = adopter;
        this.animal = animal;
        this.status = RequestStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
    }

    /**
     * Approves this adoption request, transitioning its status to {@link RequestStatus#APPROVED}.
     * This method may only be called while the request is in {@link RequestStatus#PENDING} status.
     *
     * @throws IllegalStateException if the request is not currently {@link RequestStatus#PENDING}
     */
    public void approve() {
        requirePending("approve");
        this.status = RequestStatus.APPROVED;
    }

    /**
     * Rejects this adoption request, transitioning its status to {@link RequestStatus#REJECTED}.
     * This method may only be called while the request is in {@link RequestStatus#PENDING} status.
     *
     * @throws IllegalStateException if the request is not currently {@link RequestStatus#PENDING}
     */
    public void reject() {
        requirePending("reject");
        this.status = RequestStatus.REJECTED;
    }

    /**
     * Cancels this adoption request, transitioning its status to {@link RequestStatus#CANCELLED}.
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
                    "Cannot " + action + " a request that is already " + status + ".");
        }
    }

    /**
     * Returns the unique identifier of this adoption request.
     *
     * @return the UUID string identifying this request
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the adopter who submitted this request.
     *
     * @return the {@link Adopter} associated with this request
     */
    public Adopter getAdopter() {
        return adopter;
    }

    /**
     * Returns the animal this request targets.
     *
     * @return the {@link Animal} associated with this request
     */
    public Animal getAnimal() {
        return animal;
    }

    /**
     * Returns the current status of this adoption request.
     *
     * @return the {@link RequestStatus} of this request
     */
    public RequestStatus getStatus() {
        return status;
    }

    /**
     * Returns the timestamp at which this request was created.
     *
     * @return the submission {@link LocalDateTime}
     */
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    /**
     * Copy constructor that creates a new AdoptionRequest with all field values copied from {@code other}.
     * The copy preserves the same ID, adopter, animal, status, and submission timestamp.
     *
     * @param other the AdoptionRequest instance to copy; must not be null
     */
    public AdoptionRequest(AdoptionRequest other) {
        this(other.id, other.adopter, other.animal, other.status, other.submittedAt);
    }

    /**
     * Compares this request to another by submission timestamp ascending.
     * Earlier requests are ordered first, reflecting the natural queue order.
     *
     * @param other the other AdoptionRequest to compare to
     * @return a negative number if this request was submitted earlier, positive if later, zero if equal
     */
    @Override
    public int compareTo(AdoptionRequest other) {
        return this.submittedAt.compareTo(other.submittedAt);
    }

    /**
     * Returns true if the given object is an AdoptionRequest with the same ID.
     *
     * @param o the object to compare
     * @return true if {@code o} is an AdoptionRequest with the same ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdoptionRequest)) return false;
        AdoptionRequest other = (AdoptionRequest) o;
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
     * Returns a string representation of this request including ID, adopter, animal, and status.
     *
     * @return a human-readable description of this request
     */
    @Override
    public String toString() {
        return "AdoptionRequest[id=" + id + ", adopter=" + adopter.getName()
                + ", animal=" + animal.getName() + ", status=" + status + "]";
    }
}
