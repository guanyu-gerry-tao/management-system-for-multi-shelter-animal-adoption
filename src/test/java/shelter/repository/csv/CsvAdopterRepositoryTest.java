package shelter.repository.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shelter.domain.ActivityLevel;
import shelter.domain.Adopter;
import shelter.domain.AdopterPreferences;
import shelter.domain.DailySchedule;
import shelter.domain.LivingSpace;
import shelter.domain.Species;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvAdopterRepository}, verifying all CRUD operations and CSV round-trip
 * fidelity using a JUnit {@code @TempDir}.
 */
class CsvAdopterRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvAdopterRepository repo;

    /**
     * Creates a fresh repository backed by the JUnit temp directory before each test.
     * This guarantees tests are isolated and never share persisted state.
     */
    @BeforeEach
    void setUp() {
        repo = new CsvAdopterRepository(tempDir.toString());
    }

    /** Builds a simple Adopter with no species/breed/activity preference and age range 0–10. */
    private Adopter buildAdopter(String name) {
        AdopterPreferences prefs = new AdopterPreferences(null, null, null, null, 0, 10);
        return new Adopter(name, LivingSpace.APARTMENT, DailySchedule.HOME_MOST_OF_DAY, null, prefs);
    }

    /**
     * Verifies that a saved Adopter can be retrieved by ID with all fields intact.
     */
    @Test
    void saveAndFindById_returnsCorrectAdopter() {
        AdopterPreferences prefs = new AdopterPreferences(
                Species.DOG, "Labrador", ActivityLevel.HIGH, null, 1, 5);
        Adopter a = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, "Loves dogs", prefs);
        repo.save(a);

        Optional<Adopter> found = repo.findById(a.getId());
        assertTrue(found.isPresent());
        Adopter loaded = found.get();
        assertEquals(a.getId(), loaded.getId());
        assertEquals("Alice", loaded.getName());
        assertEquals(LivingSpace.HOUSE_WITH_YARD, loaded.getLivingSpace());
        assertEquals(DailySchedule.HOME_MOST_OF_DAY, loaded.getDailySchedule());
        assertEquals("Loves dogs", loaded.getPersonalNotes());
        assertEquals(Species.DOG, loaded.getPreferences().getPreferredSpecies());
        assertEquals("Labrador", loaded.getPreferences().getPreferredBreed());
        assertEquals(ActivityLevel.HIGH, loaded.getPreferences().getPreferredActivityLevel());
        assertEquals(1, loaded.getPreferences().getMinAge());
        assertEquals(5, loaded.getPreferences().getMaxAge());
    }

    /**
     * Verifies that findAll returns all saved adopters.
     */
    @Test
    void findAll_returnsAllSaved() {
        repo.save(buildAdopter("Alice"));
        repo.save(buildAdopter("Bob"));

        List<Adopter> all = repo.findAll();
        assertEquals(2, all.size());
    }

    /**
     * Verifies that delete removes the adopter from the store.
     */
    @Test
    void delete_removesAdopter() {
        Adopter a = buildAdopter("ToDelete");
        repo.save(a);
        repo.delete(a.getId());

        assertTrue(repo.findById(a.getId()).isEmpty());
        assertEquals(0, repo.findAll().size());
    }

    /**
     * Verifies that findById returns empty for an ID that was never saved.
     */
    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(repo.findById("no-such-id").isEmpty());
    }

    /**
     * Verifies that null preferences fields (species, breed, activity) are stored and restored
     * as null rather than throwing or producing garbage values.
     */
    @Test
    void nullPreferences_roundTrip() {
        AdopterPreferences prefs = new AdopterPreferences(null, null, null, null, 0, 15);
        Adopter a = new Adopter("NoPrefs", LivingSpace.APARTMENT,
                DailySchedule.AWAY_MOST_OF_DAY, null, prefs);
        repo.save(a);

        Adopter loaded = repo.findById(a.getId()).orElseThrow();
        assertNull(loaded.getPreferences().getPreferredSpecies());
        assertNull(loaded.getPreferences().getPreferredBreed());
        assertNull(loaded.getPreferences().getPreferredActivityLevel());
        assertNull(loaded.getPersonalNotes());
    }

    /**
     * Regression test for the nullable minAge/maxAge bug.
     * Before the fix, null age bounds were written as empty strings but parsed back with
     * {@code Integer.parseInt()} directly, throwing {@code NumberFormatException} on reload.
     * This verifies that null age bounds survive a full CSV round-trip without error.
     */
    @Test
    void nullAge_roundTrip() {
        AdopterPreferences prefs = new AdopterPreferences(null, null, null, null, null, null);
        Adopter a = new Adopter("NoAgePrefs", LivingSpace.APARTMENT,
                DailySchedule.AWAY_MOST_OF_DAY, null, prefs);
        repo.save(a);

        CsvAdopterRepository repo2 = new CsvAdopterRepository(tempDir.toString());
        Adopter loaded = repo2.findById(a.getId()).orElseThrow();
        assertNull(loaded.getPreferences().getMinAge());
        assertNull(loaded.getPreferences().getMaxAge());
    }

    /**
     * Verifies that adoptedAnimalIds are preserved through a CSV round-trip.
     */
    @Test
    void adoptedAnimalIds_roundTrip() {
        Adopter a = buildAdopter("WithAdoptions");
        a.addAdoptedAnimalId("animal-1");
        a.addAdoptedAnimalId("animal-2");
        repo.save(a);

        CsvAdopterRepository repo2 = new CsvAdopterRepository(tempDir.toString());
        Adopter loaded = repo2.findById(a.getId()).orElseThrow();
        assertEquals(List.of("animal-1", "animal-2"), loaded.getAdoptedAnimalIds());
    }

    /**
     * Verifies that an adopter with an empty adoptedAnimalIds list is stored and restored correctly.
     */
    @Test
    void emptyAdoptedAnimalIds_roundTrip() {
        Adopter a = buildAdopter("NoAdoptions");
        repo.save(a);

        CsvAdopterRepository repo2 = new CsvAdopterRepository(tempDir.toString());
        assertTrue(repo2.findById(a.getId()).orElseThrow().getAdoptedAnimalIds().isEmpty());
    }

    /**
     * Verifies CSV round-trip: data saved in one instance is readable in a new instance.
     */
    @Test
    void persistence_roundTrip() {
        Adopter a = buildAdopter("Persistent");
        repo.save(a);

        CsvAdopterRepository repo2 = new CsvAdopterRepository(tempDir.toString());
        assertTrue(repo2.findById(a.getId()).isPresent());
        assertEquals("Persistent", repo2.findById(a.getId()).get().getName());
    }

    /**
     * Verifies that saving an adopter with the same ID twice overwrites the first record.
     * This confirms the upsert semantics of save: the most recently saved state is the truth.
     */
    @Test
    void save_upsert_overwritesExistingRecord() {
        Adopter a = buildAdopter("Original");
        repo.save(a);

        Adopter updated = new Adopter(
                a.getId(), "Updated", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.AWAY_PART_OF_DAY, "updated notes",
                new AdopterPreferences(Species.CAT, null, null, null, 0, 10),
                List.of());
        repo.save(updated);

        assertEquals(1, repo.findAll().size());
        Adopter loaded = repo.findById(a.getId()).orElseThrow();
        assertEquals("Updated", loaded.getName());
        assertEquals(LivingSpace.HOUSE_WITH_YARD, loaded.getLivingSpace());
        assertEquals(Species.CAT, loaded.getPreferences().getPreferredSpecies());
    }

    /**
     * Verifies that deleting a non-existent ID does not throw an exception.
     * The operation should be a silent no-op when the record is not found.
     */
    @Test
    void delete_nonExistentId_doesNotThrow() {
        assertDoesNotThrow(() -> repo.delete("id-that-does-not-exist"));
    }

    /**
     * Verifies that adopter names containing commas and quotes survive a CSV round-trip.
     * This guards against CSV parsing bugs where special characters break field boundaries.
     */
    @Test
    void specialCharactersInName_roundTrip() {
        Adopter a = buildAdopter("Smith, John \"JJ\"");
        repo.save(a);

        Adopter loaded = repo.findById(a.getId()).orElseThrow();
        assertEquals("Smith, John \"JJ\"", loaded.getName());
    }
}
