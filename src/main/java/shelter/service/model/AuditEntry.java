package shelter.service.model;

import shelter.domain.Staff;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single audit log entry recording who performed an action, what was affected, and when.
 * Instances are immutable once created to preserve the integrity of the audit trail.
 *
 * @param <T> the type of the target object that was acted upon
 */
public class AuditEntry<T> {

    private final Staff staff;
    private final String action;
    private final T target;
    private final LocalDateTime timestamp;

    /**
     * Constructs an AuditEntry with the given staff, action, target, and timestamp.
     * The timestamp should represent the moment the action was performed; no argument may be null.
     *
     * @param staff     the staff member who performed the action; must not be null
     * @param action    a short description of the action; must not be null or blank
     * @param target    the object that was acted upon; must not be null
     * @param timestamp the date and time the action occurred; must not be null
     * @throws IllegalArgumentException if any argument is null or {@code action} is blank
     */
    public AuditEntry(Staff staff, String action, T target, LocalDateTime timestamp) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff must not be null.");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action must not be null or blank.");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target must not be null.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp must not be null.");
        }
        this.staff = staff;
        this.action = action;
        this.target = target;
        this.timestamp = timestamp;
    }

    /**
     * Returns the staff member who performed the action.
     * Never null for a validly constructed entry.
     *
     * @return the staff member
     */
    public Staff getStaff() {
        return staff;
    }

    /**
     * Returns the description of the action performed.
     * Never null or blank for a validly constructed entry.
     *
     * @return the action string
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the target object that was acted upon.
     * Never null for a validly constructed entry.
     *
     * @return the target object
     */
    public T getTarget() {
        return target;
    }

    /**
     * Returns the timestamp of when the action was performed.
     * Never null for a validly constructed entry.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Copy constructor that creates a new AuditEntry with all field values copied from {@code other}.
     * The copy preserves the same staff, action, target reference, and timestamp.
     *
     * @param other the AuditEntry instance to copy; must not be null
     */
    public AuditEntry(AuditEntry<T> other) {
        this(other.staff, other.action, other.target, other.timestamp);
    }

    /**
     * Returns true if the given object is an AuditEntry with equal staff, action, target, and timestamp.
     * Equality is value-based since audit entries have no unique ID.
     *
     * @param o the object to compare
     * @return true if all fields are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditEntry)) return false;
        AuditEntry<?> other = (AuditEntry<?>) o;
        return Objects.equals(staff, other.staff)
                && Objects.equals(action, other.action)
                && Objects.equals(target, other.target)
                && Objects.equals(timestamp, other.timestamp);
    }

    /**
     * Returns a hash code based on all fields.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(staff, action, target, timestamp);
    }

    /**
     * Returns a string representation of this audit entry including staff, action, target, and timestamp.
     *
     * @return a human-readable description of this audit entry
     */
    @Override
    public String toString() {
        return "AuditEntry[staff=" + staff.getName()
                + ", action=" + action
                + ", target=" + target
                + ", timestamp=" + timestamp + "]";
    }

}
