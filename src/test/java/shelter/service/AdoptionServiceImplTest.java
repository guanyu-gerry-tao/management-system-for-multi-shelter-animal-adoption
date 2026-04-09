package shelter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.ActivityLevel;
import shelter.domain.Adopter;
import shelter.domain.AdopterPreferences;
import shelter.domain.AdoptionRequest;
import shelter.domain.Animal;
import shelter.domain.DailySchedule;
import shelter.domain.Dog;
import shelter.domain.LivingSpace;
import shelter.domain.RequestStatus;
import shelter.domain.Shelter;
import shelter.domain.Species;
import shelter.repository.AdopterRepository;
import shelter.repository.AdoptionRequestRepository;
import shelter.repository.AnimalRepository;
import shelter.service.AuditService;
import shelter.service.impl.AdoptionServiceImpl;
import shelter.service.model.AuditEntry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdoptionServiceImpl}, covering the full adoption request lifecycle
 * and all query methods. Repositories are replaced with simple in-memory stubs to isolate
 * the service logic from persistence concerns.
 */
class AdoptionServiceImplTest {

    private StubAdoptionRequestRepository requestRepo;
    private StubAnimalRepository animalRepo;
    private StubAdopterRepository adopterRepo;
    private AdoptionServiceImpl service;

    private Adopter adopter;
    private Dog dog;

    /**
     * Sets up fresh in-memory stubs and constructs the service before each test.
     * Seeds one adopter and one available dog for use across test cases.
     */
    @BeforeEach
    void setUp() {
        requestRepo = new StubAdoptionRequestRepository();
        animalRepo = new StubAnimalRepository();
        adopterRepo = new StubAdopterRepository();
        service = new AdoptionServiceImpl(requestRepo, animalRepo, adopterRepo, new NoOpAuditService<>());

        AdopterPreferences prefs = new AdopterPreferences(Species.DOG, null, null, 0, 10);
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null, prefs);
        dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3), ActivityLevel.HIGH, false, Dog.Size.LARGE, false);
        dog.setShelterId("shelter-1");
        animalRepo.save(dog);
        adopterRepo.save(adopter);
    }

    // ── submit ────────────────────────────────────────────────────────────────

    /**
     * Verifies that a valid request for an available animal is persisted as PENDING.
     */
    @Test
    void submit_availableAnimal_savesRequest() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        service.submit(req);

        assertEquals(1, requestRepo.findAll().size());
        assertEquals(RequestStatus.PENDING, requestRepo.findAll().get(0).getStatus());
    }

    /**
     * Verifies that submitting a request for an already-adopted animal throws.
     */
    @Test
    void submit_alreadyAdopted_throws() {
        dog.setAdopterId(adopter.getId());
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        assertThrows(IllegalStateException.class, () -> service.submit(req));
    }

    /**
     * Verifies that submit rejects a null request.
     */
    @Test
    void submit_nullRequest_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.submit(null));
    }

    // ── approve ───────────────────────────────────────────────────────────────

    /**
     * Verifies that approving a PENDING request transitions it to APPROVED,
     * sets the animal's adopterId, and adds the animal to the adopter's list.
     */
    @Test
    void approve_pendingRequest_updatesAllRecords() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);

        service.approve(req);

        assertEquals(RequestStatus.APPROVED, req.getStatus());
        assertEquals(adopter.getId(), dog.getAdopterId());
        assertTrue(adopter.getAdoptedAnimalIds().contains(dog.getId()));
        assertEquals(RequestStatus.APPROVED, requestRepo.findById(req.getId()).get().getStatus());
    }

    /**
     * Verifies that approving an already-approved request throws.
     */
    @Test
    void approve_alreadyApproved_throws() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);
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
     * Verifies that rejecting a PENDING request transitions it to REJECTED
     * and leaves the animal available.
     */
    @Test
    void reject_pendingRequest_transitionsToRejected() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);
        service.reject(req);

        assertEquals(RequestStatus.REJECTED, req.getStatus());
        assertTrue(dog.isAvailable());
    }

    /**
     * Verifies that rejecting an already-rejected request throws.
     */
    @Test
    void reject_alreadyRejected_throws() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);
        service.reject(req);
        assertThrows(IllegalStateException.class, () -> service.reject(req));
    }

    // ── cancel ────────────────────────────────────────────────────────────────

    /**
     * Verifies that cancelling a PENDING request transitions it to CANCELLED.
     */
    @Test
    void cancel_pendingRequest_transitionsToCancelled() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);
        service.cancel(req);

        assertEquals(RequestStatus.CANCELLED, req.getStatus());
    }

    /**
     * Verifies that cancelling an approved request throws.
     */
    @Test
    void cancel_approvedRequest_throws() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);
        service.approve(req);
        assertThrows(IllegalStateException.class, () -> service.cancel(req));
    }

    // ── query methods ─────────────────────────────────────────────────────────

    /**
     * Verifies that getRequestsByAdopter returns only requests for that adopter.
     */
    @Test
    void getRequestsByAdopter_returnsCorrectRequests() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);

        List<AdoptionRequest> result = service.getRequestsByAdopter(adopter);
        assertEquals(1, result.size());
        assertEquals(adopter.getId(), result.get(0).getAdopter().getId());
    }

    /**
     * Verifies that getRequestsByAnimal returns only requests for that animal.
     */
    @Test
    void getRequestsByAnimal_returnsCorrectRequests() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);

        List<AdoptionRequest> result = service.getRequestsByAnimal(dog);
        assertEquals(1, result.size());
        assertEquals(dog.getId(), result.get(0).getAnimal().getId());
    }

    /**
     * Verifies that getRequestsByShelter returns only requests for animals in that shelter.
     */
    @Test
    void getRequestsByShelter_returnsCorrectRequests() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);

        Shelter shelter = new Shelter("shelter-1", "Shelter One", "Boston", 10);
        List<AdoptionRequest> result = service.getRequestsByShelter(shelter);
        assertEquals(1, result.size());
    }

    /**
     * Verifies that getRequestsAfter filters by submission date correctly.
     */
    @Test
    void getRequestsAfter_filtersCorrectly() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);

        List<AdoptionRequest> result = service.getRequestsAfter(LocalDate.now().minusDays(1));
        assertEquals(1, result.size());

        List<AdoptionRequest> empty = service.getRequestsAfter(LocalDate.now().plusDays(1));
        assertTrue(empty.isEmpty());
    }

    /**
     * Verifies that getApprovedAfter returns only approved requests after the given date.
     */
    @Test
    void getApprovedAfter_returnsOnlyApproved() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);
        service.approve(req);

        List<AdoptionRequest> result = service.getApprovedAfter(LocalDate.now().minusDays(1));
        assertEquals(1, result.size());
        assertEquals(RequestStatus.APPROVED, result.get(0).getStatus());
    }

    // ── null guard on query methods ───────────────────────────────────────────

    @Test void getRequestsByAdopter_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getRequestsByAdopter(null));
    }

    @Test void getRequestsByAnimal_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getRequestsByAnimal(null));
    }

    @Test void getRequestsByShelter_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getRequestsByShelter(null));
    }

    @Test void getRequestsAfter_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getRequestsAfter(null));
    }

    @Test void getApprovedAfter_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getApprovedAfter(null));
    }

    // ── audit logging ─────────────────────────────────────────────────────────

    /**
     * Verifies that submit logs an audit entry with the expected action string.
     */
    @Test
    void submit_logsAuditEntry() {
        SpyAuditService<AdoptionRequest> spy = new SpyAuditService<>();
        service = new AdoptionServiceImpl(requestRepo, animalRepo, adopterRepo, spy);

        service.submit(new AdoptionRequest(adopter, dog));

        assertTrue(spy.loggedActions.contains("submitted adoption request"));
    }

    /**
     * Verifies that approve logs an audit entry with the expected action string.
     */
    @Test
    void approve_logsAuditEntry() {
        SpyAuditService<AdoptionRequest> spy = new SpyAuditService<>();
        service = new AdoptionServiceImpl(requestRepo, animalRepo, adopterRepo, spy);
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);

        service.approve(req);

        assertTrue(spy.loggedActions.contains("approved adoption request"));
    }

    /**
     * Verifies that reject logs an audit entry with the expected action string.
     */
    @Test
    void reject_logsAuditEntry() {
        SpyAuditService<AdoptionRequest> spy = new SpyAuditService<>();
        service = new AdoptionServiceImpl(requestRepo, animalRepo, adopterRepo, spy);
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);

        service.reject(req);

        assertTrue(spy.loggedActions.contains("rejected adoption request"));
    }

    /**
     * Verifies that cancel logs an audit entry with the expected action string.
     */
    @Test
    void cancel_logsAuditEntry() {
        SpyAuditService<AdoptionRequest> spy = new SpyAuditService<>();
        service = new AdoptionServiceImpl(requestRepo, animalRepo, adopterRepo, spy);
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        requestRepo.save(req);

        service.cancel(req);

        assertTrue(spy.loggedActions.contains("cancelled adoption request"));
    }

    // ── in-memory stubs ───────────────────────────────────────────────────────

    private static class StubAdoptionRequestRepository implements AdoptionRequestRepository {
        private final Map<String, AdoptionRequest> store = new LinkedHashMap<>();

        @Override public void save(AdoptionRequest r) { store.put(r.getId(), r); }
        @Override public Optional<AdoptionRequest> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<AdoptionRequest> findAll() { return new ArrayList<>(store.values()); }
        @Override public void delete(String id) { store.remove(id); }

        @Override public List<AdoptionRequest> findByAdopterId(String id) {
            return store.values().stream().filter(r -> r.getAdopter().getId().equals(id)).collect(Collectors.toList());
        }
        @Override public List<AdoptionRequest> findByAnimalId(String id) {
            return store.values().stream().filter(r -> r.getAnimal().getId().equals(id)).collect(Collectors.toList());
        }
        @Override public List<AdoptionRequest> findByShelterId(String id) {
            return store.values().stream().filter(r -> id.equals(r.getAnimal().getShelterId())).collect(Collectors.toList());
        }
        @Override public List<AdoptionRequest> findByStatus(RequestStatus s) {
            return store.values().stream().filter(r -> r.getStatus() == s).collect(Collectors.toList());
        }
        @Override public List<AdoptionRequest> findByAdopterIdAndStatus(String id, RequestStatus s) {
            return store.values().stream().filter(r -> r.getAdopter().getId().equals(id) && r.getStatus() == s).collect(Collectors.toList());
        }
        @Override public List<AdoptionRequest> findByShelterIdAndStatus(String id, RequestStatus s) {
            return store.values().stream().filter(r -> id.equals(r.getAnimal().getShelterId()) && r.getStatus() == s).collect(Collectors.toList());
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

    private static class StubAdopterRepository implements AdopterRepository {
        private final Map<String, Adopter> store = new LinkedHashMap<>();

        @Override public void save(Adopter a) { store.put(a.getId(), a); }
        @Override public Optional<Adopter> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Adopter> findAll() { return new ArrayList<>(store.values()); }
        @Override public void delete(String id) { store.remove(id); }
    }

    /** No-op audit stub that silently discards all log calls during tests. */
    private static class NoOpAuditService<T> implements AuditService<T> {
        @Override public void log(String action, T target) { }
        @Override public List<AuditEntry<T>> getLog() { return new ArrayList<>(); }
    }

    /** Spy audit stub that records action strings so tests can assert on them. */
    private static class SpyAuditService<T> implements AuditService<T> {
        final List<String> loggedActions = new ArrayList<>();
        @Override public void log(String action, T target) { loggedActions.add(action); }
        @Override public List<AuditEntry<T>> getLog() { return new ArrayList<>(); }
    }
}
