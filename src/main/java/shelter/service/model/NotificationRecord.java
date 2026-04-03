package shelter.service.model;

import shelter.domain.Staff;

import java.time.LocalDateTime;

/**
 * Represents a record of a notification that was dispatched by a staff member.
 * Captures who sent the notification, what action triggered it, the target identifier, and when it occurred.
 */
public class NotificationRecord {

    private final Staff staff;
    private final String action;
    private final String targetId;
    private final LocalDateTime timestamp;

    /**
     * Constructs a NotificationRecord with the given staff, action, target identifier, and timestamp.
     * All fields are immutable once set to preserve the integrity of the notification history.
     * No argument may be null; {@code action} and {@code targetId} must not be blank.
     *
     * @param staff     the staff member who dispatched the notification; must not be null
     * @param action    a short description of the action that triggered the notification; must not be null or blank
     * @param targetId  the identifier of the target object (e.g. request ID); must not be null or blank
     * @param timestamp the date and time the notification was sent; must not be null
     * @throws IllegalArgumentException if any argument is null or {@code action}/{@code targetId} is blank
     */
    public NotificationRecord(Staff staff, String action, String targetId, LocalDateTime timestamp) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff must not be null.");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action must not be null or blank.");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("Target ID must not be null or blank.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp must not be null.");
        }
        this.staff = staff;
        this.action = action;
        this.targetId = targetId;
        this.timestamp = timestamp;
    }

    /**
     * Returns the staff member who dispatched this notification.
     * Never null for a validly constructed record.
     *
     * @return the staff member
     */
    public Staff getStaff() {
        return staff;
    }

    /**
     * Returns the action description that triggered this notification.
     * Never null or blank for a validly constructed record.
     *
     * @return the action string
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the identifier of the target object this notification relates to.
     * Never null or blank for a validly constructed record.
     *
     * @return the target identifier
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Returns the timestamp of when this notification was dispatched.
     * Never null for a validly constructed record.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a string representation of this notification record including staff, action, target, and timestamp.
     *
     * @return a human-readable description of this notification record
     */
    @Override
    public String toString() {
        return "NotificationRecord[staff=" + staff.getName()
                + ", action=" + action
                + ", targetId=" + targetId
                + ", timestamp=" + timestamp + "]";
    }

}
