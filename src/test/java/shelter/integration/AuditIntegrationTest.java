package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter audit} command group.
 * Verifies that mutations are recorded in the audit log and visible via CLI.
 */
class AuditIntegrationTest extends CliIntegrationTest {

    @Test
    void log_noActions_printsEmptyMessage() throws Exception {
        RunResult r = run("audit", "log");
        assertSuccess(r);
        assertOutputContains(r, "Audit log is empty.");
    }

    @Test
    void log_afterAdmitAndRegister_containsEntries() throws Exception {
        RunResult sr = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        String shelterId = extractId(sr.stdout());
        run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        RunResult r = run("audit", "log");
        assertSuccess(r);
        assertOutputDoesNotContain(r, "Audit log is empty.");
    }
}
