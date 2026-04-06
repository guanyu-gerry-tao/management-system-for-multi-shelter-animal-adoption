package shelter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.ActivityLevel;
import shelter.domain.Animal;
import shelter.domain.Dog;
import shelter.domain.RequestStatus;
import shelter.domain.Shelter;
import shelter.domain.TransferRequest;
import shelter.repository.AnimalRepository;
import shelter.repository.ShelterRepository;
import shelter.repository.TransferRequestRepository;
import shelter.service.impl.TransferServiceImpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransferServiceImpl}, covering the full transfer request lifecycle
 * and the pending-request query. Repositories are replaced with in-memory stubs to keep
 * tests fast and isolated from CSV persistence.
 */
class TransferServiceImplTest {

    private StubTransferRequestRepository requestRepo;
    private StubAnimalRepository animalRepo;
    private StubShelterRepository shelterRepo;
    private TransferServiceImpl service;

    private Shelter shelterA;
    private Shelter shelterB;
    private Dog dog;

    /**
     * Sets up two shelters, one dog in the first shelter, and fresh stubs before each test.
     */
    @BeforeEach
    void setUp() {
        requestRepo = new StubTransferRequestRepository();
        animalRepo = new StubAnimalRepository();
        shelterRepo = new StubShelterRepository();
        service = new TransferServiceImpl(requestRepo, animalRepo, shelterRepo);

        shelterA = new Shelter("Shelter A", "Boston", 10);
        shelterB = new Shelter("Shelter B", "Cambridge", 10);

        dog = new Dog("Rex", "Labrador", 3, ActivityLevel.HIGH, false, Dog.Size.LARGE, false);
        dog.setShelterId(shelterA.getId());
        shelterA.addAnimal(dog);

        animalRepo.save(dog);
        shelterRepo.save(shelterA);
        shelterRepo.save(shelterB);
    }

    // ── requestTransfer ───────────────────────────────────────────────────────

    /**
     * Verifies that a valid transfer request is created in PENDING status.
     */
    @Test
    void requestTransfer_valid_createsPendingRequest() {
        TransferRequest req = service.requestTransfer(dog, shelterA, shelterB);

        assertNotNull(req);
        assertEquals(RequestStatus.PENDING, req.getStatus());
        assertEquals(dog.getId(), req.getAnimal().getId());
        assertEquals(1, requestRepo.findAll().size());
    }

    /**
     * Verifies that requesting a transfer for an animal not in the source shelter throws.
     */
    @Test
    void requestTransfer_animalNotInSource_throws() {
        Shelter shelterC = new Shelter("Shelter C", "Somerville", 10);
        assertThrows(IllegalArgumentException.class,
                () -> service.requestTransfer(dog, shelterC, shelterB));
    }

    /**
     * Verifies that requesting a transfer to a full shelter throws.
     */
    @Test
    void requestTransfer_destinationFull_throws() {
        Shelter tiny = new Shelter("Tiny", "Waltham", 1);
        Dog blocker = new Dog("Blocker", "Poodle", 2, ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        tiny.addAnimal(blocker);
        assertThrows(IllegalStateException.class,
                () -> service.requestTransfer(dog, shelterA, tiny));
    }

    /**
     * Verifies that null arguments throw IllegalArgumentException.
     */
    @Test
    void requestTransfer_nullArgs_throw() {
        assertThrows(IllegalArgumentException.class, () -> service.requestTransfer(null, shelterA, shelterB));
        assertThrows(IllegalArgumentException.class, () -> service.requestTransfer(dog, null, shelterB));
        assertThrows(IllegalArgumentException.class, () -> service.requestTransfer(dog, shelterA, null));
    }

    // ── approve ───────────────────────────────────────────────────────────────

    /**
     * Verifies that approving a transfer moves the animal to the destination shelter
     * and updates its shelterId.
     */
    @Test
    void approve_pendingRequest_movesAnimal() {
        TransferRequest req = service.requestTransfer(dog, shelterA, shelterB);
        service.approve(req);

        assertEquals(RequestStatus.APPROVED, req.getStatus());
        assertEquals(shelterB.getId(), dog.getShelterId());
        assertFalse(shelterA.containsAnimal(dog.getId()));
        assertTrue(shelterB.containsAnimal(dog.getId()));
    }

    /**
     * Verifies that approving an already-approved transfer throws.
     */
    @Test
    void approve_alreadyApproved_throws() {
        TransferRequest req = service.requestTransfer(dog, shelterA, shelterB);
        service.approve(req);
        assertThrows(IllegalStateException.class, () -> service.approve(req));
    }

    /**
     * Verifies that approve rejects a null request.
     */
    @Test
    void approve_nullRequest_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.approve(null));
    }

    // ── reject ────────────────────────────────────────────────────────────────

    /**
     * Verifies that rejecting a transfer transitions it to REJECTED and does not move the animal.
     */
    @Test
    void reject_pendingRequest_transitionsToRejected() {
        TransferRequest req = service.requestTransfer(dog, shelterA, shelterB);
        service.reject(req);

        assertEquals(RequestStatus.REJECTED, req.getStatus());
        assertTrue(shelterA.containsAnimal(dog.getId()));
        assertEquals(shelterA.getId(), dog.getShelterId());
    }

    /**
     * Verifies that rejecting an already-rejected request throws.
     */
    @Test
    void reject_alreadyRejected_throws() {
        TransferRequest req = service.requestTransfer(dog, shelterA, shelterB);
        service.reject(req);
        assertThrows(IllegalStateException.class, () -> service.reject(req));
    }

    // ── dismiss ───────────────────────────────────────────────────────────────

    /**
     * Verifies that dismissing a transfer transitions it to CANCELLED and does not move the animal.
     */
    @Test
    void dismiss_pendingRequest_transitionsToCancelled() {
        TransferRequest req = service.requestTransfer(dog, shelterA, shelterB);
        service.dismiss(req);

        assertEquals(RequestStatus.CANCELLED, req.getStatus());
        assertTrue(shelterA.containsAnimal(dog.getId()));
    }

    /**
     * Verifies that dismiss rejects a null request.
     */
    @Test
    void dismiss_nullRequest_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.dismiss(null));
    }

    // ── getPendingRequests ────────────────────────────────────────────────────

    /**
     * Verifies that getPendingRequests returns only PENDING requests for the given shelter.
     */
    @Test
    void getPendingRequests_returnsPendingOnly() {
        TransferRequest req = service.requestTransfer(dog, shelterA, shelterB);

        List<TransferRequest> pending = service.getPendingRequests(shelterA);
        assertEquals(1, pending.size());

        service.approve(req);
        List<TransferRequest> afterApproval = service.getPendingRequests(shelterA);
        assertTrue(afterApproval.isEmpty());
    }

    /**
     * Verifies that getPendingRequests works for the destination shelter as well.
     */
    @Test
    void getPendingRequests_includesDestinationShelter() {
        service.requestTransfer(dog, shelterA, shelterB);
        List<TransferRequest> pending = service.getPendingRequests(shelterB);
        assertEquals(1, pending.size());
    }

    /**
     * Verifies that getPendingRequests rejects a null shelter.
     */
    @Test
    void getPendingRequests_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getPendingRequests(null));
    }

    // ── in-memory stubs ───────────────────────────────────────────────────────

    private static class StubTransferRequestRepository implements TransferRequestRepository {
        private final Map<String, TransferRequest> store = new LinkedHashMap<>();

        @Override public void save(TransferRequest r) { store.put(r.getId(), r); }
        @Override public Optional<TransferRequest> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<TransferRequest> findAll() { return new ArrayList<>(store.values()); }
        @Override public void delete(String id) { store.remove(id); }
        @Override public List<TransferRequest> findByAnimalId(String id) {
            return store.values().stream().filter(r -> r.getAnimal().getId().equals(id)).collect(Collectors.toList());
        }
        @Override public List<TransferRequest> findByFromShelterId(String id) {
            return store.values().stream().filter(r -> r.getFrom().getId().equals(id)).collect(Collectors.toList());
        }
        @Override public List<TransferRequest> findByToShelterId(String id) {
            return store.values().stream().filter(r -> r.getTo().getId().equals(id)).collect(Collectors.toList());
        }
        @Override public List<TransferRequest> findByStatus(RequestStatus s) {
            return store.values().stream().filter(r -> r.getStatus() == s).collect(Collectors.toList());
        }
        @Override public List<TransferRequest> findByShelterIdAndStatus(String id, RequestStatus s) {
            return store.values().stream()
                    .filter(r -> (r.getFrom().getId().equals(id) || r.getTo().getId().equals(id)) && r.getStatus() == s)
                    .collect(Collectors.toList());
        }
        @Override public List<TransferRequest> findByAnimalIdAndStatus(String id, RequestStatus s) {
            return store.values().stream().filter(r -> r.getAnimal().getId().equals(id) && r.getStatus() == s).collect(Collectors.toList());
        }
    }

    private static class StubAnimalRepository implements AnimalRepository {
        private final Map<String, Animal> store = new LinkedHashMap<>();

        @Override public void save(Animal a) { store.put(a.getId(), a); }
        @Override public Optional<Animal> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Animal> findAll() { return new ArrayList<>(store.values()); }
        @Override public void delete(String id) { store.remove(id); }
        @Override public List<Animal> findByShelterId(String id) {
            return store.values().stream().filter(a -> id.equals(a.getShelterId())).collect(Collectors.toList());
        }
        @Override public List<Animal> findByAdopterId(String id) {
            return store.values().stream().filter(a -> id.equals(a.getAdopterId())).collect(Collectors.toList());
        }
    }

    private static class StubShelterRepository implements ShelterRepository {
        private final Map<String, Shelter> store = new LinkedHashMap<>();

        @Override public void save(Shelter s) { store.put(s.getId(), s); }
        @Override public Optional<Shelter> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Shelter> findAll() { return new ArrayList<>(store.values()); }
        @Override public void delete(String id) { store.remove(id); }
    }
}
