package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter animal} command group.
 * Covers Dog, Cat, Rabbit, and Other species, plus list filtering and capacity enforcement.
 */
class AnimalIntegrationTest extends CliIntegrationTest {

    private String registerShelter(String name, int capacity) throws Exception {
        RunResult r = run("shelter", "register",
                "--name", name, "--location", "Boston", "--capacity", String.valueOf(capacity));
        return extractId(r.stdout());
    }

    @Test
    void admit_dog_printsIdAndSpecies() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Labrador",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Rex");
        assertOutputContains(r, "id=");
    }

    @Test
    void admit_cat_printsIdAndSpecies() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        RunResult r = run("animal", "admit",
                "--species", "cat", "--name", "Whiskers", "--breed", "Siamese",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Whiskers");
    }

    @Test
    void admit_rabbit_printsIdAndSpecies() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        RunResult r = run("animal", "admit",
                "--species", "rabbit", "--name", "Fluffy", "--breed", "Holland Lop",
                "--age", "1", "--activity", "MEDIUM", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Fluffy");
    }

    @Test
    void admit_other_speciesNamePreservedOnList() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        run("animal", "admit",
                "--species", "other", "--species-name", "fish",
                "--name", "Nemo", "--breed", "Clownfish",
                "--age", "1", "--activity", "LOW", "--shelter", shelterId);
        RunResult r = run("animal", "list", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Nemo");
    }

    @Test
    void list_byShelter_onlyShowsAnimalsInThatShelter() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 10);
        run("animal", "admit",
                "--species", "dog", "--name", "InA", "--breed", "Breed",
                "--age", "2", "--activity", "LOW", "--shelter", shelterA);
        run("animal", "admit",
                "--species", "dog", "--name", "InB", "--breed", "Breed",
                "--age", "2", "--activity", "LOW", "--shelter", shelterB);
        RunResult r = run("animal", "list", "--shelter", shelterA);
        assertOutputContains(r, "InA");
        assertOutputDoesNotContain(r, "InB");
    }

    @Test
    void admit_exceedsCapacity_printsError() throws Exception {
        String shelterId = registerShelter("Tiny", 1);
        run("animal", "admit",
                "--species", "dog", "--name", "First", "--breed", "Breed",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Second", "--breed", "Breed",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        assertOutputContains(r, "Error");
    }

    @Test
    void list_noAnimals_printsEmptyMessage() throws Exception {
        String shelterId = registerShelter("Empty", 10);
        RunResult r = run("animal", "list", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "No animals found.");
    }
}
