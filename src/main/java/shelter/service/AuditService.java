package shelter.service;

import shelter.service.model.AuditEntry;

import java.util.List;

/**
 * Records audit log entries for operations performed within a service.
 * The staff member is provided at construction time and applied to all log entries automatically.
 *
 * @param <T> the type of the target object being acted upon
 */
public interface AuditService<T> {

    /**
     * Records an audit entry for the given action and target object.
     * The staff member is determined internally from the instance configured at construction time.
     * Throws an exception if any argument is null.
     *
     * @param action a short description of the action (e.g. "approved", "recorded vaccination")
     * @param target the object that was acted upon
     */
    void log(String action, T target);

    /**
     * Returns all audit log entries recorded so far.
     * Returns an empty list if no entries have been logged.
     *
     * @return a list of all audit entries
     */
    List<AuditEntry<T>> getLog();
}
