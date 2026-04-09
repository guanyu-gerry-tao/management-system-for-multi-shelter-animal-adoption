package shelter.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.application.impl.AdoptionApplicationServiceImpl;
import shelter.domain.*;
import shelter.exception.AnimalNotAvailableException;
import shelter.exception.EntityNotFoundException;
import shelter.service.*;
import shelter.service.model.AuditEntry;
import shelter.service.model.NotificationRecord;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdoptionApplicationServiceImpl}.
 * Verifies submission, approval, rejection, cancellation, and guard conditions.
 */
class AdoptionApplicationServiceImplTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private StubAdoptionService adoptionService;
    private StubAnimalService animalService;
    private StubAdopterService adopterService;
    private StubNotificationService notificationService;
    private SpyAuditService<AdoptionRequest> auditService;
    private AdoptionApplicationServiceImpl service;
    private Adopter adopter;
    private Dog dog;

    @BeforeEach
    void setUp() {
        adoptionService     = new StubAdoptionService();
        animalService       = new StubAnimalService();
        adopterService      = new StubAdopterService();
        notificationService = new StubNotificationService();
        auditService        = new SpyAuditService<>();
        service = new AdoptionApplicationServiceImpl(
                adoptionService, animalService, adopterService, notificationService, auditService);

        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(null, null, null, 0, 20));
        dog = new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);

        adopterService.store.put(adopter.getId(), adopter);
        animalService.store.put(dog.getId(), dog);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void submitRequest_createsRequestAndLogsAudit() {
        AdoptionRequest request = service.submitRequest(adopter.getId(), dog.getId());

        assertNotNull(request);
        assertEquals(1, adoptionService.submitCount);
        assertTrue(auditService.actions.contains("submitted adoption request"));
    }

    @Test
    void submitRequest_animalAlreadyAdopted_throws() {
        dog.setAdopterId("someone");

        assertThrows(AnimalNotAvailableException.class,
                () -> service.submitRequest(adopter.getId(), dog.getId()));
    }

    @Test
    void submitRequest_adopterNotFound_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> service.submitRequest("missing", dog.getId()));
    }

    @Test
    void approveRequest_approvesAndUpdatesAnimalAndAdopter() {
        AdoptionRequest request = new AdoptionRequest(adopter, dog);
        adoptionService.store.put(request.getId(), request);

        service.approveRequest(request.getId());

        assertEquals(1, adoptionService.approveCount);
        assertEquals(adopter.getId(), animalService.store.get(dog.getId()).getAdopterId());
        assertTrue(adopterService.store.get(adopter.getId()).getAdoptedAnimalIds().contains(dog.getId()));
        assertEquals(1, notificationService.adoptionNotifyCount);
        assertTrue(auditService.actions.contains("approved adoption request"));
    }

    @Test
    void rejectRequest_rejectsAndNotifies() {
        AdoptionRequest request = new AdoptionRequest(adopter, dog);
        adoptionService.store.put(request.getId(), request);

        service.rejectRequest(request.getId());

        assertEquals(1, adoptionService.rejectCount);
        assertEquals(1, notificationService.adoptionNotifyCount);
        assertTrue(auditService.actions.contains("rejected adoption request"));
    }

    @Test
    void cancelRequest_cancelsAndNotifies() {
        AdoptionRequest request = new AdoptionRequest(adopter, dog);
        adoptionService.store.put(request.getId(), request);

        service.cancelRequest(request.getId());

        assertEquals(1, adoptionService.cancelCount);
        assertEquals(1, notificationService.adoptionNotifyCount);
        assertTrue(auditService.actions.contains("cancelled adoption request"));
    }

    @Test
    void approveRequest_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.approveRequest("missing"));
    }

    // -------------------------------------------------------------------------
    // Stubs
    // -------------------------------------------------------------------------

    /**
     * Stub implementation of {@link AdoptionService} backed by an in-memory map.
     * Counters track how many times each state-transition method was invoked.
     */
    private static class StubAdoptionService implements AdoptionService {

        final Map<String, AdoptionRequest> store = new HashMap<>();
        int submitCount;
        int approveCount;
        int rejectCount;
        int cancelCount;

        @Override
        public void submit(AdoptionRequest r) {
            store.put(r.getId(), r);
            submitCount++;
        }

        @Override
        public void approve(AdoptionRequest r) {
            r.approve();
            approveCount++;
        }

        @Override
        public void reject(AdoptionRequest r) {
            r.reject();
            rejectCount++;
        }

        @Override
        public void cancel(AdoptionRequest r) {
            r.cancel();
            cancelCount++;
        }

        @Override
        public List<AdoptionRequest> getRequestsByAdopter(Adopter a) {
            List<AdoptionRequest> result = new ArrayList<>();
            for (AdoptionRequest r : store.values()) {
                if (r.getAdopter().getId().equals(a.getId())) result.add(r);
            }
            return result;
        }

        @Override
        public List<AdoptionRequest> getRequestsByShelter(Shelter s) {
            return List.of();
        }

        @Override
        public List<AdoptionRequest> getRequestsByAnimal(Animal a) {
            return List.of();
        }

        @Override
        public List<AdoptionRequest> getRequestsAfter(LocalDate d) {
            return List.of();
        }

        @Override
        public List<AdoptionRequest> getApprovedAfter(LocalDate d) {
            return List.of();
        }
    }

    /**
     * Stub implementation of {@link AnimalService} backed by an in-memory map.
     * Tracks update calls to verify that animal state is persisted after approval.
     */
    private static class StubAnimalService implements AnimalService {

        final Map<String, Animal> store = new HashMap<>();
        int updateCount;

        @Override
        public void register(Animal a, Shelter s) {
            store.put(a.getId(), a);
        }

        @Override
        public void update(Animal a) {
            store.put(a.getId(), a);
            updateCount++;
        }

        @Override
        public void remove(Animal a) {
            store.remove(a.getId());
        }

        @Override
        public Animal findById(String id) {
            Animal a = store.get(id);
            if (a == null) throw new EntityNotFoundException("Animal not found: " + id);
            return a;
        }

        @Override
        public List<Animal> getAnimalsByShelter(Shelter s) {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Animal> registeredAfter(LocalDate d) {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Animal> adoptedAfter(LocalDate d) {
            return List.of();
        }

        @Override
        public List<Animal> adoptedBy(Adopter a) {
            return List.of();
        }
    }

    /**
     * Stub implementation of {@link AdopterService} backed by an in-memory map.
     * Tracks update calls to verify that adopter state is persisted after approval.
     */
    private static class StubAdopterService implements AdopterService {

        final Map<String, Adopter> store = new HashMap<>();
        int updateCount;

        @Override
        public void register(Adopter a) {
            store.put(a.getId(), a);
        }

        @Override
        public void update(Adopter a) {
            store.put(a.getId(), a);
            updateCount++;
        }

        @Override
        public void remove(Adopter a) {
            store.remove(a.getId());
        }

        @Override
        public List<Adopter> listAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Adopter findById(String id) {
            Adopter a = store.get(id);
            if (a == null) throw new EntityNotFoundException("Adopter not found: " + id);
            return a;
        }

        @Override
        public List<Adopter> registeredAfter(LocalDate d) {
            return List.of();
        }

        @Override
        public List<Adopter> adoptedAfter(LocalDate d) {
            return List.of();
        }

        @Override
        public List<Adopter> adoptedAnimal(Animal a) {
            return List.of();
        }
    }

    /**
     * Stub implementation of {@link RequestNotificationService} that counts notification calls.
     * Use {@link #adoptionNotifyCount} to assert adoption notifications were dispatched.
     */
    private static class StubNotificationService implements RequestNotificationService {

        int adoptionNotifyCount;
        int transferNotifyCount;

        @Override
        public void notifyAdoptionStatusChange(AdoptionRequest r) {
            adoptionNotifyCount++;
        }

        @Override
        public void notifyTransferStatusChange(TransferRequest r) {
            transferNotifyCount++;
        }

        @Override
        public List<NotificationRecord> listAll() {
            return List.of();
        }

        @Override
        public List<NotificationRecord> getByStaff(Staff s) {
            return List.of();
        }

        @Override
        public List<NotificationRecord> getByTarget(String t) {
            return List.of();
        }

        @Override
        public List<NotificationRecord> searchByAction(String k) {
            return List.of();
        }
    }

    /**
     * Spy implementation of {@link AuditService} that records the action strings passed to {@link #log}.
     * Use {@link #actions} to assert which operations were audited.
     *
     * @param <T> the type of the audit target
     */
    private static class SpyAuditService<T> implements AuditService<T> {

        final List<String> actions = new ArrayList<>();

        @Override
        public void log(String action, T target) {
            actions.add(action);
        }

        @Override
        public List<AuditEntry<T>> getLog() {
            return List.of();
        }
    }
}
