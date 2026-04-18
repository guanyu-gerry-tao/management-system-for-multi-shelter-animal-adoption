package shelter.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.application.impl.AdopterApplicationServiceImpl;
import shelter.domain.*;
import shelter.exception.EntityNotFoundException;
import shelter.service.AdopterService;
import shelter.service.AdoptionService;
import shelter.service.AuditService;
import shelter.service.model.AuditEntry;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdopterApplicationServiceImpl}.
 * Verifies adopter registration, listing, update, removal, and constructor guard conditions.
 */
class AdopterApplicationServiceImplTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private StubAdopterService adopterService;
    private StubAdoptionService adoptionService;
    private SpyAuditService<Adopter> auditService;
    private AdopterApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        adopterService  = new StubAdopterService();
        adoptionService = new StubAdoptionService();
        auditService    = new SpyAuditService<>();
        service         = new AdopterApplicationServiceImpl(adopterService, adoptionService, auditService);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void registerAdopter_createsAndPersists() {
        Adopter result = service.registerAdopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, Species.DOG, null, ActivityLevel.MEDIUM,
                null, 0, 10);

        assertNotNull(result);
        assertEquals("Alice", result.getName());
        assertEquals(1, adopterService.registerCount);
        assertTrue(auditService.actions.contains("registered adopter"));
    }

    @Test
    void listAdopters_returnsAll() {
        service.registerAdopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null, null, null, null, 0, 20);

        assertEquals(1, service.listAdopters().size());
    }

    @Test
    void updateAdopter_mergesNameOnly() {
        Adopter adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, null, null, 0, 20));
        adopterService.store.put(adopter.getId(), adopter);

        Adopter updated = service.updateAdopter(adopter.getId(), "Bob", null, null,
                null, null, null, null, null, null);

        assertEquals("Bob", updated.getName());
        assertEquals(LivingSpace.HOUSE_WITH_YARD, updated.getLivingSpace()); // unchanged
        assertEquals(1, adopterService.updateCount);
        assertTrue(auditService.actions.contains("updated adopter"));
    }

    @Test
    void updateAdopter_notFound_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> service.updateAdopter("missing", "Bob", null, null,
                        null, null, null, null, null, null));
    }

    @Test
    void removeAdopter_delegatesAndLogs() {
        Adopter adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, null, null, 0, 20));
        adopterService.store.put(adopter.getId(), adopter);

        service.removeAdopter(adopter.getId());

        assertEquals(1, adopterService.removeCount);
        assertTrue(auditService.actions.contains("removed adopter"));
    }

    @Test
    void removeAdopter_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.removeAdopter("missing"));
    }

    @Test
    void constructor_nullArgument_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new AdopterApplicationServiceImpl(null, adoptionService, auditService));
        assertThrows(IllegalArgumentException.class,
                () -> new AdopterApplicationServiceImpl(adopterService, null, auditService));
        assertThrows(IllegalArgumentException.class,
                () -> new AdopterApplicationServiceImpl(adopterService, adoptionService, null));
    }

    // -------------------------------------------------------------------------
    // Stubs
    // -------------------------------------------------------------------------

    /**
     * Stub implementation of {@link AdopterService} backed by an in-memory map.
     * Uses a copy constructor in {@code register} and {@code update} to simulate value-copy CSV behavior.
     */
    private static class StubAdopterService implements AdopterService {

        final Map<String, Adopter> store = new HashMap<>();
        int registerCount;
        int updateCount;
        int removeCount;

        @Override
        public void register(Adopter a) {
            store.put(a.getId(), new Adopter(a));
            registerCount++;
        }

        @Override
        public void update(Adopter a) {
            store.put(a.getId(), new Adopter(a));
            updateCount++;
        }

        @Override
        public void remove(Adopter a) {
            store.remove(a.getId());
            removeCount++;
        }

        @Override
        public List<Adopter> listAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Adopter findById(String id) {
            Adopter a = store.get(id);
            if (a == null) throw new EntityNotFoundException("Adopter not found: " + id);
            return a;
        }

        @Override
        public List<Adopter> registeredAfter(LocalDate d) {
            return List.of();
        }

        @Override
        public List<Adopter> adoptedAfter(LocalDate d) {
            return List.of();
        }

        @Override
        public List<Adopter> adoptedAnimal(Animal a) {
            return List.of();
        }
    }

    /**
     * Stub implementation of {@link AdoptionService} that returns empty lists for all queries.
     * Used to satisfy the dependency without simulating pending requests in most tests.
     */
    private static class StubAdoptionService implements AdoptionService {
        @Override public void submit(shelter.domain.AdoptionRequest r) {}
        @Override public void approve(shelter.domain.AdoptionRequest r) {}
        @Override public void reject(shelter.domain.AdoptionRequest r) {}
        @Override public void cancel(shelter.domain.AdoptionRequest r) {}
        @Override public List<shelter.domain.AdoptionRequest> getRequestsByAdopter(Adopter a) { return List.of(); }
        @Override public List<shelter.domain.AdoptionRequest> getRequestsByShelter(shelter.domain.Shelter s) { return List.of(); }
        @Override public List<shelter.domain.AdoptionRequest> getRequestsByAnimal(Animal a) { return List.of(); }
        @Override public List<shelter.domain.AdoptionRequest> getRequestsAfter(java.time.LocalDate d) { return List.of(); }
        @Override public List<shelter.domain.AdoptionRequest> getApprovedAfter(java.time.LocalDate d) { return List.of(); }
        @Override public List<shelter.domain.AdoptionRequest> listAll() { return List.of(); }
    }

    /**
     * Spy implementation of {@link AuditService} that records the action strings passed to {@link #log}.
     * Use {@link #actions} to assert which operations were audited.
     *
     * @param <T> the type of the audit target
     */
    private static class SpyAuditService<T> implements AuditService<T> {

        final List<String> actions = new ArrayList<>();

        @Override
        public void log(String action, T target) {
            actions.add(action);
        }

        @Override
        public List<AuditEntry<T>> getLog() {
            return List.of();
        }
    }
}
