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
     * Constructs a copy of the given adoption request, preserving the same ID and all field values.
     * This copy constructor creates an independent snapshot of an existing request instance.
     *
     * @param other the adoption request to copy; must not be null
     */
    public AdoptionRequest(AdoptionRequest other) {
        this.id = other.id;
        this.adopter = other.adopter;
        this.animal = other.animal;
        this.status = other.status;
        this.submittedAt = other.submittedAt;
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
            throw new IllegalStateException(
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
     * Returns a string representation of this adoption request including its ID, status, and participants.
     *
     * @return a human-readable description of this adoption request
     */
    @Override
    public String toString() {
        return "AdoptionRequest[id=" + id + ", adopter=" + adopter.getName()
                + ", animal=" + animal.getName() + ", status=" + status
                + ", submittedAt=" + submittedAt + "]";
    }

    /**
     * Returns true if the given object is an AdoptionRequest with the same unique ID.
     * Request identity is determined solely by its UUID, consistent with entity semantics.
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
     * Compares this adoption request to another by submission timestamp in ascending order.
     * Earlier requests are ordered first, reflecting a first-come-first-served processing order.
     *
     * @param other the other adoption request to compare to
     * @return a negative number if this was submitted earlier, positive if later, zero if same time
     */
    @Override
    public int compareTo(AdoptionRequest other) {
        return this.submittedAt.compareTo(other.submittedAt);
    }
}
