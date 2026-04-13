package shelter.repository.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shelter.domain.ActivityLevel;
import shelter.domain.Dog;
import shelter.domain.RequestStatus;
import shelter.domain.Shelter;
import shelter.domain.TransferRequest;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvTransferRequestRepository}, verifying all CRUD and query operations
 * including shelter-based and animal-based filtering.
 * Uses real backing CSV repositories to avoid mocking complexity.
 */
class CsvTransferRequestRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvAnimalRepository animalRepo;
    private CsvShelterRepository shelterRepo;
    private CsvTransferRequestRepository repo;

    /** Reusable shelters and animal seeded before each test. */
    private Shelter shelterA;
    private Shelter shelterB;
    private Dog dog;

    /**
     * Creates fresh backing repositories and seeds two shelters and one dog before each test.
     * The seeded entities provide valid cross-repository references for request construction.
     */
    @BeforeEach
    void setUp() {
        animalRepo  = new CsvAnimalRepository(tempDir.toString());
        shelterRepo = new CsvShelterRepository(tempDir.toString());
        repo        = new CsvTransferRequestRepository(tempDir.toString(), animalRepo, shelterRepo);

        shelterA = new Shelter("Shelter Alpha", "City A", 20);
        shelterB = new Shelter("Shelter Beta",  "City B", 20);
        shelterRepo.save(shelterA);
        shelterRepo.save(shelterB);

        dog = new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, Dog.Size.LARGE, false);
        dog.setShelterId(shelterA.getId());
        animalRepo.save(dog);
    }

    /**
     * Verifies that a saved TransferRequest can be retrieved by ID with all fields preserved.
     */
    @Test
    void saveAndFindById_returnsSameRequest() {
        TransferRequest req = new TransferRequest(dog, shelterA, shelterB);
        repo.save(req);

        Optional<TransferRequest> found = repo.findById(req.getId());
        assertTrue(found.isPresent());
        TransferRequest loaded = found.get();
        assertEquals(req.getId(), loaded.getId());
        assertEquals(dog.getId(), loaded.getAnimal().getId());
        assertEquals(shelterA.getId(), loaded.getFrom().getId());
        assertEquals(shelterB.getId(), loaded.getTo().getId());
        assertEquals(RequestStatus.PENDING, loaded.getStatus());
        assertNotNull(loaded.getRequestedAt());
    }

    /**
     * Verifies that findAll returns all saved requests.
     */
    @Test
    void findAll_returnsAllSaved() {
        repo.save(new TransferRequest(dog, shelterA, shelterB));
        Dog dog2 = new Dog("Buddy", "Poodle", LocalDate.now().minusYears(2), ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        dog2.setShelterId(shelterB.getId());
        animalRepo.save(dog2);
        repo.save(new TransferRequest(dog2, shelterB, shelterA));

        assertEquals(2, repo.findAll().size());
    }

    /**
     * Verifies that delete removes the request from the store.
     */
    @Test
    void delete_removesRequest() {
        TransferRequest req = new TransferRequest(dog, shelterA, shelterB);
        repo.save(req);
        repo.delete(req.getId());

        assertTrue(repo.findById(req.getId()).isEmpty());
    }

    /**
     * Verifies that findById returns empty for an unknown ID.
     */
    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(repo.findById("unknown").isEmpty());
    }

    /**
     * Verifies that findByAnimalId returns requests only for the given animal.
     */
    @Test
    void findByAnimalId_filtersCorrectly() {
        repo.save(new TransferRequest(dog, shelterA, shelterB));

        List<TransferRequest> result = repo.findByAnimalId(dog.getId());
        assertEquals(1, result.size());
        assertEquals(dog.getId(), result.get(0).getAnimal().getId());
    }

    /**
     * Verifies that findByFromShelterId returns requests whose source matches the given shelter.
     */
    @Test
    void findByFromShelterId_filtersCorrectly() {
        repo.save(new TransferRequest(dog, shelterA, shelterB));

        assertEquals(1, repo.findByFromShelterId(shelterA.getId()).size());
        assertEquals(0, repo.findByFromShelterId(shelterB.getId()).size());
    }

    /**
     * Verifies that findByToShelterId returns requests whose destination matches the given shelter.
     */
    @Test
    void findByToShelterId_filtersCorrectly() {
        repo.save(new TransferRequest(dog, shelterA, shelterB));

        assertEquals(1, repo.findByToShelterId(shelterB.getId()).size());
        assertEquals(0, repo.findByToShelterId(shelterA.getId()).size());
    }

    /**
     * Verifies that findByStatus returns only requests with the matching status.
     */
    @Test
    void findByStatus_filtersCorrectly() {
        TransferRequest pending = new TransferRequest(dog, shelterA, shelterB);
        repo.save(pending);

        Dog dog2 = new Dog("Dog2", "Mutt", LocalDate.now().minusYears(1), ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        dog2.setShelterId(shelterB.getId());
        animalRepo.save(dog2);
        TransferRequest approved = new TransferRequest(dog2, shelterB, shelterA);
        approved.approve();
        repo.save(approved);

        assertEquals(1, repo.findByStatus(RequestStatus.PENDING).size());
        assertEquals(1, repo.findByStatus(RequestStatus.APPROVED).size());
    }

    /**
     * Verifies findByShelterIdAndStatus matches the shelter ID against BOTH from and to fields.
     */
    @Test
    void findByShelterIdAndStatus_matchesBothFromAndTo() {
        // shelterA is the source
        TransferRequest fromA = new TransferRequest(dog, shelterA, shelterB);
        repo.save(fromA);

        // shelterA is the destination
        Dog dog2 = new Dog("Dog2", "Breed", LocalDate.now().minusYears(2), ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        dog2.setShelterId(shelterB.getId());
        animalRepo.save(dog2);
        TransferRequest toA = new TransferRequest(dog2, shelterB, shelterA);
        repo.save(toA);

        List<TransferRequest> result =
                repo.findByShelterIdAndStatus(shelterA.getId(), RequestStatus.PENDING);
        assertEquals(2, result.size(), "Both from-A and to-A requests should be found");
    }

    /**
     * Verifies findByAnimalIdAndStatus combines both filters correctly.
     */
    @Test
    void findByAnimalIdAndStatus_combinedFilter() {
        TransferRequest req = new TransferRequest(dog, shelterA, shelterB);
        repo.save(req);

        List<TransferRequest> pending =
                repo.findByAnimalIdAndStatus(dog.getId(), RequestStatus.PENDING);
        assertEquals(1, pending.size());

        List<TransferRequest> approved =
                repo.findByAnimalIdAndStatus(dog.getId(), RequestStatus.APPROVED);
        assertEquals(0, approved.size());
    }

    /**
     * Verifies CSV round-trip: requests saved in one instance are readable in a new instance
     * with status and timestamp preserved.
     */
    @Test
    void persistence_roundTrip() {
        TransferRequest req = new TransferRequest(dog, shelterA, shelterB);
        repo.save(req);

        CsvTransferRequestRepository repo2 =
                new CsvTransferRequestRepository(tempDir.toString(), animalRepo, shelterRepo);
        Optional<TransferRequest> found = repo2.findById(req.getId());
        assertTrue(found.isPresent());
        assertEquals(RequestStatus.PENDING, found.get().getStatus());
        assertEquals(req.getRequestedAt(), found.get().getRequestedAt());
    }

    /**
     * Verifies that constructing the repository with a null ShelterRepository throws
     * {@link IllegalArgumentException}.
     */
    @Test
    void constructor_nullShelterRepository_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new CsvTransferRequestRepository(tempDir.toString(), animalRepo, null));
    }
}
