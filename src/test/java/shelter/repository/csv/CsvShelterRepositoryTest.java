package shelter.repository.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shelter.domain.Shelter;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvShelterRepository}, verifying all CRUD operations and query methods
 * using a temporary directory so no real filesystem state is affected by the tests.
 */
class CsvShelterRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvShelterRepository repo;

    /**
     * Creates a fresh repository backed by the JUnit temp directory before each test.
     * This guarantees tests are isolated and never share persisted state.
     */
    @BeforeEach
    void setUp() {
        repo = new CsvShelterRepository(tempDir.toString());
    }

    /**
     * Verifies that a saved shelter can be retrieved by its ID with all fields intact.
     */
    @Test
    void saveAndFindById_returnsSameShelter() {
        Shelter s = new Shelter("Happy Paws", "Boston", 20);
        repo.save(s);

        Optional<Shelter> found = repo.findById(s.getId());
        assertTrue(found.isPresent());
        assertEquals(s.getId(), found.get().getId());
        assertEquals("Happy Paws", found.get().getName());
        assertEquals("Boston", found.get().getLocation());
        assertEquals(20, found.get().getCapacity());
    }

    /**
     * Verifies that findAll returns all shelters that have been saved.
     */
    @Test
    void findAll_returnsAllSavedShelters() {
        Shelter s1 = new Shelter("Alpha", "City A", 10);
        Shelter s2 = new Shelter("Beta",  "City B", 15);
        repo.save(s1);
        repo.save(s2);

        List<Shelter> all = repo.findAll();
        assertEquals(2, all.size());
    }

    /**
     * Verifies that deleting a shelter removes it from the store and findById returns empty.
     */
    @Test
    void delete_removesFromStore() {
        Shelter s = new Shelter("ToDelete", "Somewhere", 5);
        repo.save(s);
        repo.delete(s.getId());

        assertTrue(repo.findById(s.getId()).isEmpty());
        assertEquals(0, repo.findAll().size());
    }

    /**
     * Verifies that findById returns an empty Optional for an ID that was never saved.
     */
    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(repo.findById("nonexistent-id").isEmpty());
    }

    /**
     * Verifies that data persisted in one repository instance is readable by a second instance
     * pointing to the same directory, confirming CSV round-trip correctness.
     */
    @Test
    void persistence_roundTrip_surviveNewInstance() {
        Shelter s = new Shelter("Persisted", "Testville", 8);
        repo.save(s);

        CsvShelterRepository repo2 = new CsvShelterRepository(tempDir.toString());
        Optional<Shelter> found = repo2.findById(s.getId());
        assertTrue(found.isPresent());
        assertEquals("Persisted", found.get().getName());
    }

    /**
     * Verifies that saving a shelter twice (same ID) overwrites the previous record.
     */
    @Test
    void save_overwritesExistingRecord() {
        Shelter s = new Shelter("Original", "Place", 10);
        repo.save(s);
        // shelters are immutable after construction, so just save again (upsert path)
        repo.save(s);
        assertEquals(1, repo.findAll().size());
    }

    /**
     * Verifies that constructing the repository with a null dataDir throws
     * {@link IllegalArgumentException}.
     */
    @Test
    void constructor_nullDataDir_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new CsvShelterRepository(null));
    }

    /**
     * Verifies that findById with a null ID throws {@link IllegalArgumentException}.
     */
    @Test
    void findById_nullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> repo.findById(null));
    }
}
