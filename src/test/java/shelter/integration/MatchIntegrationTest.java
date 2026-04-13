package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter match} command group.
 * Covers UC-04.1 (match animals for an adopter) and UC-04.2 (match adopters for an animal),
 * including empty result sets, multiple scored entities, and the already-adopted error case.
 */
class MatchIntegrationTest extends CliIntegrationTest {

    /** Registers a shelter with ample capacity and returns its ID. */
    private String registerShelter() throws Exception {
        RunResult r = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "20");
        return extractId(r.stdout());
    }

    /** Admits a dog with the given name to the given shelter and returns its ID. */
    private String admitDog(String name, String shelterId) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", name, "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    /** Registers a DOG-preferring adopter with the given name and returns its ID. */
    private String registerAdopter(String name) throws Exception {
        RunResult r = run("adopter", "register",
                "--name", name, "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY", "--species", "DOG");
        return extractId(r.stdout());
    }

    // -------------------------------------------------------------------------
    // UC-04.1: Match Animals for an Adopter
    // -------------------------------------------------------------------------

    /**
     * Verifies that matching animals for an adopter when two available animals are in the shelter
     * returns scored results containing both animal names, confirming the ranker sees all candidates.
     */
    @Test
    void matchAnimal_multipleAnimals_returnsRankedList() throws Exception {
        String shelterId = registerShelter();
        admitDog("Rex", shelterId);
        admitDog("Max", shelterId);
        String adopterId = registerAdopter("Alice");
        RunResult r = run("match", "animal",
                "--adopter", adopterId, "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Rex");
        assertOutputContains(r, "Max");
    }

    /**
     * Verifies that matching animals for an adopter when the shelter is empty prints an
     * empty-result message rather than crashing. Two adopters are registered so the
     * empty state is confirmed against an existing adopter.
     */
    @Test
    void matchAnimal_noAnimals_printsEmptyMessage() throws Exception {
        String shelterId = registerShelter();
        registerAdopter("Alice");
        String adopterId = registerAdopter("Bob");
        RunResult r = run("match", "animal",
                "--adopter", adopterId, "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "No available animals");
    }

    /**
     * Regression: verifies that an adopter registered without {@code --min-age} or
     * {@code --max-age} does not apply a default max-age of 20, which would incorrectly
     * exclude older animals. Both a young and a senior dog must appear in the results.
     */
    @Test
    void matchAnimal_nullAgePrefs_doesNotExcludeOlderAnimals() throws Exception {
        String shelterId = registerShelter();
        admitDog("Young", shelterId);
        run("animal", "admit",
                "--species", "dog", "--name", "Senior", "--breed", "Lab",
                "--age", "10", "--activity", "HIGH", "--shelter", shelterId);
        RunResult adopterResult = run("adopter", "register",
                "--name", "NoAgePrefs", "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        String adopterId = extractId(adopterResult.stdout());
        RunResult r = run("match", "animal",
                "--adopter", adopterId, "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Young");
        assertOutputContains(r, "Senior");
    }

    // -------------------------------------------------------------------------
    // UC-04.2: Match Adopters for an Animal
    // -------------------------------------------------------------------------

    /**
     * Verifies that matching adopters for an available animal when two adopters are registered
     * returns scored results containing both adopter names.
     */
    @Test
    void matchAdopter_multipleAdopters_returnsRankedList() throws Exception {
        String shelterId = registerShelter();
        String animalId = admitDog("Rex", shelterId);
        registerAdopter("Alice");
        registerAdopter("Bob");
        RunResult r = run("match", "adopter", "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "Alice");
        assertOutputContains(r, "Bob");
    }

    /**
     * Verifies that matching adopters when no adopters are registered returns an empty-result
     * message rather than crashing. Two animals are in the shelter so the empty state is
     * confirmed against an existing animal.
     */
    @Test
    void matchAdopter_noAdopters_printsEmptyMessage() throws Exception {
        String shelterId = registerShelter();
        admitDog("Rex", shelterId);
        String animalId = admitDog("Max", shelterId);
        RunResult r = run("match", "adopter", "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "No adopters registered.");
    }

    /**
     * Verifies that attempting to match adopters for an already-adopted animal prints an error.
     * A first adopter approves the animal, then a second adopter attempts a match.
     */
    @Test
    void matchAdopter_adoptedAnimal_printsError() throws Exception {
        String shelterId = registerShelter();
        String animalId = admitDog("Rex", shelterId);
        admitDog("Free", shelterId);
        String adopterId = registerAdopter("Alice");
        registerAdopter("Bob");
        RunResult submitResult = run("adopt", "submit",
                "--adopter", adopterId, "--animal", animalId);
        String requestId = extractId(submitResult.stdout());
        run("adopt", "approve", "--request", requestId);
        RunResult r = run("match", "adopter", "--animal", animalId);
        assertOutputContains(r, "Error");
    }
}
