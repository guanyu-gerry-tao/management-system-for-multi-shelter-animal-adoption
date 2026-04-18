package shelter.service.impl;

import org.junit.jupiter.api.Test;
import shelter.domain.Species;
import shelter.domain.VaccinationRecord;
import shelter.domain.VaccineType;
import shelter.repository.VaccinationRecordRepository;
import shelter.repository.VaccineTypeRepository;
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
 * Unit tests for {@link VaccinationServiceImpl#listAllRecords()}.
 * Verifies that the method delegates to the underlying {@link VaccinationRecordRepository#findAll()}
 * and returns every persisted vaccination record.
 */
class VaccinationServiceImplListAllTest {

    @Test
    void listAllRecords_returnsEveryRecord() {
        StubRecordRepo repo = new StubRecordRepo();
        VaccinationRecord r1 = new VaccinationRecord("animal-1", "vaccine-1", LocalDate.now().minusDays(10));
        VaccinationRecord r2 = new VaccinationRecord("animal-2", "vaccine-1", LocalDate.now().minusDays(5));
        repo.records.add(r1);
        repo.records.add(r2);

        VaccinationServiceImpl svc = new VaccinationServiceImpl(
                repo, new StubTypeRepo(), new NoopAudit<>());

        List<VaccinationRecord> all = svc.listAllRecords();

        assertEquals(2, all.size());
        assertTrue(all.contains(r1));
        assertTrue(all.contains(r2));
    }

    @Test
    void listAllRecords_emptyRepository_returnsEmptyList() {
        VaccinationServiceImpl svc = new VaccinationServiceImpl(
                new StubRecordRepo(), new StubTypeRepo(), new NoopAudit<>());

        assertTrue(svc.listAllRecords().isEmpty());
    }

    /**
     * Minimal in-memory stub for {@link VaccinationRecordRepository}.
     * Only {@link #findAll()} is exercised by these tests; other methods return empty results.
     */
    private static class StubRecordRepo implements VaccinationRecordRepository {
        private final List<VaccinationRecord> records = new ArrayList<>();

        @Override
        public void save(VaccinationRecord record) {
            records.add(record);
        }

        @Override
        public Optional<VaccinationRecord> findById(String id) {
            return Optional.empty();
        }

        @Override
        public List<VaccinationRecord> findByAnimalId(String animalId) {
            return Collections.emptyList();
        }

        @Override
        public List<VaccinationRecord> findAll() {
            return new ArrayList<>(records);
        }

        @Override
        public List<VaccinationRecord> findByShelterId(String shelterId) {
            return Collections.emptyList();
        }

        @Override
        public void delete(String id) {
            // no-op
        }
    }

    /**
     * Minimal in-memory stub for {@link VaccineTypeRepository}; all methods are no-ops.
     */
    private static class StubTypeRepo implements VaccineTypeRepository {
        @Override
        public void save(VaccineType vaccineType) {
            // no-op
        }

        @Override
        public Optional<VaccineType> findById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<VaccineType> findByName(String name) {
            return Optional.empty();
        }

        @Override
        public List<VaccineType> findByApplicableSpecies(Species species) {
            return Collections.emptyList();
        }

        @Override
        public List<VaccineType> findAll() {
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
