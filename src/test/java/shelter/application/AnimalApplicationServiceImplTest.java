package shelter.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.application.impl.AnimalApplicationServiceImpl;
import shelter.domain.*;
import shelter.domain.Other;
import shelter.exception.EntityNotFoundException;
import shelter.service.AdoptionService;
import shelter.service.AnimalService;
import shelter.service.AuditService;
import shelter.service.ShelterService;
import shelter.service.model.AuditEntry;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AnimalApplicationServiceImpl}.
 * Verifies animal admission by species, listing, update, removal, and constructor guard conditions.
 */
class AnimalApplicationServiceImplTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private StubAnimalService animalService;
    private StubShelterService shelterService;
    private StubAdoptionService adoptionService;
    private SpyAuditService<Animal> auditService;
    private AnimalApplicationServiceImpl service;
    private Shelter shelter;

    @BeforeEach
    void setUp() {
        animalService   = new StubAnimalService();
        shelterService  = new StubShelterService();
        adoptionService = new StubAdoptionService();
        auditService    = new SpyAuditService<>();
        service         = new AnimalApplicationServiceImpl(animalService, shelterService, adoptionService, auditService);
        shelter         = new Shelter("Test Shelter", "Boston", 20);
        shelterService.store.put(shelter.getId(), shelter);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void admitDog_createsAndRegisters() {
        Dog result = service.admitDog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM,
                shelter.getId(), Dog.Size.LARGE, false);

        assertNotNull(result);
        assertInstanceOf(Dog.class, result);
        assertEquals(Dog.Size.LARGE, result.getSize());
        assertFalse(result.isNeutered());
        assertEquals(1, animalService.registerCount);
        assertTrue(auditService.actions.contains("admitted animal"));
    }

    @Test
    void admitCat_createsCat() {
        Cat result = service.admitCat("Luna", "Siamese", LocalDate.now().minusYears(2), ActivityLevel.LOW,
                shelter.getId(), true, true);

        assertInstanceOf(Cat.class, result);
        assertTrue(result.isIndoor());
        assertTrue(result.isNeutered());
    }

    @Test
    void admitRabbit_createsRabbit() {
        Rabbit result = service.admitRabbit("Bunny", "Dutch", LocalDate.now().minusYears(1), ActivityLevel.LOW,
                shelter.getId(), Rabbit.FurLength.SHORT);

        assertInstanceOf(Rabbit.class, result);
        assertEquals(Rabbit.FurLength.SHORT, result.getFurLength());
    }

    @Test
    void admitOther_createsOtherAnimal() {
        Other result = service.admitOther("Nemo", "Clownfish", LocalDate.now().minusYears(1), ActivityLevel.LOW,
                shelter.getId(), "fish");

        assertInstanceOf(Other.class, result);
        assertEquals("fish", result.getSpeciesName());
        assertEquals(Species.OTHER, result.getSpecies());
        assertEquals(1, animalService.registerCount);
        assertTrue(auditService.actions.contains("admitted animal"));
    }

    @Test
    void admitOther_shelterNotFound_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> service.admitOther("Nemo", "Clownfish", LocalDate.now().minusYears(1), ActivityLevel.LOW,
                        "missing", "fish"));
    }

    @Test
    void admitDog_shelterNotFound_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> service.admitDog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM,
                        "missing", Dog.Size.MEDIUM, false));
    }

    @Test
    void listAnimals_withShelterId_filtersByShelter() {
        Dog dog = new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        dog.setShelterId(shelter.getId());
        animalService.store.put(dog.getId(), dog);

        List<Animal> result = service.listAnimals(shelter.getId());

        assertEquals(1, result.size());
    }

    @Test
    void listAnimals_nullShelterId_returnsAll() {
        animalService.store.put("a1", new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false));
        animalService.store.put("a2", new Cat("Luna", "Persian", LocalDate.now().minusYears(2), ActivityLevel.LOW, false, true, false));

        assertEquals(2, service.listAnimals(null).size());
    }

    @Test
    void updateAnimal_mergesFields() {
        Dog dog = new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        animalService.store.put(dog.getId(), dog);

        Animal updated = service.updateAnimal(dog.getId(), "Max", null);

        assertEquals("Max", updated.getName());
        assertEquals("Lab", updated.getBreed()); // unchanged
        assertEquals(1, animalService.updateCount);
        assertTrue(auditService.actions.contains("updated animal"));
    }

    @Test
    void updateAnimal_notFound_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> service.updateAnimal("missing", "Max", null));
    }

    @Test
    void removeAnimal_delegatesAndLogs() {
        Dog dog = new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        animalService.store.put(dog.getId(), dog);

        service.removeAnimal(dog.getId());

        assertEquals(1, animalService.removeCount);
        assertTrue(auditService.actions.contains("removed animal"));
    }

    @Test
    void removeAnimal_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.removeAnimal("missing"));
    }

    @Test
    void constructor_nullArgument_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnimalApplicationServiceImpl(null, shelterService, adoptionService, auditService));
        assertThrows(IllegalArgumentException.class,
                () -> new AnimalApplicationServiceImpl(animalService, null, adoptionService, auditService));
        assertThrows(IllegalArgumentException.class,
                () -> new AnimalApplicationServiceImpl(animalService, shelterService, null, auditService));
        assertThrows(IllegalArgumentException.class,
                () -> new AnimalApplicationServiceImpl(animalService, shelterService, adoptionService, null));
    }

    // -------------------------------------------------------------------------
    // Stubs
    // -------------------------------------------------------------------------

    /**
     * Stub implementation of {@link AnimalService} backed by an in-memory map.
     * Counters track how many times each mutating method was invoked.
     */
    private static class StubAnimalService implements AnimalService {

        final Map<String, Animal> store = new HashMap<>();
        int registerCount;
        int updateCount;
        int removeCount;

        @Override
        public void register(Animal a, Shelter s) {
            store.put(a.getId(), a);
            registerCount++;
        }

        @Override
        public void update(Animal a) {
            store.put(a.getId(), a);
            updateCount++;
        }

        @Override
        public void remove(Animal a) {
            store.remove(a.getId());
            removeCount++;
        }

        @Override
        public Animal findById(String id) {
            Animal a = store.get(id);
            if (a == null) throw new EntityNotFoundException("Animal not found: " + id);
            return a;
        }

        @Override
        public List<Animal> getAnimalsByShelter(Shelter s) {
            if (s == null) return new ArrayList<>(store.values());
            List<Animal> result = new ArrayList<>();
            for (Animal a : store.values()) {
                if (s.getId().equals(a.getShelterId())) result.add(a);
            }
            return result;
        }

        @Override
        public List<Animal> registeredAfter(LocalDate d) {
            return new ArrayList<>(store.values());
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
     * Stub implementation of {@link ShelterService} backed by an in-memory map.
     * Only {@code register} and {@code findById} are exercised in these tests.
     */
    private static class StubShelterService implements ShelterService {

        final Map<String, Shelter> store = new HashMap<>();

        @Override
        public void register(Shelter s) {
            store.put(s.getId(), s);
        }

        @Override
        public void update(Shelter s) {
            store.put(s.getId(), s);
        }

        @Override
        public void remove(Shelter s) {
            store.remove(s.getId());
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
     * Stub implementation of {@link AdoptionService} that returns empty lists for all queries.
     * Used to satisfy the dependency without simulating pending requests in most tests.
     */
    private static class StubAdoptionService implements AdoptionService {
        @Override public void submit(shelter.domain.AdoptionRequest r) {}
        @Override public void approve(shelter.domain.AdoptionRequest r) {}
        @Override public void reject(shelter.domain.AdoptionRequest r) {}
        @Override public void cancel(shelter.domain.AdoptionRequest r) {}
        @Override public List<shelter.domain.AdoptionRequest> getRequestsByAdopter(Adopter a) { return List.of(); }
        @Override public List<shelter.domain.AdoptionRequest> getRequestsByShelter(Shelter s) { return List.of(); }
        @Override public List<shelter.domain.AdoptionRequest> getRequestsByAnimal(Animal a) { return List.of(); }
        @Override public List<shelter.domain.AdoptionRequest> getRequestsAfter(LocalDate d) { return List.of(); }
        @Override public List<shelter.domain.AdoptionRequest> getApprovedAfter(LocalDate d) { return List.of(); }
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
