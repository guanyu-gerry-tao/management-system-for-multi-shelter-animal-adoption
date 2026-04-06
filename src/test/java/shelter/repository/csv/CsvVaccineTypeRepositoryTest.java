package shelter.repository.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shelter.domain.Species;
import shelter.domain.VaccineType;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvVaccineTypeRepository}, verifying all CRUD operations and query methods
 * using a temporary directory so no real filesystem state is affected.
 */
class CsvVaccineTypeRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvVaccineTypeRepository repo;

    /**
     * Creates a fresh repository backed by the JUnit temp directory before each test.
     * This guarantees tests are isolated and never share persisted state.
     */
    @BeforeEach
    void setUp() {
        repo = new CsvVaccineTypeRepository(tempDir.toString());
    }

    /**
     * Verifies that a saved VaccineType can be retrieved by ID with all fields intact.
     */
    @Test
    void saveAndFindById_returnsSameVaccineType() {
        VaccineType vt = new VaccineType("Rabies", Species.DOG, 365);
        repo.save(vt);

        Optional<VaccineType> found = repo.findById(vt.getId());
        assertTrue(found.isPresent());
        assertEquals(vt.getId(), found.get().getId());
        assertEquals("Rabies", found.get().getName());
        assertEquals(Species.DOG, found.get().getApplicableSpecies());
        assertEquals(365, found.get().getValidityDays());
    }

    /**
     * Verifies that findAll returns all saved VaccineType records.
     */
    @Test
    void findAll_returnsAllSaved() {
        repo.save(new VaccineType("Rabies", Species.DOG, 365));
        repo.save(new VaccineType("FVRCP", Species.CAT, 365));

        assertEquals(2, repo.findAll().size());
    }

    /**
     * Verifies that delete removes a VaccineType from the store.
     */
    @Test
    void delete_removesRecord() {
        VaccineType vt = new VaccineType("ToDelete", Species.RABBIT, 180);
        repo.save(vt);
        repo.delete(vt.getId());

        assertTrue(repo.findById(vt.getId()).isEmpty());
    }

    /**
     * Verifies that findById returns empty for an unknown ID.
     */
    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(repo.findById("unknown").isEmpty());
    }

    /**
     * Verifies that findByName performs a case-insensitive lookup.
     */
    @Test
    void findByName_caseInsensitive() {
        repo.save(new VaccineType("Rabies", Species.DOG, 365));

        Optional<VaccineType> found = repo.findByName("rabies");
        assertTrue(found.isPresent());
        assertEquals("Rabies", found.get().getName());
    }

    /**
     * Verifies that findByName returns empty when no match exists.
     */
    @Test
    void findByName_noMatch_returnsEmpty() {
        assertTrue(repo.findByName("Unknown Vaccine").isEmpty());
    }

    /**
     * Verifies that findByApplicableSpecies filters correctly by species.
     */
    @Test
    void findByApplicableSpecies_filtersCorrectly() {
        repo.save(new VaccineType("Rabies", Species.DOG, 365));
        repo.save(new VaccineType("FVRCP", Species.CAT, 365));
        repo.save(new VaccineType("DogBooster", Species.DOG, 180));

        List<VaccineType> dogs = repo.findByApplicableSpecies(Species.DOG);
        assertEquals(2, dogs.size());
        dogs.forEach(vt -> assertEquals(Species.DOG, vt.getApplicableSpecies()));
    }

    /**
     * Verifies CSV round-trip: records saved in one instance are readable in a new instance.
     */
    @Test
    void persistence_roundTrip() {
        VaccineType vt = new VaccineType("FVRCP", Species.CAT, 365);
        repo.save(vt);

        CsvVaccineTypeRepository repo2 = new CsvVaccineTypeRepository(tempDir.toString());
        assertTrue(repo2.findById(vt.getId()).isPresent());
        assertEquals("FVRCP", repo2.findById(vt.getId()).get().getName());
    }
}
