package shelter.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.application.impl.TransferApplicationServiceImpl;
import shelter.domain.*;
import shelter.exception.AnimalNotAvailableException;
import shelter.exception.AnimalNotInShelterException;
import shelter.exception.EntityNotFoundException;
import shelter.service.*;
import shelter.service.model.AuditEntry;
import shelter.service.model.NotificationRecord;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransferApplicationServiceImpl}.
 * Verifies transfer request, approval, rejection, cancellation, and guard conditions.
 */
class TransferApplicationServiceImplTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private StubTransferService transferService;
    private StubAnimalService animalService;
    private StubShelterService shelterService;
    private StubNotificationService notificationService;
    private SpyAuditService<TransferRequest> auditService;
    private TransferApplicationServiceImpl service;
    private Shelter from;
    private Shelter to;
    private Dog dog;

    @BeforeEach
    void setUp() {
        transferService     = new StubTransferService();
        animalService       = new StubAnimalService();
        shelterService      = new StubShelterService();
        notificationService = new StubNotificationService();
        auditService        = new SpyAuditService<>();
        service = new TransferApplicationServiceImpl(
                transferService, animalService, shelterService, notificationService, auditService);

        from = new Shelter("From Shelter", "Boston", 10);
        to   = new Shelter("To Shelter",   "Cambridge", 10);
        dog  = new Dog("Rex", "Lab", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        dog.setShelterId(from.getId());

        shelterService.store.put(from.getId(), from);
        shelterService.store.put(to.getId(), to);
        animalService.store.put(dog.getId(), dog);
        from.addAnimal(dog);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void requestTransfer_createsRequestAndNotifies() {
        TransferRequest result = service.requestTransfer(dog.getId(), from.getId(), to.getId());

        assertNotNull(result);
        assertEquals(1, transferService.requestCount);
        assertEquals(1, notificationService.transferNotifyCount);
        assertTrue(auditService.actions.contains("requested transfer"));
    }

    @Test
    void requestTransfer_animalNotInShelter_throws() {
        Dog other = new Dog("Buddy", "Poodle", LocalDate.now().minusYears(2), ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        animalService.store.put(other.getId(), other);

        assertThrows(AnimalNotInShelterException.class,
                () -> service.requestTransfer(other.getId(), from.getId(), to.getId()));
    }

    @Test
    void requestTransfer_animalAlreadyAdopted_throws() {
        dog.setAdopterId("someone");

        assertThrows(AnimalNotAvailableException.class,
                () -> service.requestTransfer(dog.getId(), from.getId(), to.getId()));
    }

    @Test
    void approveTransfer_approvesAndNotifies() {
        TransferRequest request = new TransferRequest(dog, from, to);
        transferService.store.put(request.getId(), request);

        service.approveTransfer(request.getId());

        assertEquals(1, transferService.approveCount);
        assertEquals(1, notificationService.transferNotifyCount);
        assertTrue(auditService.actions.contains("approved transfer"));
    }

    @Test
    void rejectTransfer_rejectsAndLogs() {
        TransferRequest request = new TransferRequest(dog, from, to);
        transferService.store.put(request.getId(), request);

        service.rejectTransfer(request.getId());

        assertEquals(1, transferService.rejectCount);
        assertTrue(auditService.actions.contains("rejected transfer"));
    }

    @Test
    void cancelTransfer_dismissesAndLogs() {
        TransferRequest request = new TransferRequest(dog, from, to);
        transferService.store.put(request.getId(), request);

        service.cancelTransfer(request.getId());

        assertEquals(1, transferService.dismissCount);
        assertTrue(auditService.actions.contains("cancelled transfer"));
    }

    @Test
    void approveTransfer_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.approveTransfer("missing"));
    }

    // -------------------------------------------------------------------------
    // Stubs
    // -------------------------------------------------------------------------

    /**
     * Stub implementation of {@link TransferService} backed by an in-memory map.
     * Counters track how many times each state-transition method was invoked.
     */
    private static class StubTransferService implements TransferService {

        final Map<String, TransferRequest> store = new HashMap<>();
        int requestCount;
        int approveCount;
        int rejectCount;
        int dismissCount;

        @Override
        public TransferRequest requestTransfer(Animal a, Shelter from, Shelter to) {
            TransferRequest r = new TransferRequest(a, from, to);
            store.put(r.getId(), r);
            requestCount++;
            return r;
        }

        @Override
        public void approve(TransferRequest r) {
            r.approve();
            approveCount++;
        }

        @Override
        public void reject(TransferRequest r) {
            r.reject();
            rejectCount++;
        }

        @Override
        public void dismiss(TransferRequest r) {
            r.cancel();
            dismissCount++;
        }

        @Override
        public List<TransferRequest> getPendingRequests(Shelter s) {
            return new ArrayList<>(store.values());
        }
    }

    /**
     * Stub implementation of {@link AnimalService} backed by an in-memory map.
     * Only lookup and update paths are exercised in these tests.
     */
    private static class StubAnimalService implements AnimalService {

        final Map<String, Animal> store = new HashMap<>();

        @Override
        public void register(Animal a, Shelter s) {
            store.put(a.getId(), a);
        }

        @Override
        public void update(Animal a) {
            store.put(a.getId(), a);
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
            return List.of();
        }

        @Override
        public List<Animal> registeredAfter(java.time.LocalDate d) {
            return List.of();
        }

        @Override
        public List<Animal> adoptedAfter(java.time.LocalDate d) {
            return List.of();
        }

        @Override
        public List<Animal> adoptedBy(Adopter a) {
            return List.of();
        }
    }

    /**
     * Stub implementation of {@link ShelterService} backed by an in-memory map.
     * Only lookup paths are exercised in these tests.
     */
    private static class StubShelterService implements ShelterService {

        final Map<String, Shelter> store = new HashMap<>();

        @Override
        public void register(Shelter s) {
            store.put(s.getId(), s);
        }

        @Override
        public void update(Shelter s) {
            store.put(s.getId(), s);
        }

        @Override
        public void remove(Shelter s) {
            store.remove(s.getId());
        }

        @Override
        public Shelter findById(String id) {
            Shelter s = store.get(id);
            if (s == null) throw new EntityNotFoundException("Shelter not found: " + id);
            return s;
        }

        @Override
        public List<Shelter> listAll() {
            return new ArrayList<>(store.values());
        }
    }

    /**
     * Stub implementation of {@link RequestNotificationService} that counts notification calls.
     * Use {@link #transferNotifyCount} to assert transfer notifications were dispatched.
     */
    private static class StubNotificationService implements RequestNotificationService {

        int transferNotifyCount;

        @Override
        public void notifyAdoptionStatusChange(AdoptionRequest r) {
            // not under test
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
