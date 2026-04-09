package shelter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.ActivityLevel;
import shelter.domain.Dog;
import shelter.domain.Species;
import shelter.domain.VaccinationRecord;
import shelter.domain.VaccineType;
import shelter.exception.EntityNotFoundException;
import shelter.exception.SpeciesMismatchException;
import shelter.repository.VaccinationRecordRepository;
import shelter.repository.VaccineTypeRepository;
import shelter.service.impl.VaccinationServiceImpl;
import shelter.service.model.AuditEntry;
import shelter.service.model.OverdueVaccination;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link VaccinationServiceImpl}, covering vaccination recording,
 * overdue detection, history queries, and null guards. Repositories are replaced
 * with in-memory stubs to isolate service logic from persistence concerns.
 */
class VaccinationServiceImplTest {

    private StubVaccinationRecordRepository recordRepo;
    private StubVaccineTypeRepository typeRepo;
    private VaccinationServiceImpl service;

    private Dog dog;
    private VaccineType rabies;   // valid 365 days, for DOG
    private VaccineType bordetella; // valid 180 days, for DOG

    /**
     * Sets up fresh stubs and a dog with two applicable vaccine types before each test.
     */
    @BeforeEach
    void setUp() {
        recordRepo = new StubVaccinationRecordRepository();
        typeRepo = new StubVaccineTypeRepository();
        service = new VaccinationServiceImpl(recordRepo, typeRepo, new NoOpAuditService<>());

        dog = new Dog("Rex", "Labrador", 3, ActivityLevel.HIGH, false, Dog.Size.LARGE, false);
        dog.setShelterId("shelter-1");

        rabies = new VaccineType("Rabies", Species.DOG, 365);
        bordetella = new VaccineType("Bordetella", Species.DOG, 180);
        typeRepo.save(rabies);
        typeRepo.save(bordetella);
    }

    // ── recordVaccination ─────────────────────────────────────────────────────

    /**
     * Verifies that a valid vaccination is persisted to the repository.
     */
    @Test
    void recordVaccination_valid_savesRecord() {
        service.recordVaccination(dog, rabies, LocalDate.now());

        assertEquals(1, recordRepo.findAll().size());
        assertEquals(dog.getId(), recordRepo.findAll().get(0).getAnimalId());
        assertEquals(rabies.getId(), recordRepo.findAll().get(0).getVaccineTypeId());
    }

    /**
     * Verifies that vaccinating a dog with a cat-specific vaccine throws SpeciesMismatchException.
     */
    @Test
    void recordVaccination_speciesMismatch_throws() {
        VaccineType catVaccine = new VaccineType("FVRCP", Species.CAT, 365);
        assertThrows(SpeciesMismatchException.class,
                () -> service.recordVaccination(dog, catVaccine, LocalDate.now()));
    }

    /**
     * Verifies that recordVaccination rejects null inputs.
     */
    @Test void recordVaccination_nullAnimal_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordVaccination(null, rabies, LocalDate.now()));
    }

    @Test void recordVaccination_nullVaccineType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordVaccination(dog, null, LocalDate.now()));
    }

    @Test void recordVaccination_nullDate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordVaccination(dog, rabies, null));
    }

    // ── getOverdueVaccinations ────────────────────────────────────────────────

    /**
     * Verifies that a vaccine never given is reported as overdue.
     */
    @Test
    void getOverdueVaccinations_neverGiven_allOverdue() {
        List<OverdueVaccination> overdue = service.getOverdueVaccinations(dog);

        // Both rabies and bordetella have never been given
        assertEquals(2, overdue.size());
    }

    /**
     * Verifies that a recently given vaccine within its validity window is not overdue.
     */
    @Test
    void getOverdueVaccinations_recentVaccine_notOverdue() {
        // Rabies given today — valid for 365 days, not overdue
        recordRepo.save(new VaccinationRecord(dog.getId(), rabies.getId(), LocalDate.now()));

        List<OverdueVaccination> overdue = service.getOverdueVaccinations(dog);

        // Only bordetella should be overdue
        assertEquals(1, overdue.size());
        assertEquals(bordetella.getId(), overdue.get(0).getVaccineType().getId());
    }

    /**
     * Verifies that a vaccine given beyond its validity period is reported as overdue.
     */
    @Test
    void getOverdueVaccinations_expiredVaccine_isOverdue() {
        // Rabies given 400 days ago — validity is 365 days, so overdue
        LocalDate longAgo = LocalDate.now().minusDays(400);
        recordRepo.save(new VaccinationRecord(dog.getId(), rabies.getId(), longAgo));

        List<OverdueVaccination> overdue = service.getOverdueVaccinations(dog);
        boolean rabiesOverdue = overdue.stream()
                .anyMatch(o -> o.getVaccineType().getId().equals(rabies.getId()));

        assertTrue(rabiesOverdue);
    }

    /**
     * Verifies that only the most recent dose is used when checking overdue status.
     */
    @Test
    void getOverdueVaccinations_usesLatestDose() {
        // Old dose 400 days ago, new dose today — should NOT be overdue
        LocalDate longAgo = LocalDate.now().minusDays(400);
        recordRepo.save(new VaccinationRecord(dog.getId(), rabies.getId(), longAgo));
        recordRepo.save(new VaccinationRecord(dog.getId(), rabies.getId(), LocalDate.now()));

        List<OverdueVaccination> overdue = service.getOverdueVaccinations(dog);
        boolean rabiesOverdue = overdue.stream()
                .anyMatch(o -> o.getVaccineType().getId().equals(rabies.getId()));

        assertFalse(rabiesOverdue);
    }

    /**
     * Verifies that getOverdueVaccinations rejects a null animal.
     */
    @Test
    void getOverdueVaccinations_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getOverdueVaccinations(null));
    }

    // ── getVaccinationHistory ─────────────────────────────────────────────────

    /**
     * Verifies that getVaccinationHistory returns all records for the given animal.
     */
    @Test
    void getVaccinationHistory_returnsAllRecords() {
        recordRepo.save(new VaccinationRecord(dog.getId(), rabies.getId(), LocalDate.now()));
        recordRepo.save(new VaccinationRecord(dog.getId(), bordetella.getId(), LocalDate.now()));

        assertEquals(2, service.getVaccinationHistory(dog).size());
    }

    /**
     * Verifies that getVaccinationHistory returns empty list when no records exist.
     */
    @Test
    void getVaccinationHistory_noRecords_returnsEmpty() {
        assertTrue(service.getVaccinationHistory(dog).isEmpty());
    }

    /**
     * Verifies that getVaccinationHistory rejects a null animal.
     */
    @Test
    void getVaccinationHistory_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getVaccinationHistory(null));
    }

    // ── findById ─────────────────────────────────────────────────────────────

    /**
     * Verifies that findById returns the correct record when it exists.
     */
    @Test
    void findById_existing_returnsRecord() {
        VaccinationRecord record = new VaccinationRecord(dog.getId(), rabies.getId(), LocalDate.now());
        recordRepo.save(record);

        VaccinationRecord found = service.findById(record.getId());
        assertEquals(record.getId(), found.getId());
    }

    /**
     * Verifies that findById throws EntityNotFoundException when the ID does not exist.
     */
    @Test
    void findById_notFound_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.findById("nonexistent"));
    }

    /**
     * Verifies that findById rejects null and blank IDs.
     */
    @Test void findById_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.findById(null));
    }

    @Test void findById_blank_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.findById("  "));
    }

    // ── audit logging ─────────────────────────────────────────────────────────

    /**
     * Verifies that recordVaccination calls auditService.log().
     */
    @Test
    void recordVaccination_logsAuditEntry() {
        SpyAuditService<VaccinationRecord> spy = new SpyAuditService<>();
        service = new VaccinationServiceImpl(recordRepo, typeRepo, spy);

        service.recordVaccination(dog, rabies, LocalDate.now());

        assertEquals(1, spy.loggedActions.size());
        assertTrue(spy.loggedActions.get(0).contains("Rabies"));
    }

    // ── in-memory stubs ───────────────────────────────────────────────────────

    private static class StubVaccinationRecordRepository implements VaccinationRecordRepository {
        private final Map<String, VaccinationRecord> store = new LinkedHashMap<>();

        @Override public void save(VaccinationRecord r) { store.put(r.getId(), r); }
        @Override public Optional<VaccinationRecord> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<VaccinationRecord> findAll() { return new ArrayList<>(store.values()); }
        @Override public void delete(String id) { store.remove(id); }
        @Override public List<VaccinationRecord> findByAnimalId(String id) {
            return store.values().stream().filter(r -> id.equals(r.getAnimalId())).collect(Collectors.toList());
        }
        @Override public List<VaccinationRecord> findByShelterId(String id) { return new ArrayList<>(); }
    }

    private static class StubVaccineTypeRepository implements VaccineTypeRepository {
        private final Map<String, VaccineType> store = new LinkedHashMap<>();

        @Override public void save(VaccineType v) { store.put(v.getId(), v); }
        @Override public Optional<VaccineType> findById(String id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<VaccineType> findAll() { return new ArrayList<>(store.values()); }
        @Override public void delete(String id) { store.remove(id); }
        @Override public Optional<VaccineType> findByName(String name) {
            return store.values().stream().filter(v -> v.getName().equalsIgnoreCase(name)).findFirst();
        }
        @Override public List<VaccineType> findByApplicableSpecies(Species species) {
            return store.values().stream().filter(v -> v.getApplicableSpecies() == species).collect(Collectors.toList());
        }
    }

    /** No-op audit stub. */
    private static class NoOpAuditService<T> implements AuditService<T> {
        @Override public void log(String action, T target) { }
        @Override public List<AuditEntry<T>> getLog() { return new ArrayList<>(); }
    }

    /** Spy audit stub that records action strings. */
    private static class SpyAuditService<T> implements AuditService<T> {
        final List<String> loggedActions = new ArrayList<>();
        @Override public void log(String action, T target) { loggedActions.add(action); }
        @Override public List<AuditEntry<T>> getLog() { return new ArrayList<>(); }
    }
}
