package shelter.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shelter.domain.Staff;
import shelter.repository.AuditRepository;
import shelter.repository.csv.CsvAuditRepository;
import shelter.service.impl.AuditServiceImpl;
import shelter.service.model.AuditEntry;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AuditServiceImpl}, verifying that log entries are stored in memory,
 * persisted to CSV, and printed to the console with the expected format.
 */
class AuditServiceImplTest {

    @TempDir
    Path tempDir;

    private AuditRepository auditRepository;
    private AuditServiceImpl<String> service;
    private Staff staff;

    private ByteArrayOutputStream outCapture;
    private PrintStream originalOut;

    /**
     * Redirects stdout before each test so console output can be inspected.
     * Constructs a real CsvAuditRepository backed by the JUnit temporary directory.
     */
    @BeforeEach
    void setUp() {
        // Redirect System.out so we can assert on printed lines
        outCapture = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outCapture));

        staff = new Staff("admin", "Manager");
        auditRepository = new CsvAuditRepository(tempDir.toString());
        service = new AuditServiceImpl<>(staff, auditRepository);
    }

    /**
     * Restores the original stdout after each test.
     */
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ── in-memory log ─────────────────────────────────────────────────────────

    /**
     * Verifies that log() adds an entry to the in-memory list returned by getLog().
     */
    @Test
    void log_addsEntryToInMemoryLog() {
        service.log("approved adoption", "AdoptionRequest#001");

        List<AuditEntry<String>> log = service.getLog();
        assertEquals(1, log.size());
        assertEquals("approved adoption", log.get(0).getAction());
        assertEquals("AdoptionRequest#001", log.get(0).getTarget());
        assertEquals(staff.getName(), log.get(0).getStaff().getName());
    }

    /**
     * Verifies that multiple log() calls accumulate all entries in order.
     */
    @Test
    void log_multipleEntries_allStoredInOrder() {
        service.log("submitted adoption", "req-1");
        service.log("approved adoption", "req-1");

        List<AuditEntry<String>> log = service.getLog();
        assertEquals(2, log.size());
        assertEquals("submitted adoption", log.get(0).getAction());
        assertEquals("approved adoption", log.get(1).getAction());
    }

    // ── CSV persistence ───────────────────────────────────────────────────────

    /**
     * Verifies that log() appends a row to the CSV file so the entry survives across sessions.
     */
    @Test
    void log_writesRowToCsvFile() throws Exception {
        service.log("approved adoption", "AdoptionRequest#001");

        Path csvFile = tempDir.resolve("audit.csv");
        List<String> lines = Files.readAllLines(csvFile);

        // Header plus one data row
        assertEquals(2, lines.size());
        assertTrue(lines.get(1).contains("approved adoption"));
        assertTrue(lines.get(1).contains("admin"));
    }

    /**
     * Verifies that each subsequent log() call appends a new row without overwriting previous ones.
     */
    @Test
    void log_appendsWithoutOverwriting() throws Exception {
        service.log("submitted adoption", "req-1");
        service.log("approved adoption", "req-1");

        Path csvFile = tempDir.resolve("audit.csv");
        List<String> lines = Files.readAllLines(csvFile);

        // Header plus two data rows
        assertEquals(3, lines.size());
    }

    // ── console output ────────────────────────────────────────────────────────

    /**
     * Verifies that log() prints a line starting with [INFO] to the console.
     */
    @Test
    void log_printsInfoLineToConsole() {
        service.log("approved adoption", "AdoptionRequest#001");

        String output = outCapture.toString();
        assertTrue(output.contains("[INFO]"), "Expected [INFO] prefix in console output");
    }

    /**
     * Verifies that the console line contains the staff name, action, and target.
     */
    @Test
    void log_consoleLineContainsAllFields() {
        service.log("approved adoption", "AdoptionRequest#001");

        String output = outCapture.toString();
        assertTrue(output.contains("admin"),               "Expected staff name in output");
        assertTrue(output.contains("approved adoption"),   "Expected action in output");
        assertTrue(output.contains("AdoptionRequest#001"), "Expected target in output");
    }

    // ── null / blank guards ───────────────────────────────────────────────────

    @Test void log_nullAction_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.log(null, "target"));
    }

    @Test void log_blankAction_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.log("  ", "target"));
    }

    @Test void log_nullTarget_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.log("action", null));
    }
}
