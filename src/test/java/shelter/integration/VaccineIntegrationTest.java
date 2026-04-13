package shelter.integration;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter vaccine} command group.
 * Covers vaccine type CRUD, recording vaccinations, and overdue checks.
 */
class VaccineIntegrationTest extends CliIntegrationTest {

    private String registerShelter() throws Exception {
        RunResult r = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        return extractId(r.stdout());
    }

    private String admitDog(String shelterId) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    @Test
    void vaccineType_addAndList_showsType() throws Exception {
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        RunResult r = run("vaccine", "type", "list");
        assertSuccess(r);
        assertOutputContains(r, "Rabies");
    }

    @Test
    void record_validVaccination_printsConfirmation() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        RunResult r = run("vaccine", "record",
                "--animal", animalId,
                "--type", "Rabies",
                "--date", LocalDate.now().toString());
        assertSuccess(r);
        assertOutputContains(r, "Recorded vaccination");
    }

    @Test
    void overdue_recentVaccination_notOverdue() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        run("vaccine", "record",
                "--animal", animalId,
                "--type", "Rabies",
                "--date", LocalDate.now().toString());
        RunResult r = run("vaccine", "overdue", "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "All vaccinations are current");
    }

    @Test
    void overdue_expiredVaccination_printsOverdueEntry() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        String pastDate = LocalDate.now().minusYears(2).toString();
        run("vaccine", "record",
                "--animal", animalId, "--type", "Rabies", "--date", pastDate);
        RunResult r = run("vaccine", "overdue", "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "Rabies");
    }
}
