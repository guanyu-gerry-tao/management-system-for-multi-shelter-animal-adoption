package shelter.application;

import shelter.service.model.AuditEntry;

import java.util.List;

/**
 * Application service for retrieving the session audit log.
 * Provides access to the full history of staff actions performed during the current session,
 * delegating directly to the underlying {@code AuditService}.
 */
public interface AuditApplicationService {

    /**
     * Returns the full audit log for the current session.
     * Each entry records who performed an action, what was affected, and when it occurred.
     * Returns an empty list if no actions have been logged in this session.
     *
     * @return a list of {@link AuditEntry} records in chronological order
     */
    List<AuditEntry<?>> getLog();
}
