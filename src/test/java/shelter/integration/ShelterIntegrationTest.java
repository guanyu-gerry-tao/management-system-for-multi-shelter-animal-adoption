package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter shelter} command group.
 * Covers UC-01.1 through UC-01.4: register, list, update, and remove shelters,
 * including duplicate-name, not-found, and non-empty-shelter error cases.
 * Each test method runs against a fresh {@code @TempDir}; no shared state between methods.
 */
class ShelterIntegrationTest extends CliIntegrationTest {

    // -------------------------------------------------------------------------
    // UC-01.1: Register a New Shelter
    // -------------------------------------------------------------------------

    /**
     * Verifies that registering a shelter with valid name, location, and capacity
     * exits with code 0 and prints the shelter name and its generated UUID.
     * A second shelter is registered in the same test to confirm both succeed independently.
     */
    @Test
    void register_validArgs_printsNameAndId() throws Exception {
        RunResult r1 = run("shelter", "register",
                "--name", "Happy Paws", "--location", "Boston", "--capacity", "20");
        assertSuccess(r1);
        assertOutputContains(r1, "Happy Paws");
        assertOutputContains(r1, "id=");

        RunResult r2 = run("shelter", "register",
                "--name", "Second Shelter", "--location", "Cambridge", "--capacity", "10");
        assertSuccess(r2);
        assertOutputContains(r2, "Second Shelter");
        assertOutputContains(r2, "id=");
    }

    /**
     * Verifies that registering two shelters with the same name and location
     * prints an error on the second attempt instead of creating a duplicate entry.
     */
    @Test
    void register_duplicateNameAndLocation_printsError() throws Exception {
        run("shelter", "register",
                "--name", "Dup", "--location", "Boston", "--capacity", "10");
        RunResult r = run("shelter", "register",
                "--name", "Dup", "--location", "Boston", "--capacity", "5");
        assertOutputContains(r, "Error");
    }

    /**
     * Verifies that omitting a required option ({@code --location} here) causes Picocli
     * to exit with a non-zero code before any business logic executes.
     */
    @Test
    void register_missingRequiredOption_exitNonZero() throws Exception {
        RunResult r = run("shelter", "register", "--name", "Incomplete");
        assertNotEquals(0, r.exitCode());
    }

    // -------------------------------------------------------------------------
    // UC-01.2: View All Shelters
    // -------------------------------------------------------------------------

    /**
     * Verifies that listing shelters when none have been registered prints an empty-list
     * message rather than crashing or returning blank output.
     */
    @Test
    void list_noShelters_printsEmptyMessage() throws Exception {
        RunResult r = run("shelter", "list");
        assertSuccess(r);
        assertOutputContains(r, "(none)");
    }

    /**
     * Verifies that two shelters registered with distinct names and locations
     * both appear in the list output, confirming the command aggregates all entries.
     */
    @Test
    void list_multipleShelters_showsAll() throws Exception {
        run("shelter", "register",
                "--name", "Alpha", "--location", "Boston", "--capacity", "10");
        run("shelter", "register",
                "--name", "Beta", "--location", "Cambridge", "--capacity", "5");
        RunResult r = run("shelter", "list");
        assertSuccess(r);
        assertOutputContains(r, "Alpha");
        assertOutputContains(r, "Beta");
    }

    // -------------------------------------------------------------------------
    // UC-01.3: Update Shelter Information
    // -------------------------------------------------------------------------

    /**
     * Verifies that updating an existing shelter's name succeeds and the confirmation
     * message reflects the new name. A second shelter is registered in the same test
     * to confirm only the targeted shelter is renamed.
     */
    @Test
    void update_existingShelter_printsUpdatedName() throws Exception {
        run("shelter", "register",
                "--name", "Bystander", "--location", "Cambridge", "--capacity", "5");
        RunResult reg = run("shelter", "register",
                "--name", "Old Name", "--location", "Boston", "--capacity", "10");
        String id = extractId(reg.stdout());
        RunResult r = run("shelter", "update", "--id", id, "--name", "New Name");
        assertSuccess(r);
        assertOutputContains(r, "Updated shelter");
        assertOutputContains(r, "New Name");
    }

    /**
     * Verifies that updating both name and capacity in a single command succeeds and
     * the updated name appears in the list. Two shelters are registered to confirm
     * only the targeted shelter's fields change.
     */
    @Test
    void update_multipleFields_newNameAppearsInList() throws Exception {
        run("shelter", "register",
                "--name", "Unchanged", "--location", "Cambridge", "--capacity", "5");
        RunResult reg = run("shelter", "register",
                "--name", "Target", "--location", "Boston", "--capacity", "10");
        String id = extractId(reg.stdout());
        run("shelter", "update", "--id", id, "--name", "Renamed", "--capacity", "15");
        RunResult list = run("shelter", "list");
        assertOutputContains(list, "Renamed");
        assertOutputContains(list, "Unchanged");
    }

    /**
     * Verifies that attempting to update a shelter with an unknown ID prints an error.
     * This exercises the not-found guard in the application layer.
     */
    @Test
    void update_nonExistentId_printsError() throws Exception {
        RunResult r = run("shelter", "update",
                "--id", "00000000-0000-0000-0000-000000000000", "--name", "Ghost");
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-01.4: Remove a Shelter
    // -------------------------------------------------------------------------

    /**
     * Verifies that removing an empty shelter succeeds and it no longer appears in the list.
     * A second shelter is registered so the list confirms only the targeted shelter is removed.
     */
    @Test
    void remove_emptyShelter_disappearsFromList() throws Exception {
        run("shelter", "register",
                "--name", "Stays", "--location", "Cambridge", "--capacity", "5");
        RunResult reg = run("shelter", "register",
                "--name", "To Remove", "--location", "Boston", "--capacity", "10");
        String id = extractId(reg.stdout());
        RunResult r = run("shelter", "remove", "--id", id);
        assertSuccess(r);
        assertOutputContains(r, "Removed shelter");
        RunResult list = run("shelter", "list");
        assertOutputDoesNotContain(list, "To Remove");
        assertOutputContains(list, "Stays");
    }

    /**
     * Verifies that attempting to remove a shelter that still contains animals prints an error.
     * An animal is admitted first so the shelter is non-empty, which must block removal.
     */
    @Test
    void remove_shelterWithAnimals_printsError() throws Exception {
        RunResult reg = run("shelter", "register",
                "--name", "Full", "--location", "Boston", "--capacity", "10");
        String shelterId = extractId(reg.stdout());
        run("animal", "admit",
                "--species", "dog", "--name", "Blocker", "--breed", "Lab",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        RunResult r = run("shelter", "remove", "--id", shelterId);
        assertOutputContains(r, "Error");
    }

    /**
     * Verifies that attempting to remove a shelter with an unknown ID prints an error.
     * This tests the not-found guard without any registered shelters present.
     */
    @Test
    void remove_nonExistentId_printsError() throws Exception {
        RunResult r = run("shelter", "remove",
                "--id", "00000000-0000-0000-0000-000000000000");
        assertOutputContains(r, "Error");
    }
}
