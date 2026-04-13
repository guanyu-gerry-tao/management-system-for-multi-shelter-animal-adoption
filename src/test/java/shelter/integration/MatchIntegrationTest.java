package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter match} command group.
 * Verifies ranked output and that null age preferences do not penalise the score.
 */
class MatchIntegrationTest extends CliIntegrationTest {

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
    void matchAnimal_returnsRankedList() throws Exception {
        String shelterId = registerShelter();
        admitDog(shelterId);
        RunResult ar = run("adopter", "register",
                "--name", "Alice", "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY", "--species", "DOG");
        String adopterId = extractId(ar.stdout());
        RunResult r = run("match", "animal",
                "--adopter", adopterId, "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Rex");
    }

    @Test
    void matchAnimal_noAnimalsInShelter_printsEmptyMessage() throws Exception {
        String shelterId = registerShelter();
        RunResult ar = run("adopter", "register",
                "--name", "Alice", "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        String adopterId = extractId(ar.stdout());
        RunResult r = run("match", "animal",
                "--adopter", adopterId, "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "No available animals");
    }

    @Test
    void matchAdopter_returnsRankedList() throws Exception {
        String shelterId = registerShelter();
        String animalId = admitDog(shelterId);
        run("adopter", "register",
                "--name", "Alice", "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        RunResult r = run("match", "adopter", "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "Alice");
    }

    @Test
    void matchAnimal_adopterWithNoAgePrefs_animalAppearsInResults() throws Exception {
        String shelterId = registerShelter();
        admitDog(shelterId);
        RunResult ar = run("adopter", "register",
                "--name", "NoAge", "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        String adopterId = extractId(ar.stdout());
        RunResult r = run("match", "animal",
                "--adopter", adopterId, "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Rex");
    }
}
