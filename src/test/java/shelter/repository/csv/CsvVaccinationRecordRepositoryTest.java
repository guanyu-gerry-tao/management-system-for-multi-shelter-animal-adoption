package shelter.repository.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shelter.domain.ActivityLevel;
import shelter.domain.Dog;
import shelter.domain.VaccinationRecord;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvVaccinationRecordRepository}, verifying all CRUD and query operations
 * using a JUnit {@code @TempDir} so tests never touch the real filesystem.
 */
class CsvVaccinationRecordRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvAnimalRepository animalRepo;
    private CsvVaccinationRecordRepository repo;

    /**
     * Creates fresh backing repositories before each test.
     * Uses a real {@link CsvAnimalRepository} so the shelter-based lookup is exercised end-to-end.
     */
    @BeforeEach
    void setUp() {
        animalRepo = new CsvAnimalRepository(tempDir.toString());
        repo = new CsvVaccinationRecordRepository(tempDir.toString(), animalRepo);
    }

    /**
     * Verifies that a saved VaccinationRecord can be retrieved by ID with all fields intact.
     */
    @Test
    void saveAndFindById_returnsSameRecord() {
        VaccinationRecord rec = new VaccinationRecord("animal-1", "vaccine-1", LocalDate.of(2025, 1, 15));
        repo.save(rec);

        Optional<VaccinationRecord> found = repo.findById(rec.getId());
        assertTrue(found.isPresent());
        assertEquals(rec.getId(), found.get().getId());
        assertEquals("animal-1", found.get().getAnimalId());
        assertEquals("vaccine-1", found.get().getVaccineTypeId());
        assertEquals(LocalDate.of(2025, 1, 15), found.get().getDateAdministered());
    }

    /**
     * Verifies that findAll returns all saved records.
     */
    @Test
    void findAll_returnsAllSaved() {
        repo.save(new VaccinationRecord("a1", "v1", LocalDate.of(2025, 1, 1)));
        repo.save(new VaccinationRecord("a2", "v2", LocalDate.of(2025, 2, 1)));

        assertEquals(2, repo.findAll().size());
    }

    /**
     * Verifies that delete removes the record from the store.
     */
    @Test
    void delete_removesRecord() {
        VaccinationRecord rec = new VaccinationRecord("a1", "v1", LocalDate.of(2025, 1, 1));
        repo.save(rec);
        repo.delete(rec.getId());

        assertTrue(repo.findById(rec.getId()).isEmpty());
    }

    /**
     * Verifies that findById returns empty for an unknown ID.
     */
    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(repo.findById("no-such-id").isEmpty());
    }

    /**
     * Verifies that findByAnimalId returns only records for the requested animal.
     */
    @Test
    void findByAnimalId_filtersCorrectly() {
        repo.save(new VaccinationRecord("animal-A", "v1", LocalDate.of(2025, 1, 1)));
        repo.save(new VaccinationRecord("animal-A", "v2", LocalDate.of(2025, 3, 1)));
        repo.save(new VaccinationRecord("animal-B", "v1", LocalDate.of(2025, 2, 1)));

        List<VaccinationRecord> forA = repo.findByAnimalId("animal-A");
        assertEquals(2, forA.size());
        forA.forEach(r -> assertEquals("animal-A", r.getAnimalId()));
    }

    /**
     * Verifies that findByAnimalId returns an empty list when there are no records for that animal.
     */
    @Test
    void findByAnimalId_noMatch_returnsEmpty() {
        assertTrue(repo.findByAnimalId("unknown-animal").isEmpty());
    }

    /**
     * Verifies findByShelterId returns records for animals in the specified shelter.
     */
    @Test
    void findByShelterId_returnsRecordsForAnimalsInShelter() {
        Dog dog = new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, Dog.Size.LARGE, false);
        dog.setShelterId("shelter-X");
        animalRepo.save(dog);

        VaccinationRecord rec = new VaccinationRecord(dog.getId(), "v1", LocalDate.of(2025, 1, 1));
        repo.save(rec);

        // Record for a different shelter
        repo.save(new VaccinationRecord("other-animal", "v2", LocalDate.of(2025, 1, 1)));

        List<VaccinationRecord> result = repo.findByShelterId("shelter-X");
        assertEquals(1, result.size());
        assertEquals(dog.getId(), result.get(0).getAnimalId());
    }

    /**
     * Verifies CSV round-trip: records saved in one instance are readable in a new instance.
     */
    @Test
    void persistence_roundTrip() {
        VaccinationRecord rec = new VaccinationRecord("a1", "v1", LocalDate.of(2025, 6, 15));
        repo.save(rec);

        CsvVaccinationRecordRepository repo2 =
                new CsvVaccinationRecordRepository(tempDir.toString(), animalRepo);
        assertTrue(repo2.findById(rec.getId()).isPresent());
        assertEquals(LocalDate.of(2025, 6, 15), repo2.findById(rec.getId()).get().getDateAdministered());
    }

    /**
     * Verifies that constructing the repository with a null AnimalRepository throws
     * {@link IllegalArgumentException}.
     */
    @Test
    void constructor_nullAnimalRepository_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new CsvVaccinationRecordRepository(tempDir.toString(), null));
    }
}
