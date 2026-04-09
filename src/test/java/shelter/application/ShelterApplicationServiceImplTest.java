package shelter.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.application.impl.ShelterApplicationServiceImpl;
import shelter.domain.Shelter;
import shelter.exception.EntityNotFoundException;
import shelter.service.AuditService;
import shelter.service.ShelterService;
import shelter.service.model.AuditEntry;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ShelterApplicationServiceImpl}.
 * Verifies that the correct service methods are called and audit entries are recorded.
 */
class ShelterApplicationServiceImplTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private StubShelterService shelterService;
    private SpyAuditService<Shelter> auditService;
    private ShelterApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        shelterService = new StubShelterService();
        auditService   = new SpyAuditService<>();
        service        = new ShelterApplicationServiceImpl(shelterService, auditService);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void registerShelter_createsAndPersistsShelter() {
        Shelter result = service.registerShelter("Happy Paws", "Boston", 20);
        assertEquals("Happy Paws", result.getName());
        assertEquals(1, shelterService.registerCount);
    }

    @Test
    void registerShelter_logsAuditEntry() {
        service.registerShelter("Happy Paws", "Boston", 20);
        assertTrue(auditService.loggedActions.contains("registered shelter"));
    }

    @Test
    void listShelters_delegatesToService() {
        shelterService.store.put("s1", new Shelter("s1", "A", "Loc", 10));
        assertEquals(1, service.listShelters().size());
    }

    @Test
    void updateShelter_mergesFieldsAndPersists() {
        Shelter original = new Shelter("Happy Paws", "Boston", 20);
        shelterService.store.put(original.getId(), original);

        Shelter updated = service.updateShelter(original.getId(), "New Name", null, null);

        assertEquals("New Name", updated.getName());
        assertEquals("Boston", updated.getLocation()); // unchanged
        assertEquals(1, shelterService.updateCount);
        assertTrue(auditService.loggedActions.contains("updated shelter"));
    }

    @Test
    void removeShelter_delegatesAndLogsAudit() {
        Shelter shelter = new Shelter("Happy Paws", "Boston", 20);
        shelterService.store.put(shelter.getId(), shelter);

        service.removeShelter(shelter.getId());

        assertEquals(1, shelterService.removeCount);
        assertTrue(auditService.loggedActions.contains("removed shelter"));
    }

    @Test
    void removeShelter_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.removeShelter("missing"));
    }

    @Test
    void constructor_nullShelterService_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShelterApplicationServiceImpl(null, auditService));
    }

    @Test
    void constructor_nullAuditService_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShelterApplicationServiceImpl(shelterService, null));
    }

    // -------------------------------------------------------------------------
    // Stubs
    // -------------------------------------------------------------------------

    /**
     * Stub implementation of {@link ShelterService} backed by an in-memory map.
     * Counters track how many times each mutating method was invoked.
     */
    private static class StubShelterService implements ShelterService {

        final Map<String, Shelter> store = new HashMap<>();
        int registerCount;
        int updateCount;
        int removeCount;

        @Override
        public void register(Shelter s) {
            store.put(s.getId(), s);
            registerCount++;
        }

        @Override
        public void update(Shelter s) {
            store.put(s.getId(), s);
            updateCount++;
        }

        @Override
        public void remove(Shelter s) {
            store.remove(s.getId());
            removeCount++;
        }

        @Override
        public Shelter findById(String id) {
            Shelter s = store.get(id);
            if (s == null) throw new EntityNotFoundException("Shelter not found: " + id);
            return s;
        }

        @Override
        public List<Shelter> listAll() {
            return new ArrayList<>(store.values());
        }
    }

    /**
     * Spy implementation of {@link AuditService} that records the action strings passed to {@link #log}.
     * Use {@link #loggedActions} to assert which operations were audited.
     *
     * @param <T> the type of the audit target
     */
    private static class SpyAuditService<T> implements AuditService<T> {

        final List<String> loggedActions = new ArrayList<>();

        @Override
        public void log(String action, T target) {
            loggedActions.add(action);
        }

        @Override
        public List<AuditEntry<T>> getLog() {
            return List.of();
        }
    }
}
