package shelter.service.impl;

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
import shelter.repository.AdopterRepository;
import shelter.repository.AdoptionRequestRepository;
import shelter.repository.AnimalRepository;
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
 * Unit tests for {@link AdoptionServiceImpl#listAll()}.
 * Verifies that the method delegates to the underlying {@link AdoptionRequestRepository#findAll()}
 * and returns every persisted request (or an empty list when none are stored).
 */
class AdoptionServiceImplListAllTest {

    /**
     * Builds a minimal valid {@link Adopter} for use in the stub repository.
     */
    private static Adopter newAdopter() {
        return new Adopter("Alice", LivingSpace.APARTMENT, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(null, null, null, null, 0, 20));
    }

    /**
     * Builds a minimal valid {@link Dog} for use in the stub repository.
     */
    private static Dog newDog() {
        return new Dog("Rex", "Lab", LocalDate.now().minusYears(2), ActivityLevel.LOW,
                false, Dog.Size.MEDIUM, false);
    }

    @Test
    void listAll_returnsEveryRequestFromRepository() {
        StubAdoptionRequestRepo repo = new StubAdoptionRequestRepo();
        Adopter a = newAdopter();
        Dog d = newDog();
        AdoptionRequest r1 = new AdoptionRequest(a, d);
        AdoptionRequest r2 = new AdoptionRequest(a, d);
        repo.records.add(r1);
        repo.records.add(r2);

        AdoptionServiceImpl service = new AdoptionServiceImpl(
                repo, new StubAnimalRepo(), new StubAdopterRepo(), new NoopAudit<>());

        List<AdoptionRequest> all = service.listAll();

        assertEquals(2, all.size());
        assertTrue(all.contains(r1));
        assertTrue(all.contains(r2));
    }

    @Test
    void listAll_emptyRepository_returnsEmptyList() {
        AdoptionServiceImpl service = new AdoptionServiceImpl(
                new StubAdoptionRequestRepo(), new StubAnimalRepo(), new StubAdopterRepo(), new NoopAudit<>());

        assertTrue(service.listAll().isEmpty());
    }

    /**
     * Minimal in-memory stub for {@link AdoptionRequestRepository}.
     * Only {@link #findAll()} is exercised by these tests; other methods return empty results.
     */
    private static class StubAdoptionRequestRepo implements AdoptionRequestRepository {
        private final List<AdoptionRequest> records = new ArrayList<>();

        @Override
        public void save(AdoptionRequest request) {
            records.add(request);
        }

        @Override
        public Optional<AdoptionRequest> findById(String id) {
            return Optional.empty();
        }

        @Override
        public List<AdoptionRequest> findAll() {
            return new ArrayList<>(records);
        }

        @Override
        public List<AdoptionRequest> findByAdopterId(String adopterId) {
            return Collections.emptyList();
        }

        @Override
        public List<AdoptionRequest> findByAnimalId(String animalId) {
            return Collections.emptyList();
        }

        @Override
        public List<AdoptionRequest> findByShelterId(String shelterId) {
            return Collections.emptyList();
        }

        @Override
        public List<AdoptionRequest> findByStatus(RequestStatus status) {
            return Collections.emptyList();
        }

        @Override
        public List<AdoptionRequest> findByAdopterIdAndStatus(String adopterId, RequestStatus status) {
            return Collections.emptyList();
        }

        @Override
        public List<AdoptionRequest> findByShelterIdAndStatus(String shelterId, RequestStatus status) {
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
     * Minimal in-memory stub for {@link AdopterRepository}; all methods are no-ops.
     */
    private static class StubAdopterRepo implements AdopterRepository {
        @Override
        public void save(Adopter adopter) {
            // no-op
        }

        @Override
        public Optional<Adopter> findById(String id) {
            return Optional.empty();
        }

        @Override
        public List<Adopter> findAll() {
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
