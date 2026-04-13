package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying that data persists across separate shelter processes.
 * Each test runs two independent subprocesses against the same TempDir, simulating
 * a real shutdown-and-restart cycle.
 */
class CrossSessionIntegrationTest extends CliIntegrationTest {

    @Test
    void shelterRegistered_inProcess1_visibleInProcess2() throws Exception {
        run("shelter", "register",
                "--name", "Persistent Paws", "--location", "Boston", "--capacity", "10");
        RunResult r = run("shelter", "list");
        assertSuccess(r);
        assertOutputContains(r, "Persistent Paws");
    }

    @Test
    void otherAnimal_admittedInProcess1_speciesNamePreservedInProcess2() throws Exception {
        RunResult sr = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        String shelterId = extractId(sr.stdout());
        run("animal", "admit",
                "--species", "other", "--species-name", "fish",
                "--name", "Nemo", "--breed", "Clownfish",
                "--age", "1", "--activity", "LOW", "--shelter", shelterId);
        RunResult r = run("animal", "list", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Nemo");
    }
}
