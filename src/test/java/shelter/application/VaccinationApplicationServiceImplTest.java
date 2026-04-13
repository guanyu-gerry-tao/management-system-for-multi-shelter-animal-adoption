package shelter.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.application.impl.VaccinationApplicationServiceImpl;
import shelter.domain.*;
import shelter.exception.EntityNotFoundException;
import shelter.service.*;
import shelter.service.model.AuditEntry;
import shelter.service.model.OverdueVaccination;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link VaccinationApplicationServiceImpl}.
 * Verifies vaccination recording, overdue checks, vaccine type CRUD, and guard conditions.
 */
class VaccinationApplicationServiceImplTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private StubVaccinationService vaccinationService;
    private StubVaccineTypeCatalogService catalogService;
    private StubAnimalService animalService;
    private SpyAuditService<Object> auditService;
    private VaccinationApplicationServiceImpl service;
    private Dog dog;
    private VaccineType rabies;

    @BeforeEach
    void setUp() {
        vaccinationService = new StubVaccinationService();
        catalogService     = new StubVaccineTypeCatalogService();
        animalService      = new StubAnimalService();
        auditService       = new SpyAuditService<>();
        service = new VaccinationApplicationServiceImpl(
                vaccinationService, catalogService, animalService, auditService);

        dog    = new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        rabies = new VaccineType("Rabies", Species.DOG, 365);
        animalService.store.put(dog.getId(), dog);
        catalogService.store.put(rabies.getId(), rabies);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void recordVaccination_delegatesAndLogs() {
        service.recordVaccination(dog.getId(), "Rabies", LocalDate.now());

        assertEquals(1, vaccinationService.recordCount);
        assertTrue(auditService.actions.contains("recorded vaccination"));
    }

    @Test
    void recordVaccination_animalNotFound_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> service.recordVaccination("missing", "Rabies", LocalDate.now()));
    }

    @Test
    void recordVaccination_vaccineTypeNotFound_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> service.recordVaccination(dog.getId(), "Unknown", LocalDate.now()));
    }

    @Test
    void getOverdueVaccinations_returnsResults() {
        List<OverdueVaccination> result = service.getOverdueVaccinations(dog.getId());

        assertFalse(result.isEmpty());
    }

    @Test
    void addVaccineType_persistsAndLogs() {
        VaccineType result = service.addVaccineType("FVRCP", Species.CAT, 365);

        assertNotNull(result);
        assertEquals(1, catalogService.addCount);
        assertTrue(auditService.actions.contains("added vaccine type"));
    }

    @Test
    void updateVaccineType_mergesAndLogs() {
        VaccineType updated = service.updateVaccineType(rabies.getId(), "Rabies-Updated", null, null);

        assertEquals("Rabies-Updated", updated.getName());
        assertEquals(1, catalogService.updateCount);
        assertTrue(auditService.actions.contains("updated vaccine type"));
    }

    @Test
    void removeVaccineType_delegatesAndLogs() {
        service.removeVaccineType(rabies.getId());

        assertEquals(1, catalogService.removeCount);
        assertTrue(auditService.actions.contains("removed vaccine type"));
    }

    @Test
    void listVaccineTypes_returnsAll() {
        assertEquals(1, service.listVaccineTypes().size());
    }

    // -------------------------------------------------------------------------
    // Stubs
    // -------------------------------------------------------------------------

    /**
     * Stub implementation of {@link VaccinationService} that counts {@code recordVaccination} calls
     * and returns a fixed overdue result from {@code getOverdueVaccinations}.
     */
    private static class StubVaccinationService implements VaccinationService {

        int recordCount;

        @Override
        public void recordVaccination(Animal a, VaccineType v, LocalDate d) {
            recordCount++;
        }

        @Override
        public List<OverdueVaccination> getOverdueVaccinations(Animal a) {
            return List.of(new OverdueVaccination(
                    new VaccineType("Rabies", Species.DOG, 365), null, LocalDate.now().minusDays(10)));
        }

        @Override
        public List<VaccinationRecord> getVaccinationHistory(Animal a) {
            return List.of();
        }

        @Override
        public VaccinationRecord findById(String id) {
            return null;
        }
    }

    /**
     * Stub implementation of {@link VaccineTypeCatalogService} backed by an in-memory map.
     * Counters track how many times each mutating method was invoked.
     */
    private static class StubVaccineTypeCatalogService implements VaccineTypeCatalogService {

        final Map<String, VaccineType> store = new HashMap<>();
        int addCount;
        int updateCount;
        int removeCount;

        @Override
        public void add(VaccineType v) {
            store.put(v.getId(), new VaccineType(v));
            addCount++;
        }

        @Override
        public void update(VaccineType v) {
            store.put(v.getId(), new VaccineType(v));
            updateCount++;
        }

        @Override
        public void remove(String id) {
            if (!store.containsKey(id)) throw new EntityNotFoundException("not found: " + id);
            store.remove(id);
            removeCount++;
        }

        @Override
        public VaccineType findById(String id) {
            VaccineType v = store.get(id);
            if (v == null) throw new EntityNotFoundException("not found: " + id);
            return v;
        }

        @Override
        public VaccineType findByName(String name) {
            return store.values().stream()
                    .filter(v -> v.getName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("not found: " + name));
        }

        @Override
        public List<VaccineType> listAll() {
            return new ArrayList<>(store.values());
        }
    }

    /**
     * Stub implementation of {@link AnimalService} backed by an in-memory map.
     * Only the {@code findById} path is exercised in these tests.
     */
    private static class StubAnimalService implements AnimalService {

        final Map<String, Animal> store = new HashMap<>();

        @Override
        public void register(Animal a, Shelter s) {}

        @Override
        public void update(Animal a) {}

        @Override
        public void remove(Animal a) {}

        @Override
        public Animal findById(String id) {
            Animal a = store.get(id);
            if (a == null) throw new EntityNotFoundException("not found: " + id);
            return a;
        }

        @Override
        public List<Animal> getAnimalsByShelter(Shelter s) {
            return List.of();
        }

        @Override
        public List<Animal> registeredAfter(LocalDate d) {
            return List.of();
        }

        @Override
        public List<Animal> adoptedAfter(LocalDate d) {
            return List.of();
        }

        @Override
        public List<Animal> adoptedBy(Adopter a) {
            return List.of();
        }
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
