package shelter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.Shelter;
import shelter.exception.EntityNotFoundException;
import shelter.repository.ShelterRepository;
import shelter.service.impl.ShelterServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link shelter.service.impl.ShelterServiceImpl}.
 * Uses a stub repository backed by a HashMap to isolate service logic from persistence.
 */
class ShelterServiceImplTest {

    private static class StubShelterRepository implements ShelterRepository {
        private final Map<String, Shelter> store = new HashMap<>();

        @Override public void save(Shelter s) { store.put(s.getId(), new Shelter(s)); }
        @Override public Optional<Shelter> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Shelter> findAll() { return new ArrayList<>(store.values()); }
        @Override public void delete(String id) { store.remove(id); }
    }

    private ShelterServiceImpl service;
    private StubShelterRepository repo;
    private Shelter shelter;

    @BeforeEach
    void setUp() {
        repo = new StubShelterRepository();
        service = new ShelterServiceImpl(repo);
        shelter = new Shelter("Happy Paws", "Boston", 20);
    }

    @Test
    void register_newShelter_saves() {
        service.register(shelter);
        assertTrue(repo.findById(shelter.getId()).isPresent());
    }

    @Test
    void register_duplicate_throws() {
        service.register(shelter);
        Shelter duplicate = new Shelter("Happy Paws", "Boston", 10);
        assertThrows(IllegalArgumentException.class, () -> service.register(duplicate));
    }

    @Test
    void register_nullShelter_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null));
    }

    @Test
    void update_existingShelter_saves() {
        repo.save(shelter);
        service.update(shelter);
        assertTrue(repo.findById(shelter.getId()).isPresent());
    }

    @Test
    void update_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.update(shelter));
    }

    @Test
    void remove_emptyShelter_deletes() {
        repo.save(shelter);
        service.remove(shelter);
        assertFalse(repo.findById(shelter.getId()).isPresent());
    }

    @Test
    void remove_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.remove(shelter));
    }

    @Test
    void findById_found_returnsShelter() {
        repo.save(shelter);
        assertEquals(shelter, service.findById(shelter.getId()));
    }

    @Test
    void findById_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.findById("missing"));
    }

    @Test
    void findById_blankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.findById(""));
    }

    @Test
    void listAll_returnsAllShelters() {
        repo.save(shelter);
        Shelter s2 = new Shelter("Safe Haven", "Cambridge", 15);
        repo.save(s2);
        assertEquals(2, service.listAll().size());
    }

    @Test
    void constructor_nullRepository_throws() {
        assertThrows(IllegalArgumentException.class, () -> new ShelterServiceImpl(null));
    }
}
