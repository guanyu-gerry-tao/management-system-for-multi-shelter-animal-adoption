package shelter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;
import shelter.exception.EntityNotFoundException;
import shelter.repository.AdopterRepository;
import shelter.service.impl.AdopterServiceImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link shelter.service.impl.AdopterServiceImpl}.
 * Uses a stub repository backed by a HashMap to isolate service logic from persistence.
 */
class AdopterServiceImplTest {

    private static class StubAdopterRepository implements AdopterRepository {
        private final Map<String, Adopter> store = new HashMap<>();

        @Override public void save(Adopter a) { store.put(a.getId(), a); }
        @Override public Optional<Adopter> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Adopter> findAll() { return new ArrayList<>(store.values()); }
        @Override public void delete(String id) { store.remove(id); }
    }

    private AdopterServiceImpl service;
    private StubAdopterRepository repo;
    private Adopter adopter;

    @BeforeEach
    void setUp() {
        repo = new StubAdopterRepository();
        service = new AdopterServiceImpl(repo);
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(null, null, null, 0, 20));
    }

    @Test
    void register_newAdopter_saves() {
        service.register(adopter);
        assertTrue(repo.findById(adopter.getId()).isPresent());
    }

    @Test
    void register_duplicate_throws() {
        service.register(adopter);
        assertThrows(IllegalArgumentException.class, () -> service.register(adopter));
    }

    @Test
    void register_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null));
    }

    @Test
    void update_existingAdopter_saves() {
        repo.save(adopter);
        service.update(adopter);
        assertTrue(repo.findById(adopter.getId()).isPresent());
    }

    @Test
    void update_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.update(adopter));
    }

    @Test
    void remove_existingAdopter_deletes() {
        repo.save(adopter);
        service.remove(adopter);
        assertFalse(repo.findById(adopter.getId()).isPresent());
    }

    @Test
    void remove_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.remove(adopter));
    }

    @Test
    void findById_found_returnsAdopter() {
        repo.save(adopter);
        assertEquals(adopter, service.findById(adopter.getId()));
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
    void listAll_returnsAllAdopters() {
        repo.save(adopter);
        Adopter b = new Adopter("Bob", LivingSpace.APARTMENT, DailySchedule.AWAY_PART_OF_DAY,
                null, new AdopterPreferences(null, null, null, 0, 10));
        repo.save(b);
        assertEquals(2, service.listAll().size());
    }

    @Test
    void adoptedAnimal_returnsMatchingAdopters() {
        adopter.addAdoptedAnimalId("animal-001");
        repo.save(adopter);
        Dog dog = new Dog("animal-001", "Rex", "Lab", 2, ActivityLevel.MEDIUM, false, null, null, Dog.Size.LARGE, false);
        List<Adopter> result = service.adoptedAnimal(dog);
        assertEquals(1, result.size());
        assertEquals(adopter.getId(), result.get(0).getId());
    }

    @Test
    void adoptedAfter_returnsAdoptersWithAdoptions() {
        adopter.addAdoptedAnimalId("animal-001");
        repo.save(adopter);
        Adopter noAdoptions = new Adopter("Carol", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(null, null, null, 0, 10));
        repo.save(noAdoptions);
        List<Adopter> result = service.adoptedAfter(LocalDate.now().minusDays(1));
        assertEquals(1, result.size());
    }

    @Test
    void constructor_nullRepository_throws() {
        assertThrows(IllegalArgumentException.class, () -> new AdopterServiceImpl(null));
    }
}
