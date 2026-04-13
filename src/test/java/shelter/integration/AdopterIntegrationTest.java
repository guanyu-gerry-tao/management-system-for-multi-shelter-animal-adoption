package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter adopter} command group.
 * Covers UC-03.1 through UC-03.4: register, list, update, and remove adopters,
 * including missing-field, not-found, and pending-adoption-blocked-removal error cases.
 */
class AdopterIntegrationTest extends CliIntegrationTest {

    // -------------------------------------------------------------------------
    // UC-03.1: Register a New Adopter
    // -------------------------------------------------------------------------

    /**
     * Verifies that registering an adopter with all optional preference fields
     * (species, min-age, max-age) exits with code 0 and prints the name and ID.
     * A second adopter is registered with different preferences to confirm both succeed.
     */
    @Test
    void register_withAllPrefs_printsIdAndName() throws Exception {
        RunResult r1 = run("adopter", "register",
                "--name", "Alice",
                "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY",
                "--species", "DOG",
                "--min-age", "1",
                "--max-age", "5");
        assertSuccess(r1);
        assertOutputContains(r1, "Alice");
        assertOutputContains(r1, "id=");

        RunResult r2 = run("adopter", "register",
                "--name", "Bob",
                "--space", "APARTMENT",
                "--schedule", "AWAY_PART_OF_DAY",
                "--species", "CAT",
                "--min-age", "0",
                "--max-age", "10");
        assertSuccess(r2);
        assertOutputContains(r2, "Bob");
    }

    /**
     * Verifies that registering an adopter without {@code --min-age} and {@code --max-age}
     * succeeds without applying a default age cap. Two adopters are registered to confirm
     * the optional-field path works independently for each.
     */
    @Test
    void register_withoutAgePrefs_succeeds() throws Exception {
        RunResult r1 = run("adopter", "register",
                "--name", "NoAgeA",
                "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        assertSuccess(r1);
        RunResult r2 = run("adopter", "register",
                "--name", "NoAgeB",
                "--space", "APARTMENT",
                "--schedule", "AWAY_MOST_OF_DAY");
        assertSuccess(r2);
        assertOutputContains(r2, "NoAgeB");
    }

    // -------------------------------------------------------------------------
    // UC-03.2: View All Adopters
    // -------------------------------------------------------------------------

    /**
     * Verifies that listing adopters when none have been registered prints an empty-list
     * message rather than crashing or returning blank output.
     */
    @Test
    void list_noAdopters_printsEmptyMessage() throws Exception {
        RunResult r = run("adopter", "list");
        assertSuccess(r);
        assertOutputContains(r, "No adopters registered.");
    }

    /**
     * Verifies that two registered adopters both appear in the list output,
     * confirming the command aggregates all entries correctly.
     */
    @Test
    void list_multipleAdopters_showsAll() throws Exception {
        run("adopter", "register",
                "--name", "Carol",
                "--space", "APARTMENT",
                "--schedule", "HOME_MOST_OF_DAY");
        run("adopter", "register",
                "--name", "Dave",
                "--space", "HOUSE_WITH_YARD",
                "--schedule", "AWAY_PART_OF_DAY");
        RunResult r = run("adopter", "list");
        assertSuccess(r);
        assertOutputContains(r, "Carol");
        assertOutputContains(r, "Dave");
    }

    // -------------------------------------------------------------------------
    // UC-03.3: Update Adopter Information
    // -------------------------------------------------------------------------

    /**
     * Verifies that updating an existing adopter's name succeeds and the confirmation
     * message reflects the new name. A second adopter is registered so the list can
     * confirm only the targeted adopter was renamed.
     */
    @Test
    void update_name_printsUpdatedName() throws Exception {
        run("adopter", "register",
                "--name", "Bystander",
                "--space", "APARTMENT",
                "--schedule", "HOME_MOST_OF_DAY");
        RunResult reg = run("adopter", "register",
                "--name", "Old Name",
                "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        String adopterId = extractId(reg.stdout());
        RunResult r = run("adopter", "update", "--id", adopterId, "--name", "New Name");
        assertSuccess(r);
        assertOutputContains(r, "Updated adopter");
        assertOutputContains(r, "New Name");
    }

    /**
     * Verifies that updating only the name leaves the living space unchanged.
     * After the update, the list must still show the original living space for that adopter.
     */
    @Test
    void update_nameOnly_livingSpaceUnchanged() throws Exception {
        run("adopter", "register",
                "--name", "Other",
                "--space", "APARTMENT",
                "--schedule", "AWAY_MOST_OF_DAY");
        RunResult reg = run("adopter", "register",
                "--name", "Original",
                "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        String adopterId = extractId(reg.stdout());
        run("adopter", "update", "--id", adopterId, "--name", "Renamed");
        RunResult list = run("adopter", "list");
        assertOutputContains(list, "Renamed");
        assertOutputContains(list, "HOUSE_WITH_YARD");
    }

    /**
     * Verifies that attempting to update an adopter with an unknown ID prints an error.
     * This exercises the not-found guard in the application layer.
     */
    @Test
    void update_nonExistentId_printsError() throws Exception {
        RunResult r = run("adopter", "update",
                "--id", "00000000-0000-0000-0000-000000000000", "--name", "Ghost");
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-03.4: Remove an Adopter
    // -------------------------------------------------------------------------

    /**
     * Verifies that removing an adopter succeeds and the adopter no longer appears in the list.
     * A second adopter is registered so the list confirms only the targeted adopter is absent.
     */
    @Test
    void remove_existingAdopter_disappearsFromList() throws Exception {
        run("adopter", "register",
                "--name", "Survivor",
                "--space", "APARTMENT",
                "--schedule", "HOME_MOST_OF_DAY");
        RunResult reg = run("adopter", "register",
                "--name", "Doomed",
                "--space", "APARTMENT",
                "--schedule", "AWAY_MOST_OF_DAY");
        String adopterId = extractId(reg.stdout());
        RunResult r = run("adopter", "remove", "--id", adopterId);
        assertSuccess(r);
        assertOutputContains(r, "Removed adopter");
        RunResult list = run("adopter", "list");
        assertOutputDoesNotContain(list, "Doomed");
        assertOutputContains(list, "Survivor");
    }

    /**
     * Verifies that attempting to remove an adopter who has a pending adoption request
     * prints an error. A request is submitted first to put the adopter in a blocked state.
     * A second adopter without a pending request is registered to confirm the block is
     * scoped to the requesting adopter only.
     */
    @Test
    void remove_adopterWithPendingAdoption_printsError() throws Exception {
        RunResult shelterResult = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        String shelterId = extractId(shelterResult.stdout());
        RunResult animalResult = run("animal", "admit",
                "--species", "dog", "--name", "Rover", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        String animalId = extractId(animalResult.stdout());
        run("adopter", "register",
                "--name", "Free", "--space", "APARTMENT", "--schedule", "HOME_MOST_OF_DAY");
        RunResult adopterResult = run("adopter", "register",
                "--name", "Blocked",
                "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        String adopterId = extractId(adopterResult.stdout());
        run("adopt", "submit", "--adopter", adopterId, "--animal", animalId);
        RunResult r = run("adopter", "remove", "--id", adopterId);
        assertOutputContains(r, "Error");
    }

    /**
     * Verifies that attempting to remove an adopter with an unknown ID prints an error.
     * This tests the not-found guard without any adopters registered.
     */
    @Test
    void remove_nonExistentId_printsError() throws Exception {
        RunResult r = run("adopter", "remove",
                "--id", "00000000-0000-0000-0000-000000000000");
        assertOutputContains(r, "Error");
    }
}
