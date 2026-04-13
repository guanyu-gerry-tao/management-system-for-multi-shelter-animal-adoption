package shelter.application.impl;

import shelter.application.AuditApplicationService;
import shelter.service.AuditService;
import shelter.service.model.AuditEntry;

import java.util.List;

/**
 * Default implementation of {@link AuditApplicationService} that delegates directly to
 * the underlying {@link AuditService} to retrieve the session audit log.
 * This class exists as the application-layer boundary for the audit use case (UC-08),
 * keeping the CLI layer decoupled from the service layer.
 */
public class AuditApplicationServiceImpl implements AuditApplicationService {

    private final AuditService<?> auditService;

    /**
     * Constructs an AuditApplicationServiceImpl with the given audit service.
     * The audit service provides access to all log entries recorded in the current session.
     *
     * @param auditService the audit service to delegate to; must not be null
     * @throws IllegalArgumentException if {@code auditService} is null
     */
    public AuditApplicationServiceImpl(AuditService<?> auditService) {
        if (auditService == null) {
            throw new IllegalArgumentException("AuditService must not be null.");
        }
        this.auditService = auditService;
    }

    /**
     * {@inheritDoc}
     * Delegates directly to {@link AuditService#getLog()} and returns the result.
     */
    @Override
    public List<AuditEntry<?>> getLog() {
        return (List<AuditEntry<?>>) (List<?>) auditService.getLog();
    }
}
