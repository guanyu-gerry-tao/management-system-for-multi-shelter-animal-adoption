package shelter.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.application.impl.MatchingApplicationServiceImpl;
import shelter.domain.*;
import shelter.domain.Other;
import shelter.exception.AnimalNotAvailableException;
import shelter.exception.EntityNotFoundException;
import shelter.service.*;
import shelter.service.model.AuditEntry;
import shelter.service.model.ExplanationResult;
import shelter.service.model.MatchResult;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MatchingApplicationServiceImpl}.
 * Verifies adopter-based and animal-based matching, explanation delegation, and guard conditions.
 */
class MatchingApplicationServiceImplTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private StubAnimalService animalService;
    private StubAdopterService adopterService;
    private StubShelterService shelterService;
    private SpyExplanationService explanationService;
    private MatchingApplicationServiceImpl service;
    private Adopter adopter;
    private Dog dog;
    private Shelter shelter;

    @BeforeEach
    void setUp() {
        animalService      = new StubAnimalService();
        adopterService     = new StubAdopterService();
        shelterService     = new StubShelterService();
        explanationService = new SpyExplanationService();
        service = new MatchingApplicationServiceImpl(
                new StubAdopterBasedMatchingService(),
                new StubAnimalBasedMatchingService(),
                animalService, adopterService, shelterService, explanationService);

        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(null, null, null, null, 0, 20));
        shelter = new Shelter("Test Shelter", "Boston", 20);
        dog     = new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        dog.setShelterId(shelter.getId());

        adopterService.store.put(adopter.getId(), adopter);
        shelterService.store.put(shelter.getId(), shelter);
        animalService.store.put(dog.getId(), dog);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void matchAnimalsForAdopter_returnsRankedResults() {
        List<MatchResult> results = service.matchAnimalsForAdopter(
                adopter.getId(), shelter.getId(), false);

        assertEquals(1, results.size());
        assertEquals(100, results.get(0).getScore());
    }

    @Test
    void matchAnimalsForAdopter_filtersOutAdoptedAnimals() {
        dog.setAdopterId("someone");

        List<MatchResult> results = service.matchAnimalsForAdopter(
                adopter.getId(), shelter.getId(), false);

        assertTrue(results.isEmpty());
    }

    @Test
    void matchAnimalsForAdopter_withExplanation_callsExplanationService() {
        service.matchAnimalsForAdopter(adopter.getId(), shelter.getId(), true);

        assertEquals(1, explanationService.callCount);
    }

    @Test
    void matchAnimalsForAdopter_withoutExplanation_doesNotCallExplanation() {
        service.matchAnimalsForAdopter(adopter.getId(), shelter.getId(), false);

        assertEquals(0, explanationService.callCount);
    }

    @Test
    void matchAdoptersForAnimal_returnsRankedResults() {
        List<MatchResult> results = service.matchAdoptersForAnimal(dog.getId(), false);

        assertEquals(1, results.size());
    }

    @Test
    void matchAdoptersForAnimal_alreadyAdopted_throws() {
        dog.setAdopterId("someone");

        assertThrows(AnimalNotAvailableException.class,
                () -> service.matchAdoptersForAnimal(dog.getId(), false));
    }

    @Test
    void matchAnimalsForAdopter_filtersOutOtherAnimals() {
        Other fish = new Other("Nemo", "Clownfish", LocalDate.now().minusYears(1), ActivityLevel.LOW, false, "fish");
        fish.setShelterId(shelter.getId());
        animalService.store.put(fish.getId(), fish);

        // shelter now has dog + fish; only dog should be in results
        List<MatchResult> results = service.matchAnimalsForAdopter(
                adopter.getId(), shelter.getId(), false);

        assertEquals(1, results.size());
        assertInstanceOf(Dog.class, results.get(0).getAnimal());
    }

    @Test
    void matchAdoptersForAnimal_otherAnimal_throws() {
        Other fish = new Other("Nemo", "Clownfish", LocalDate.now().minusYears(1), ActivityLevel.LOW, false, "fish");
        animalService.store.put(fish.getId(), fish);

        assertThrows(IllegalArgumentException.class,
                () -> service.matchAdoptersForAnimal(fish.getId(), false));
    }

    @Test
    void matchAdoptersForAnimal_notFound_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> service.matchAdoptersForAnimal("missing", false));
    }

    // -------------------------------------------------------------------------
    // Stubs
    // -------------------------------------------------------------------------

    /**
     * Stub implementation of {@link AnimalService} backed by an in-memory map.
     * Returns shelter-filtered animals for {@code getAnimalsByShelter}.
     */
    private static class StubAnimalService implements AnimalService {

        final Map<String, Animal> store = new HashMap<>();

        @Override
        public void register(Animal a, Shelter s) {
            store.put(a.getId(), a);
        }

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
     * Stub implementation of {@link AdopterService} backed by an in-memory map.
     * Returns all registered adopters for {@code listAll}.
     */
    private static class StubAdopterService implements AdopterService {

        final Map<String, Adopter> store = new HashMap<>();

        @Override
        public void register(Adopter a) {
            store.put(a.getId(), a);
        }

        @Override
        public void update(Adopter a) {}

        @Override
        public void remove(Adopter a) {}

        @Override
        public List<Adopter> listAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Adopter findById(String id) {
            Adopter a = store.get(id);
            if (a == null) throw new EntityNotFoundException("not found: " + id);
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
     * Stub implementation of {@link ShelterService} backed by an in-memory map.
     * Only the {@code findById} path is exercised in these tests.
     */
    private static class StubShelterService implements ShelterService {

        final Map<String, Shelter> store = new HashMap<>();

        @Override
        public void register(Shelter s) {
            store.put(s.getId(), s);
        }

        @Override
        public void update(Shelter s) {}

        @Override
        public void remove(Shelter s) {}

        @Override
        public Shelter findById(String id) {
            Shelter s = store.get(id);
            if (s == null) throw new EntityNotFoundException("not found: " + id);
            return s;
        }

        @Override
        public List<Shelter> listAll() {
            return new ArrayList<>(store.values());
        }
    }

    /**
     * Stub implementation of {@link AdopterBasedMatchingService} that returns a fixed score of 100
     * for every animal in the candidate list, regardless of adopter preferences.
     */
    private static class StubAdopterBasedMatchingService implements AdopterBasedMatchingService {

        @Override
        public List<MatchResult> match(Adopter adopter, List<Animal> animals) {
            List<MatchResult> results = new ArrayList<>();
            for (Animal a : animals) results.add(new MatchResult(a, adopter, 100));
            return results;
        }
    }

    /**
     * Stub implementation of {@link AnimalBasedMatchingService} that returns a fixed score of 100
     * for every adopter in the candidate list, regardless of animal characteristics.
     */
    private static class StubAnimalBasedMatchingService implements AnimalBasedMatchingService {

        @Override
        public List<MatchResult> match(Animal animal, List<Adopter> adopters) {
            List<MatchResult> results = new ArrayList<>();
            for (Adopter a : adopters) results.add(new MatchResult(animal, a, 100));
            return results;
        }
    }

    /**
     * Spy implementation of {@link ExplanationService} that counts how many times it was called.
     * Use {@link #callCount} to assert whether explanation was requested.
     */
    private static class SpyExplanationService implements ExplanationService {

        int callCount;

        @Override
        public ExplanationResult explain(List<MatchResult> results) {
            callCount++;
            return new ExplanationResult("rationale", "confidence", "advice");
        }
    }
}
