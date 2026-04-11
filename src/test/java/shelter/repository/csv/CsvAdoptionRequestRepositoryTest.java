package shelter.repository.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shelter.domain.ActivityLevel;
import shelter.domain.Adopter;
import shelter.domain.AdopterPreferences;
import shelter.domain.AdoptionRequest;
import shelter.domain.DailySchedule;
import shelter.domain.Dog;
import shelter.domain.LivingSpace;
import shelter.domain.RequestStatus;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvAdoptionRequestRepository}, verifying all CRUD and query operations
 * including shelter-based and status-based filtering.
 * Uses a JUnit {@code @TempDir} and real backing CSV repositories to avoid mocking complexity.
 */
class CsvAdoptionRequestRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvAnimalRepository animalRepo;
    private CsvAdopterRepository adopterRepo;
    private CsvAdoptionRequestRepository repo;

    /** A reusable adopter instance saved to the backing adopter repository. */
    private Adopter adopter;
    /** A reusable dog instance saved to the backing animal repository. */
    private Dog dog;

    /**
     * Sets up fresh backing repositories and seeds one adopter and one dog before each test.
     * The seeded entities provide valid cross-repository references for request construction.
     */
    @BeforeEach
    void setUp() {
        animalRepo  = new CsvAnimalRepository(tempDir.toString());
        adopterRepo = new CsvAdopterRepository(tempDir.toString());
        repo        = new CsvAdoptionRequestRepository(tempDir.toString(), animalRepo, adopterRepo);

        AdopterPreferences prefs = new AdopterPreferences(null, null, null, null, 0, 20);
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null, prefs);
        adopterRepo.save(adopter);

        dog = new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, Dog.Size.LARGE, false);
        dog.setShelterId("shelter-1");
        animalRepo.save(dog);
    }

    /**
     * Verifies that a saved AdoptionRequest can be retrieved by ID with all fields preserved.
     */
    @Test
    void saveAndFindById_returnsSameRequest() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        repo.save(req);

        Optional<AdoptionRequest> found = repo.findById(req.getId());
        assertTrue(found.isPresent());
        AdoptionRequest loaded = found.get();
        assertEquals(req.getId(), loaded.getId());
        assertEquals(adopter.getId(), loaded.getAdopter().getId());
        assertEquals(dog.getId(), loaded.getAnimal().getId());
        assertEquals(RequestStatus.PENDING, loaded.getStatus());
        assertNotNull(loaded.getSubmittedAt());
    }

    /**
     * Verifies that findAll returns all saved requests.
     */
    @Test
    void findAll_returnsAllSaved() {
        repo.save(new AdoptionRequest(adopter, dog));
        Dog dog2 = new Dog("Buddy", "Poodle", LocalDate.now().minusYears(2), ActivityLevel.LOW, false, Dog.Size.SMALL, true);
        dog2.setShelterId("shelter-1");
        animalRepo.save(dog2);
        repo.save(new AdoptionRequest(adopter, dog2));

        assertEquals(2, repo.findAll().size());
    }

    /**
     * Verifies that delete removes the request from the store.
     */
    @Test
    void delete_removesRequest() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        repo.save(req);
        repo.delete(req.getId());

        assertTrue(repo.findById(req.getId()).isEmpty());
    }

    /**
     * Verifies that findById returns empty for an unknown ID.
     */
    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(repo.findById("no-such-id").isEmpty());
    }

    /**
     * Verifies that findByAdopterId returns requests only for the given adopter.
     */
    @Test
    void findByAdopterId_filtersCorrectly() {
        repo.save(new AdoptionRequest(adopter, dog));

        List<AdoptionRequest> result = repo.findByAdopterId(adopter.getId());
        assertEquals(1, result.size());
        assertEquals(adopter.getId(), result.get(0).getAdopter().getId());
    }

    /**
     * Verifies that findByShelterId returns requests for animals in the given shelter.
     */
    @Test
    void findByShelterId_filtersCorrectly() {
        repo.save(new AdoptionRequest(adopter, dog)); // dog is in shelter-1

        // Dog in a different shelter
        Dog dog2 = new Dog("Other", "Mutt", LocalDate.now().minusYears(1), ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        dog2.setShelterId("shelter-2");
        animalRepo.save(dog2);
        repo.save(new AdoptionRequest(adopter, dog2));

        List<AdoptionRequest> s1Requests = repo.findByShelterId("shelter-1");
        assertEquals(1, s1Requests.size());
        assertEquals(dog.getId(), s1Requests.get(0).getAnimal().getId());
    }

    /**
     * Verifies that findByStatus returns only requests matching the given status.
     */
    @Test
    void findByStatus_filtersCorrectly() {
        AdoptionRequest pending = new AdoptionRequest(adopter, dog);
        repo.save(pending);

        Dog dog2 = new Dog("Buddy2", "Beagle", LocalDate.now().minusYears(2), ActivityLevel.MEDIUM, true, Dog.Size.MEDIUM, false);
        dog2.setShelterId("shelter-1");
        animalRepo.save(dog2);
        AdoptionRequest approved = new AdoptionRequest(adopter, dog2);
        approved.approve();
        repo.save(approved);

        List<AdoptionRequest> pendingList = repo.findByStatus(RequestStatus.PENDING);
        assertEquals(1, pendingList.size());
        assertEquals(pending.getId(), pendingList.get(0).getId());

        List<AdoptionRequest> approvedList = repo.findByStatus(RequestStatus.APPROVED);
        assertEquals(1, approvedList.size());
        assertEquals(approved.getId(), approvedList.get(0).getId());
    }

    /**
     * Verifies findByAdopterIdAndStatus combines both filters correctly.
     */
    @Test
    void findByAdopterIdAndStatus_combinedFilter() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        repo.save(req);

        List<AdoptionRequest> result =
                repo.findByAdopterIdAndStatus(adopter.getId(), RequestStatus.PENDING);
        assertEquals(1, result.size());

        List<AdoptionRequest> noMatch =
                repo.findByAdopterIdAndStatus(adopter.getId(), RequestStatus.APPROVED);
        assertEquals(0, noMatch.size());
    }

    /**
     * Verifies findByShelterIdAndStatus combines shelter and status filters.
     */
    @Test
    void findByShelterIdAndStatus_combinedFilter() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        repo.save(req);

        List<AdoptionRequest> pending =
                repo.findByShelterIdAndStatus("shelter-1", RequestStatus.PENDING);
        assertEquals(1, pending.size());

        List<AdoptionRequest> approved =
                repo.findByShelterIdAndStatus("shelter-1", RequestStatus.APPROVED);
        assertEquals(0, approved.size());
    }

    /**
     * Verifies that findByAnimalId returns only requests for the specified animal,
     * and returns an empty list for an animal with no requests.
     */
    @Test
    void findByAnimalId_returnsMatchingRequests() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        repo.save(req);

        List<AdoptionRequest> found = repo.findByAnimalId(dog.getId());
        assertEquals(1, found.size());
        assertEquals(dog.getId(), found.get(0).getAnimal().getId());

        List<AdoptionRequest> none = repo.findByAnimalId("no-such-animal");
        assertTrue(none.isEmpty());
    }

    /**
     * Verifies CSV round-trip: requests saved in one instance are readable in a new instance
     * with the original status and timestamp preserved.
     */
    @Test
    void persistence_roundTrip() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        repo.save(req);

        CsvAdoptionRequestRepository repo2 =
                new CsvAdoptionRequestRepository(tempDir.toString(), animalRepo, adopterRepo);
        Optional<AdoptionRequest> found = repo2.findById(req.getId());
        assertTrue(found.isPresent());
        assertEquals(RequestStatus.PENDING, found.get().getStatus());
        assertEquals(req.getSubmittedAt(), found.get().getSubmittedAt());
    }
}
