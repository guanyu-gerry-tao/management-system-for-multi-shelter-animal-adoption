package shelter.service.impl;

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
import shelter.service.AuditService;
import shelter.service.model.AuditEntry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TransferServiceImpl#listAll()}.
 * Verifies that the method delegates to {@link TransferRequestRepository#findAll()} and
 * returns every persisted transfer request (or an empty list when none are stored).
 */
class TransferServiceImplListAllTest {

    /**
     * Builds a minimal valid {@link Dog} for use in tests.
     */
    private static Dog newDog() {
        return new Dog("Rex", "Lab", LocalDate.now().minusYears(2),
                ActivityLevel.LOW, false, Dog.Size.MEDIUM, false);
    }

    @Test
    void listAll_returnsEveryRequestFromRepository() {
        Dog dog = newDog();
        Shelter a = new Shelter("Alpha", "Boston", 10);
        Shelter b = new Shelter("Bravo", "Cambridge", 10);
        a.addAnimal(dog);
        TransferRequest t1 = new TransferRequest(dog, a, b);
        TransferRequest t2 = new TransferRequest(dog, a, b);

        StubTransferRepo repo = new StubTransferRepo();
        repo.records.add(t1);
        repo.records.add(t2);

        TransferServiceImpl svc = new TransferServiceImpl(
                repo, new StubAnimalRepo(), new StubShelterRepo(), new NoopAudit<>());

        List<TransferRequest> all = svc.listAll();

        assertEquals(2, all.size());
        assertTrue(all.contains(t1));
        assertTrue(all.contains(t2));
    }

    @Test
    void listAll_emptyRepository_returnsEmptyList() {
        TransferServiceImpl svc = new TransferServiceImpl(
                new StubTransferRepo(), new StubAnimalRepo(), new StubShelterRepo(), new NoopAudit<>());

        assertTrue(svc.listAll().isEmpty());
    }

    /**
     * Minimal in-memory stub for {@link TransferRequestRepository}.
     * Only {@link #findAll()} is exercised by these tests; other methods return empty results.
     */
    private static class StubTransferRepo implements TransferRequestRepository {
        private final List<TransferRequest> records = new ArrayList<>();

        @Override
        public void save(TransferRequest request) {
            records.add(request);
        }

        @Override
        public Optional<TransferRequest> findById(String id) {
            return Optional.empty();
        }

        @Override
        public List<TransferRequest> findAll() {
            return new ArrayList<>(records);
        }

        @Override
        public List<TransferRequest> findByAnimalId(String animalId) {
            return Collections.emptyList();
        }

        @Override
        public List<TransferRequest> findByFromShelterId(String shelterId) {
            return Collections.emptyList();
        }

        @Override
        public List<TransferRequest> findByToShelterId(String shelterId) {
            return Collections.emptyList();
        }

        @Override
        public List<TransferRequest> findByStatus(RequestStatus status) {
            return Collections.emptyList();
        }

        @Override
        public List<TransferRequest> findByShelterIdAndStatus(String shelterId, RequestStatus status) {
            return Collections.emptyList();
        }

        @Override
        public List<TransferRequest> findByAnimalIdAndStatus(String animalId, RequestStatus status) {
            return Collections.emptyList();
        }

        @Override
        public void delete(String id) {
            // no-op
        }
    }

    /**
     * Minimal in-memory stub for {@link AnimalRepository}; all methods are no-ops.
     */
    private static class StubAnimalRepo implements AnimalRepository {
        @Override
        public void save(Animal animal) {
            // no-op
        }

        @Override
        public Optional<Animal> findById(String id) {
            return Optional.empty();
        }

        @Override
        public List<Animal> findAll() {
            return Collections.emptyList();
        }

        @Override
        public List<Animal> findByShelterId(String shelterId) {
            return Collections.emptyList();
        }

        @Override
        public List<Animal> findByAdopterId(String adopterId) {
            return Collections.emptyList();
        }

        @Override
        public void delete(String id) {
            // no-op
        }
    }

    /**
     * Minimal in-memory stub for {@link ShelterRepository}; all methods are no-ops.
     */
    private static class StubShelterRepo implements ShelterRepository {
        @Override
        public void save(Shelter shelter) {
            // no-op
        }

        @Override
        public Optional<Shelter> findById(String id) {
            return Optional.empty();
        }

        @Override
        public List<Shelter> findAll() {
            return Collections.emptyList();
        }

        @Override
        public void delete(String id) {
            // no-op
        }
    }

    /**
     * Minimal no-op audit service for tests that do not care about audit side effects.
     *
     * @param <T> the audit target type
     */
    private static class NoopAudit<T> implements AuditService<T> {
        @Override
        public void log(String action, T target) {
            // no-op
        }

        @Override
        public List<AuditEntry<T>> getLog() {
            return Collections.emptyList();
        }
    }
}
