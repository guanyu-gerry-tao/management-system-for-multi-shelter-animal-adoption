package shelter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.Species;
import shelter.domain.VaccineType;
import shelter.exception.EntityNotFoundException;
import shelter.repository.VaccineTypeRepository;
import shelter.service.impl.VaccineTypeCatalogServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link shelter.service.impl.VaccineTypeCatalogServiceImpl}.
 * Uses a stub repository backed by a HashMap to isolate service logic from persistence.
 */
class VaccineTypeCatalogServiceImplTest {

    private static class StubVaccineTypeRepository implements VaccineTypeRepository {
        private final Map<String, VaccineType> store = new HashMap<>();

        @Override public void save(VaccineType v) { store.put(v.getId(), new VaccineType(v)); }
        @Override public Optional<VaccineType> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<VaccineType> findByName(String name) {
            return store.values().stream().filter(v -> v.getName().equals(name)).findFirst();
        }
        @Override public List<VaccineType> findByApplicableSpecies(Species s) {
            List<VaccineType> result = new ArrayList<>();
            for (VaccineType v : store.values()) if (v.getApplicableSpecies() == s) result.add(v);
            return result;
        }
        @Override public List<VaccineType> findAll() { return new ArrayList<>(store.values()); }
        @Override public void delete(String id) { store.remove(id); }
    }

    private VaccineTypeCatalogServiceImpl service;
    private StubVaccineTypeRepository repo;
    private VaccineType rabies;

    @BeforeEach
    void setUp() {
        repo = new StubVaccineTypeRepository();
        service = new VaccineTypeCatalogServiceImpl(repo);
        rabies = new VaccineType("Rabies", Species.DOG, 365);
    }

    @Test
    void add_newVaccineType_saves() {
        service.add(rabies);
        assertTrue(repo.findById(rabies.getId()).isPresent());
    }

    @Test
    void add_duplicateName_throws() {
        service.add(rabies);
        VaccineType dup = new VaccineType("Rabies", Species.CAT, 180);
        assertThrows(IllegalArgumentException.class, () -> service.add(dup));
    }

    @Test
    void add_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.add(null));
    }

    @Test
    void update_existingVaccineType_saves() {
        repo.save(rabies);
        service.update(rabies);
        assertTrue(repo.findById(rabies.getId()).isPresent());
    }

    @Test
    void update_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.update(rabies));
    }

    @Test
    void update_nameConflictWithDifferentId_throws() {
        repo.save(rabies);
        VaccineType other = new VaccineType("Distemper", Species.DOG, 365);
        repo.save(other);
        // Pass a new object with the same ID as other but a name that collides with rabies
        VaccineType conflicting = new VaccineType(other.getId(), "Rabies", Species.DOG, 365);
        assertThrows(IllegalArgumentException.class, () -> service.update(conflicting));
    }

    @Test
    void remove_existingId_deletes() {
        repo.save(rabies);
        service.remove(rabies.getId());
        assertFalse(repo.findById(rabies.getId()).isPresent());
    }

    @Test
    void remove_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.remove("missing"));
    }

    @Test
    void remove_blankId_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.remove("  "));
    }

    @Test
    void findById_found_returns() {
        repo.save(rabies);
        assertEquals(rabies, service.findById(rabies.getId()));
    }

    @Test
    void findById_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.findById("missing"));
    }

    @Test
    void findByName_found_returns() {
        repo.save(rabies);
        assertEquals(rabies, service.findByName("Rabies"));
    }

    @Test
    void findByName_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.findByName("Unknown"));
    }

    @Test
    void listAll_returnsAllTypes() {
        repo.save(rabies);
        repo.save(new VaccineType("FVRCP", Species.CAT, 365));
        assertEquals(2, service.listAll().size());
    }

    @Test
    void constructor_nullRepository_throws() {
        assertThrows(IllegalArgumentException.class, () -> new VaccineTypeCatalogServiceImpl(null));
    }
}
