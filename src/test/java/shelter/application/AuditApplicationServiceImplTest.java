package shelter.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.application.impl.AuditApplicationServiceImpl;
import shelter.domain.Staff;
import shelter.service.AuditService;
import shelter.service.model.AuditEntry;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AuditApplicationServiceImpl}.
 * Verifies that the application layer correctly delegates log retrieval to the underlying
 * {@link AuditService} and enforces constructor validation.
 */
class AuditApplicationServiceImplTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private Staff staff;
    private AuditEntry<String> entry;

    @BeforeEach
    void setUp() {
        staff = new Staff("Admin", "Manager");
        entry = new AuditEntry<>(staff, "registered shelter", "shelter-1", LocalDateTime.now());
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void getLog_returnsAllEntries() {
        StubAuditService<String> auditService = new StubAuditService<>(List.of(entry));
        AuditApplicationServiceImpl service = new AuditApplicationServiceImpl(auditService);

        List<AuditEntry<?>> result = service.getLog();

        assertEquals(1, result.size());
        assertEquals("registered shelter", result.get(0).getAction());
    }

    @Test
    void getLog_emptyLog_returnsEmpty() {
        StubAuditService<String> auditService = new StubAuditService<>(List.of());
        AuditApplicationServiceImpl service = new AuditApplicationServiceImpl(auditService);

        assertTrue(service.getLog().isEmpty());
    }

    @Test
    void constructor_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new AuditApplicationServiceImpl(null));
    }

    // -------------------------------------------------------------------------
    // Stubs
    // -------------------------------------------------------------------------

    /**
     * Stub implementation of {@link AuditService} that returns a fixed pre-populated log.
     * The {@code log} method is a no-op; only {@code getLog} is exercised here.
     *
     * @param <T> the type of the audit target
     */
    private static class StubAuditService<T> implements AuditService<T> {

        private final List<AuditEntry<T>> log;

        StubAuditService(List<AuditEntry<T>> log) {
            this.log = log;
        }

        @Override
        public void log(String action, T target) {
            // no-op — write path is not under test here
        }

        @Override
        public List<AuditEntry<T>> getLog() {
            return log;
        }
    }
}
