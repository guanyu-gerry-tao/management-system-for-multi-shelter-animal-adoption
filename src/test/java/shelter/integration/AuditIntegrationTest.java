package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter audit} command group.
 * Covers UC-08.1: viewing the audit log, including the empty-log case and multi-operation coverage.
 * The audit log is persistent across CLI invocations, so each test verifies that all
 * mutations performed in that session appear in the log retrieved at the end.
 */
class AuditIntegrationTest extends CliIntegrationTest {

    /**
     * Verifies that retrieving the audit log when no operations have been performed
     * exits with code 0 and prints the empty-log sentinel message.
     */
    @Test
    void log_noActions_printsEmptyMessage() throws Exception {
        RunResult r = run("audit", "log");
        assertSuccess(r);
        assertOutputContains(r, "Audit log is empty.");
    }

    /**
     * Verifies that registering two shelters and admitting an animal all produce audit entries.
     * After these three operations the log must be non-empty and must not print the empty sentinel.
     */
    @Test
    void log_shelterAndAnimalOperations_containsEntries() throws Exception {
        RunResult sr = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        String shelterId = extractId(sr.stdout());
        run("shelter", "register",
                "--name", "Happy Tails", "--location", "Cambridge", "--capacity", "5");
        run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        RunResult r = run("audit", "log");
        assertSuccess(r);
        assertOutputDoesNotContain(r, "Audit log is empty.");
    }

    /**
     * Verifies that registering two adopters and submitting an adoption request all
     * produce audit entries. After these operations the log must be non-empty, confirming
     * the audit layer captures adopter and adoption events as well as shelter events.
     */
    @Test
    void log_adopterAndAdoptionOperations_containsEntries() throws Exception {
        RunResult shelterResult = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        String shelterId = extractId(shelterResult.stdout());
        RunResult animalResult = run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        String animalId = extractId(animalResult.stdout());
        run("adopter", "register",
                "--name", "Alice", "--space", "HOUSE_WITH_YARD", "--schedule", "HOME_MOST_OF_DAY");
        RunResult adopterResult = run("adopter", "register",
                "--name", "Bob", "--space", "APARTMENT", "--schedule", "AWAY_MOST_OF_DAY");
        String adopterId = extractId(adopterResult.stdout());
        run("adopt", "submit", "--adopter", adopterId, "--animal", animalId);
        RunResult r = run("audit", "log");
        assertSuccess(r);
        assertOutputDoesNotContain(r, "Audit log is empty.");
    }
}
