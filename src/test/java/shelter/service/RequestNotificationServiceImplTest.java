package shelter.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.ActivityLevel;
import shelter.domain.Adopter;
import shelter.domain.AdopterPreferences;
import shelter.domain.AdoptionRequest;
import shelter.domain.DailySchedule;
import shelter.domain.Dog;
import shelter.domain.LivingSpace;
import shelter.domain.Shelter;
import shelter.domain.Species;
import shelter.domain.Staff;
import shelter.domain.TransferRequest;
import shelter.repository.AuditRepository;
import shelter.service.impl.RequestNotificationServiceImpl;
import shelter.service.model.AuditEntry;
import shelter.service.model.NotificationRecord;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RequestNotificationServiceImpl}, verifying that notifications are
 * printed to the console, persisted via the audit repository, and queryable from the
 * in-memory record list.
 */
class RequestNotificationServiceImplTest {

    private SpyAuditRepository auditRepo;
    private RequestNotificationServiceImpl service;
    private Staff staff;

    private Adopter adopter;
    private Dog dog;
    private AdoptionRequest adoptionRequest;

    private Shelter shelterA;
    private Shelter shelterB;
    private TransferRequest transferRequest;

    private ByteArrayOutputStream outCapture;
    private PrintStream originalOut;

    /**
     * Redirects stdout and sets up a seeded adoption request and transfer request before each test.
     */
    @BeforeEach
    void setUp() {
        // Redirect System.out so we can assert on printed lines
        outCapture = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outCapture));

        staff = new Staff("admin", "Manager");
        auditRepo = new SpyAuditRepository();
        service = new RequestNotificationServiceImpl(staff, auditRepo);

        // Seed an adoption request
        AdopterPreferences prefs = new AdopterPreferences(Species.DOG, null, null, 0, 10);
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null, prefs);
        dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3), ActivityLevel.HIGH, false, Dog.Size.LARGE, false);
        adoptionRequest = new AdoptionRequest(adopter, dog);

        // Seed a transfer request
        shelterA = new Shelter("Shelter A", "Boston", 10);
        shelterB = new Shelter("Shelter B", "Cambridge", 10);
        dog.setShelterId(shelterA.getId());
        shelterA.addAnimal(dog);
        transferRequest = new TransferRequest(dog, shelterA, shelterB);
    }

    /**
     * Restores the original stdout after each test.
     */
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ── notifyAdoptionStatusChange ────────────────────────────────────────────

    /**
     * Verifies that notifyAdoptionStatusChange prints a [NOTIFY] line to the console.
     */
    @Test
    void notifyAdoption_printsNotifyLineToConsole() {
        service.notifyAdoptionStatusChange(adoptionRequest);

        String output = outCapture.toString();
        assertTrue(output.contains("[NOTIFY]"), "Expected [NOTIFY] prefix in console output");
    }

    /**
     * Verifies that the console line contains the adopter name, animal name, and request status.
     */
    @Test
    void notifyAdoption_consoleLineContainsKeyFields() {
        service.notifyAdoptionStatusChange(adoptionRequest);

        String output = outCapture.toString();
        assertTrue(output.contains("Alice"),   "Expected adopter name in output");
        assertTrue(output.contains("Rex"),     "Expected animal name in output");
        assertTrue(output.contains("PENDING"), "Expected request status in output");
    }

    /**
     * Verifies that notifyAdoptionStatusChange adds a record retrievable via listAll().
     */
    @Test
    void notifyAdoption_addsRecordToList() {
        service.notifyAdoptionStatusChange(adoptionRequest);

        assertEquals(1, service.listAll().size());
    }

    /**
     * Verifies that notifyAdoptionStatusChange calls auditRepository.append().
     */
    @Test
    void notifyAdoption_callsAuditRepositoryAppend() {
        service.notifyAdoptionStatusChange(adoptionRequest);

        assertEquals(1, auditRepo.appendCallCount);
        assertTrue(auditRepo.lastAction.contains("[NOTIFICATION]"));
    }

    /**
     * Verifies that notifyAdoptionStatusChange rejects a null request.
     */
    @Test
    void notifyAdoption_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.notifyAdoptionStatusChange(null));
    }

    // ── notifyTransferStatusChange ────────────────────────────────────────────

    /**
     * Verifies that notifyTransferStatusChange prints a [NOTIFY] line to the console.
     */
    @Test
    void notifyTransfer_printsNotifyLineToConsole() {
        service.notifyTransferStatusChange(transferRequest);

        String output = outCapture.toString();
        assertTrue(output.contains("[NOTIFY]"), "Expected [NOTIFY] prefix in console output");
    }

    /**
     * Verifies that the console line contains both shelter names and the animal name.
     */
    @Test
    void notifyTransfer_consoleLineContainsKeyFields() {
        service.notifyTransferStatusChange(transferRequest);

        String output = outCapture.toString();
        assertTrue(output.contains("Rex"),       "Expected animal name in output");
        assertTrue(output.contains("Shelter A"), "Expected source shelter in output");
        assertTrue(output.contains("Shelter B"), "Expected destination shelter in output");
    }

    /**
     * Verifies that notifyTransferStatusChange rejects a null request.
     */
    @Test
    void notifyTransfer_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.notifyTransferStatusChange(null));
    }

    // ── query methods ─────────────────────────────────────────────────────────

    /**
     * Verifies that getByTarget returns only records matching the given request ID.
     */
    @Test
    void getByTarget_returnsMatchingRecords() {
        service.notifyAdoptionStatusChange(adoptionRequest);

        List<NotificationRecord> result = service.getByTarget(adoptionRequest.getId());
        assertEquals(1, result.size());
        assertEquals(adoptionRequest.getId(), result.get(0).getTargetId());
    }

    /**
     * Verifies that searchByAction returns records whose action contains the keyword.
     */
    @Test
    void searchByAction_returnsMatchingRecords() {
        service.notifyAdoptionStatusChange(adoptionRequest);
        service.notifyTransferStatusChange(transferRequest);

        List<NotificationRecord> result = service.searchByAction("adoption");
        assertEquals(1, result.size());
    }

    /**
     * Verifies that listAll returns all dispatched notifications.
     */
    @Test
    void listAll_returnsAllRecords() {
        service.notifyAdoptionStatusChange(adoptionRequest);
        service.notifyTransferStatusChange(transferRequest);

        assertEquals(2, service.listAll().size());
    }

    // ── in-memory stubs ───────────────────────────────────────────────────────

    /** Spy repository that counts append() calls and records the last action string. */
    private static class SpyAuditRepository implements AuditRepository {
        int appendCallCount = 0;
        String lastAction = null;

        @Override
        public void append(String staffId, String staffName, String action,
                           String targetDescription, LocalDateTime timestamp) {
            appendCallCount++;
            lastAction = action;
        }

        @Override
        public List<AuditEntry<String>> findAll() { return new ArrayList<>(); }

        @Override
        public void clear() { appendCallCount = 0; lastAction = null; }
    }
}
