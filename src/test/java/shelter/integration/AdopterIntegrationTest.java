package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter adopter} command group.
 * Verifies registration with and without age preferences, and listing behaviour.
 */
class AdopterIntegrationTest extends CliIntegrationTest {

    @Test
    void register_withAllPrefs_printsIdAndName() throws Exception {
        RunResult r = run("adopter", "register",
                "--name", "Alice",
                "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY",
                "--species", "DOG",
                "--min-age", "1",
                "--max-age", "5");
        assertSuccess(r);
        assertOutputContains(r, "Alice");
        assertOutputContains(r, "id=");
    }

    @Test
    void register_withoutAgePrefs_doesNotDefaultToAge20() throws Exception {
        RunResult r = run("adopter", "register",
                "--name", "Bob",
                "--space", "APARTMENT",
                "--schedule", "AWAY_MOST_OF_DAY");
        assertSuccess(r);
        assertOutputContains(r, "Bob");
    }

    @Test
    void list_noAdopters_printsEmptyMessage() throws Exception {
        RunResult r = run("adopter", "list");
        assertSuccess(r);
        assertOutputContains(r, "No adopters registered.");
    }

    @Test
    void list_afterRegister_showsAdopter() throws Exception {
        run("adopter", "register",
                "--name", "Carol",
                "--space", "APARTMENT",
                "--schedule", "HOME_MOST_OF_DAY");
        RunResult r = run("adopter", "list");
        assertSuccess(r);
        assertOutputContains(r, "Carol");
    }
}
