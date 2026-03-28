package shelter.service;

import shelter.domain.AdoptionRequest;
import shelter.domain.Staff;
import shelter.domain.TransferRequest;
import shelter.service.model.NotificationRecord;

import java.util.List;

/**
 * Dispatches notifications to relevant parties when the status of a request changes.
 * Implementations may send notifications via email, in-app messaging, or other channels.
 */
public interface RequestNotificationService {

    /**
     * Sends a notification to the adopter when the status of their adoption request changes.
     * Throws an exception if the request or its associated adopter is null.
     *
     * @param request the adoption request whose status has changed
     */
    void notifyAdoptionStatusChange(AdoptionRequest request);

    /**
     * Sends a notification to relevant shelter staff when a transfer request status changes.
     * Throws an exception if the request or its associated shelters are null.
     *
     * @param request the transfer request whose status has changed
     */
    void notifyTransferStatusChange(TransferRequest request);

    /**
     * Returns all notifications that have been dispatched.
     * Returns an empty list if no notifications have been sent.
     *
     * @return a list of all notification records
     */
    List<NotificationRecord> listAll();

    /**
     * Returns all notifications dispatched by the given staff member.
     * Returns an empty list if the staff member has sent no notifications.
     *
     * @param staff the staff member to query
     * @return a list of notification records sent by the staff member
     */
    List<NotificationRecord> getByStaff(Staff staff);

    /**
     * Returns all notifications associated with the given target identifier.
     * Returns an empty list if no notifications match the target.
     *
     * @param targetId the identifier of the target object
     * @return a list of notification records for the target
     */
    List<NotificationRecord> getByTarget(String targetId);

    /**
     * Returns all notifications whose action description contains the given keyword.
     * Returns an empty list if no notifications match the keyword.
     *
     * @param keyword the string to search for in action descriptions
     * @return a list of matching notification records
     */
    List<NotificationRecord> searchByAction(String keyword);
}
