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
     *
     * @param staff     the staff member who dispatched the notification
     * @param action    a short description of the action that triggered the notification
     * @param targetId  the identifier of the target object (e.g. request ID)
     * @param timestamp the date and time the notification was sent
     */
    public NotificationRecord(Staff staff, String action, String targetId, LocalDateTime timestamp) {
        this.staff = staff;
        this.action = action;
        this.targetId = targetId;
        this.timestamp = timestamp;
    }

    /**
     * Returns the staff member who dispatched this notification.
     *
     * @return the staff member
     */
    public Staff getStaff() {
        return staff;
    }

    /**
     * Returns the action description that triggered this notification.
     *
     * @return the action string
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the identifier of the target object this notification relates to.
     *
     * @return the target identifier
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Returns the timestamp of when this notification was dispatched.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
