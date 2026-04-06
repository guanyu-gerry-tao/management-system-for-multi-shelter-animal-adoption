package shelter.repository;

import shelter.service.model.AuditEntry;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Defines persistence operations for audit log entries across all service operations.
 * Implementations store entries durably so the audit trail survives beyond a single command execution.
 * The target object is stored as its string representation, since typed reconstruction is not required
 * for audit display purposes.
 */
public interface AuditRepository {

    /**
     * Appends a new audit log entry to the persistent store.
     * All parameters are required; no argument may be null or blank.
     *
     * @param staffId           the unique identifier of the staff member who performed the action
     * @param staffName         the display name of the staff member
     * @param action            a short description of the action performed (e.g. "approved adoption")
     * @param targetDescription the string representation of the target object acted upon
     * @param timestamp         the date and time the action occurred
     * @throws IllegalArgumentException if any string argument is null or blank, or timestamp is null
     */
    void append(String staffId, String staffName, String action,
                String targetDescription, LocalDateTime timestamp);

    /**
     * Returns all audit log entries ever persisted, ordered by timestamp ascending.
     * Returns an empty list if no entries have been recorded.
     * The target field of each returned entry contains the stored string description.
     *
     * @return a list of all audit entries with string-typed targets
     */
    List<AuditEntry<String>> findAll();

    /**
     * Removes all audit entries from the store while preserving the file and its header.
     * Intended for development and testing use only; do not call in production workflows.
     */
    void clear();
}