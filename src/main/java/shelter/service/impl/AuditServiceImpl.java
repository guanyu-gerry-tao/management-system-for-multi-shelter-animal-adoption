package shelter.service.impl;

import shelter.domain.Staff;
import shelter.repository.AuditRepository;
import shelter.service.AuditService;
import shelter.service.model.AuditEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete implementation of {@link AuditService} that both prints a formatted log line
 * to the console and persists each entry to the underlying {@link AuditRepository}.
 * The in-memory list preserves type information (the generic {@code T}) for the duration
 * of a single command execution; the repository stores the target as its string description
 * so the audit trail survives across invocations.
 *
 * @param <T> the type of the target object being audited in this service context
 */
public class AuditServiceImpl<T> implements AuditService<T> {

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Staff staff;
    private final AuditRepository auditRepository;
    private final List<AuditEntry<T>> inMemoryLog;

    /**
     * Constructs a new {@code AuditServiceImpl} bound to the given staff member and repository.
     * All entries logged through this instance will be attributed to {@code staff} and persisted
     * via {@code auditRepository}.
     *
     * @param staff           the staff member performing the audited operations; must not be null
     * @param auditRepository the repository used to persist audit entries; must not be null
     * @throws IllegalArgumentException if either argument is null
     */
    public AuditServiceImpl(Staff staff, AuditRepository auditRepository) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff must not be null.");
        }
        if (auditRepository == null) {
            throw new IllegalArgumentException("AuditRepository must not be null.");
        }
        this.staff = staff;
        this.auditRepository = auditRepository;
        this.inMemoryLog = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     * Prints a formatted log line to the console, adds the entry to the in-memory list,
     * and persists it to the audit repository. The console format is:
     * {@code [INFO]  yyyy-MM-dd HH:mm:ss  staffName  →  action  |  target.toString()}.
     *
     * @throws IllegalArgumentException if {@code action} is null or blank, or {@code target} is null
     */
    @Override
    public void log(String action, T target) {
        // Guard: both fields are required to form a meaningful audit entry
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action must not be null or blank.");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target must not be null.");
        }

        LocalDateTime now = LocalDateTime.now();

        // Build the in-memory entry and add it to the session log
        AuditEntry<T> entry = new AuditEntry<>(staff, action, target, now);
        inMemoryLog.add(entry);

        // Persist the entry to durable storage using the target's string representation
        auditRepository.append(staff.getId(), staff.getName(), action,
                target.toString(), now);

        // Print a human-readable log line to the console for real-time operator feedback
        System.out.println(formatLine(action, target.toString(), now));
    }

    /**
     * {@inheritDoc}
     * Returns entries accumulated during the current command execution only.
     * Entries from previous executions are not included; use the repository's
     * {@code findAll()} method to retrieve the full historical log.
     *
     * @return an unmodifiable view of the in-memory audit log for this session
     */
    @Override
    public List<AuditEntry<T>> getLog() {
        return Collections.unmodifiableList(inMemoryLog);
    }

    /**
     * Formats an audit log line for console output.
     * The format is: {@code [INFO]  timestamp  staffName  →  action  |  targetDescription}.
     *
     * @param action            the action description
     * @param targetDescription the string representation of the target
     * @param timestamp         the time the action occurred
     * @return the formatted log line
     */
    private String formatLine(String action, String targetDescription, LocalDateTime timestamp) {
        return "[INFO]  " + timestamp.format(DISPLAY_FMT)
                + "  " + staff.getName()
                + "  \u2192  " + action
                + "  |  " + targetDescription;
    }
}
