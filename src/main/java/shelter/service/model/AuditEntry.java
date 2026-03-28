package shelter.service.model;

import shelter.domain.Staff;

import java.time.LocalDateTime;

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
     * The timestamp should represent the moment the action was performed.
     *
     * @param staff     the staff member who performed the action
     * @param action    a short description of the action
     * @param target    the object that was acted upon
     * @param timestamp the date and time the action occurred
     */
    public AuditEntry(Staff staff, String action, T target, LocalDateTime timestamp) {
        this.staff = staff;
        this.action = action;
        this.target = target;
        this.timestamp = timestamp;
    }

    /**
     * Returns the staff member who performed the action.
     *
     * @return the staff member
     */
    public Staff getStaff() {
        return staff;
    }

    /**
     * Returns the description of the action performed.
     *
     * @return the action string
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the target object that was acted upon.
     *
     * @return the target object
     */
    public T getTarget() {
        return target;
    }

    /**
     * Returns the timestamp of when the action was performed.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
