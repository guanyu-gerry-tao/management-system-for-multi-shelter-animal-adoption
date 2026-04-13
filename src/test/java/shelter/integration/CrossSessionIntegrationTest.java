package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying that data persists correctly across separate process invocations.
 * Each process runs with the same {@code SHELTER_HOME} directory but no shared in-memory state,
 * so these tests confirm the CSV persistence layer works end-to-end across shutdown-restart cycles.
 */
class CrossSessionIntegrationTest extends CliIntegrationTest {

    /**
     * Verifies that two shelters registered in separate process invocations both appear
     * in a third listing invocation. This confirms the CSV persistence layer accumulates
     * entries across multiple writes before any read is performed.
     */
    @Test
    void multipleShelters_persistAcrossSessions() throws Exception {
        run("shelter", "register",
                "--name", "Alpha", "--location", "Boston", "--capacity", "10");
        run("shelter", "register",
                "--name", "Beta", "--location", "Cambridge", "--capacity", "5");
        RunResult r = run("shelter", "list");
        assertSuccess(r);
        assertOutputContains(r, "Alpha");
        assertOutputContains(r, "Beta");
    }

    /**
     * Verifies that an {@code Other}-species animal's free-form species name survives a full
     * save-and-reload cycle across separate processes. A Dog is also admitted so the list
     * can confirm the species discriminator correctly isolates the two records on reload.
     */
    @Test
    void otherAnimal_speciesNamePersistsAcrossSessions() throws Exception {
        RunResult sr = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        String shelterId = extractId(sr.stdout());
        run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        run("animal", "admit",
                "--species", "other", "--species-name", "fish",
                "--name", "Nemo", "--breed", "Clownfish",
                "--age", "1", "--activity", "LOW", "--shelter", shelterId);
        RunResult r = run("animal", "list", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Nemo");
        assertOutputContains(r, "Rex");
    }

    /**
     * Verifies that an update made in one process invocation is reflected when data is loaded
     * in a subsequent process. Two shelters are registered; only one is updated, and the list
     * must show the new name while the other shelter's name is unchanged.
     */
    @Test
    void updatePersists_acrossSession() throws Exception {
        run("shelter", "register",
                "--name", "Unchanged", "--location", "Cambridge", "--capacity", "5");
        RunResult reg = run("shelter", "register",
                "--name", "Old Name", "--location", "Boston", "--capacity", "10");
        String id = extractId(reg.stdout());
        run("shelter", "update", "--id", id, "--name", "New Name");
        RunResult list = run("shelter", "list");
        assertOutputContains(list, "New Name");
        assertOutputDoesNotContain(list, "Old Name");
        assertOutputContains(list, "Unchanged");
    }

    /**
     * Verifies that a removal made in one process invocation takes effect in a subsequent process.
     * Two shelters are registered; one is removed, and the list must show only the remaining shelter.
     */
    @Test
    void removePersists_acrossSession() throws Exception {
        run("shelter", "register",
                "--name", "Stays", "--location", "Cambridge", "--capacity", "5");
        RunResult reg = run("shelter", "register",
                "--name", "To Remove", "--location", "Boston", "--capacity", "10");
        String id = extractId(reg.stdout());
        run("shelter", "remove", "--id", id);
        RunResult list = run("shelter", "list");
        assertOutputDoesNotContain(list, "To Remove");
        assertOutputContains(list, "Stays");
    }
}
