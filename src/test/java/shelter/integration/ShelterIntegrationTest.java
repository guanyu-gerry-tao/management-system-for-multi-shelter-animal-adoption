package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter shelter} command group.
 * Each test runs against a fresh TempDir; no shared state between methods.
 */
class ShelterIntegrationTest extends CliIntegrationTest {

    @Test
    void register_validArgs_printsNameAndId() throws Exception {
        RunResult r = run("shelter", "register",
                "--name", "Happy Paws", "--location", "Boston", "--capacity", "20");
        assertSuccess(r);
        assertOutputContains(r, "Happy Paws");
        assertOutputContains(r, "id=");
    }

    @Test
    void list_noShelters_printsEmptyMessage() throws Exception {
        RunResult r = run("shelter", "list");
        assertSuccess(r);
        assertOutputContains(r, "No shelters registered.");
    }

    @Test
    void list_afterRegister_showsRegisteredShelter() throws Exception {
        run("shelter", "register",
                "--name", "Happy Paws", "--location", "Boston", "--capacity", "20");
        RunResult r = run("shelter", "list");
        assertSuccess(r);
        assertOutputContains(r, "Happy Paws");
        assertOutputContains(r, "Boston");
    }

    @Test
    void register_missingRequiredOption_exitNonZero() throws Exception {
        RunResult r = run("shelter", "register", "--name", "Incomplete");
        assertNotEquals(0, r.exitCode());
    }
}
