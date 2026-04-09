package shelter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;
import shelter.exception.EntityNotFoundException;
import shelter.repository.AnimalRepository;
import shelter.service.impl.AnimalServiceImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link shelter.service.impl.AnimalServiceImpl}.
 * Uses a stub repository backed by a HashMap to isolate service logic from persistence.
 */
class AnimalServiceImplTest {

    // --- Stub repository ---

    private static class StubAnimalRepository implements AnimalRepository {
        private final Map<String, Animal> store = new HashMap<>();

        @Override public void save(Animal a) { store.put(a.getId(), a); } // Animal is abstract; copy handled by subtype constructors
        @Override public Optional<Animal> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Animal> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Animal> findByShelterId(String sid) {
            List<Animal> result = new ArrayList<>();
            for (Animal a : store.values()) if (sid.equals(a.getShelterId())) result.add(a);
            return result;
        }
        @Override public List<Animal> findByAdopterId(String aid) {
            List<Animal> result = new ArrayList<>();
            for (Animal a : store.values()) if (aid.equals(a.getAdopterId())) result.add(a);
            return result;
        }
        @Override public void delete(String id) { store.remove(id); }
    }

    private AnimalServiceImpl service;
    private StubAnimalRepository repo;
    private Shelter shelter;
    private Dog dog;

    @BeforeEach
    void setUp() {
        repo = new StubAnimalRepository();
        service = new AnimalServiceImpl(repo);
        shelter = new Shelter("Test Shelter", "Boston", 10);
        dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
    }

    @Test
    void register_savesAnimalAndAddsToShelter() {
        service.register(dog, shelter);
        assertTrue(repo.findById(dog.getId()).isPresent());
        assertTrue(shelter.containsAnimal(dog.getId()));
    }

    @Test
    void register_nullAnimal_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, shelter));
    }

    @Test
    void register_nullShelter_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.register(dog, null));
    }

    @Test
    void update_existingAnimal_saves() {
        repo.save(dog);
        service.update(dog);
        assertTrue(repo.findById(dog.getId()).isPresent());
    }

    @Test
    void update_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.update(dog));
    }

    @Test
    void remove_existingAnimal_deletes() {
        repo.save(dog);
        service.remove(dog);
        assertFalse(repo.findById(dog.getId()).isPresent());
    }

    @Test
    void remove_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.remove(dog));
    }

    @Test
    void findById_found_returnsAnimal() {
        repo.save(dog);
        assertEquals(dog, service.findById(dog.getId()));
    }

    @Test
    void findById_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.findById("missing"));
    }

    @Test
    void findById_blankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.findById("  "));
    }

    @Test
    void getAnimalsByShelter_returnsMatchingAnimals() {
        dog.setShelterId(shelter.getId());
        repo.save(dog);
        List<Animal> result = service.getAnimalsByShelter(shelter);
        assertEquals(1, result.size());
        assertEquals(dog.getId(), result.get(0).getId());
    }

    @Test
    void adoptedBy_returnsAnimalsWithMatchingAdopterId() {
        Adopter adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(null, null, null, 0, 20));
        dog.setAdopterId(adopter.getId());
        repo.save(dog);
        List<Animal> result = service.adoptedBy(adopter);
        assertEquals(1, result.size());
    }

    @Test
    void constructor_nullRepository_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AnimalServiceImpl(null));
    }

    @Test
    void registeredAfter_returnsAllAnimals() {
        repo.save(dog);
        assertFalse(service.registeredAfter(LocalDate.now().minusDays(1)).isEmpty());
    }

    @Test
    void adoptedAfter_returnsOnlyAdoptedAnimals() {
        repo.save(dog);
        Dog adopted = new Dog("Buddy", "Poodle", LocalDate.now().minusYears(2), ActivityLevel.LOW, true, Dog.Size.SMALL, true);
        adopted.setAdopterId("some-adopter-id");
        repo.save(adopted);
        List<Animal> result = service.adoptedAfter(LocalDate.now().minusDays(1));
        assertEquals(1, result.size());
        assertEquals(adopted.getId(), result.get(0).getId());
    }
}
