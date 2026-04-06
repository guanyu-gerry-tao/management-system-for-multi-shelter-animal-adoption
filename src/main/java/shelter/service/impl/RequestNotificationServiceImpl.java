package shelter.service.impl;

import shelter.domain.AdoptionRequest;
import shelter.domain.Staff;
import shelter.domain.TransferRequest;
import shelter.repository.AuditRepository;
import shelter.service.RequestNotificationService;
import shelter.service.model.NotificationRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete implementation of {@link RequestNotificationService} that prints formatted
 * notification messages to the console and maintains an in-memory record of all dispatched
 * notifications for the duration of the current command execution.
 * In this demo system, "sending a notification" means printing a {@code [NOTIFY]} line;
 * no external messaging channel is involved.
 */
public class RequestNotificationServiceImpl implements RequestNotificationService {

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Staff staff;
    private final AuditRepository auditRepository;
    private final List<NotificationRecord> records;

    /**
     * Constructs a new {@code RequestNotificationServiceImpl} attributed to the given staff member.
     * All notifications dispatched through this instance will be recorded under {@code staff}
     * and persisted to the audit repository with a {@code [NOTIFICATION]} level prefix.
     *
     * @param staff           the staff member responsible for dispatching notifications; must not be null
     * @param auditRepository the repository used to persist notification entries; must not be null
     * @throws IllegalArgumentException if either argument is null
     */
    public RequestNotificationServiceImpl(Staff staff, AuditRepository auditRepository) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff must not be null.");
        }
        if (auditRepository == null) {
            throw new IllegalArgumentException("AuditRepository must not be null.");
        }
        this.staff = staff;
        this.auditRepository = auditRepository;
        this.records = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     * Prints a {@code [NOTIFY]} line to the console describing the adoption status change,
     * then records the notification in the in-memory list.
     *
     * @throws IllegalArgumentException if {@code request} is null
     */
    @Override
    public void notifyAdoptionStatusChange(AdoptionRequest request) {
        // Guard: request must not be null
        if (request == null) {
            throw new IllegalArgumentException("AdoptionRequest must not be null.");
        }

        // Build the human-readable notification message
        String message = "Adopter '" + request.getAdopter().getName()
                + "' — adoption request " + request.getId()
                + " for animal '" + request.getAnimal().getName()
                + "' is now " + request.getStatus();

        dispatchAndRecord(message, request.getId());
    }

    /**
     * {@inheritDoc}
     * Prints a {@code [NOTIFY]} line to the console describing the transfer status change,
     * then records the notification in the in-memory list.
     *
     * @throws IllegalArgumentException if {@code request} is null
     */
    @Override
    public void notifyTransferStatusChange(TransferRequest request) {
        // Guard: request must not be null
        if (request == null) {
            throw new IllegalArgumentException("TransferRequest must not be null.");
        }

        // Build the human-readable notification message
        String message = "Transfer request " + request.getId()
                + " for animal '" + request.getAnimal().getName()
                + "' (" + request.getFrom().getName()
                + " \u2192 " + request.getTo().getName()
                + ") is now " + request.getStatus();

        dispatchAndRecord(message, request.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotificationRecord> listAll() {
        return Collections.unmodifiableList(records);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code staff} is null
     */
    @Override
    public List<NotificationRecord> getByStaff(Staff staff) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff must not be null.");
        }
        return records.stream()
                .filter(r -> r.getStaff().equals(staff))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code targetId} is null or blank
     */
    @Override
    public List<NotificationRecord> getByTarget(String targetId) {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("Target ID must not be null or blank.");
        }
        return records.stream()
                .filter(r -> r.getTargetId().equals(targetId))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code keyword} is null or blank
     */
    @Override
    public List<NotificationRecord> searchByAction(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword must not be null or blank.");
        }
        return records.stream()
                .filter(r -> r.getAction().contains(keyword))
                .collect(Collectors.toList());
    }

    /**
     * Prints the notification to the console and adds a record to the in-memory list.
     * This method centralises the two side effects shared by both notify methods.
     *
     * @param message  the human-readable notification text to print
     * @param targetId the ID of the request that triggered the notification
     */
    private void dispatchAndRecord(String message, String targetId) {
        LocalDateTime now = LocalDateTime.now();

        // Print the notification line for real-time operator visibility
        System.out.println("[NOTIFY]  " + now.format(DISPLAY_FMT)
                + "  " + staff.getName()
                + "  |  " + message);

        // Persist to the shared audit log with NOTIFICATION level prefix for traceability
        auditRepository.append(staff.getId(), staff.getName(),
                "[NOTIFICATION] " + message, targetId, now);

        // Keep an in-memory record for query within the current command execution
        records.add(new NotificationRecord(staff, message, targetId, now));
    }
}
