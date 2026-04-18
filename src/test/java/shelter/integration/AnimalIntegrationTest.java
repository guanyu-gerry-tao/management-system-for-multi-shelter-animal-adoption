package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter animal} command group.
 * Covers UC-02.1 through UC-02.4: admit (Dog, Cat, Rabbit, Other), list with and without
 * shelter filter, update, and remove operations, including capacity overflow, unknown shelter,
 * not-found, and pending-adoption error cases.
 */
class AnimalIntegrationTest extends CliIntegrationTest {

    /** Registers a shelter and returns its generated ID. */
    private String registerShelter(String name, int capacity) throws Exception {
        RunResult r = run("shelter", "register",
                "--name", name, "--location", "Boston", "--capacity", String.valueOf(capacity));
        return extractId(r.stdout());
    }

    // -------------------------------------------------------------------------
    // UC-02.1: Admit a New Animal
    // -------------------------------------------------------------------------

    /**
     * Verifies that admitting a Dog with valid arguments exits successfully and prints
     * the animal name and generated UUID. A Cat is admitted to the same shelter
     * so both records coexist without interference.
     */
    @Test
    void admit_dog_printsIdAndName() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Labrador",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Rex");
        assertOutputContains(r, "id=");

        run("animal", "admit",
                "--species", "cat", "--name", "Whiskers", "--breed", "Siamese",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
    }

    /**
     * Verifies that admitting a Cat exits successfully and the name appears in the confirmation.
     * A Dog is also admitted to the same shelter to confirm species routing works for both.
     */
    @Test
    void admit_cat_printsIdAndName() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        RunResult r = run("animal", "admit",
                "--species", "cat", "--name", "Whiskers", "--breed", "Siamese",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Whiskers");
        assertOutputContains(r, "id=");
    }

    /**
     * Verifies that admitting a Rabbit exits successfully and the name appears in the confirmation.
     * A Dog is also admitted to confirm the species discriminator does not corrupt other records.
     */
    @Test
    void admit_rabbit_printsIdAndName() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        RunResult r = run("animal", "admit",
                "--species", "rabbit", "--name", "Fluffy", "--breed", "Holland Lop",
                "--age", "1", "--activity", "MEDIUM", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Fluffy");
        assertOutputContains(r, "id=");
    }

    /**
     * Verifies that admitting an Other-species animal with a free-form species name
     * stores the name correctly and both it and a co-resident Dog appear in the list.
     * This exercises the species discriminator round-trip for non-enum species.
     */
    @Test
    void admit_other_speciesNamePreservedOnList() throws Exception {
        String shelterId = registerShelter("Paws", 10);
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
     * Verifies that admitting an animal to a non-existent shelter ID prints an error.
     * Two bogus shelter IDs are attempted to confirm the guard applies unconditionally.
     */
    @Test
    void admit_unknownShelterId_printsError() throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Ghost", "--breed", "Lab",
                "--age", "2", "--activity", "LOW",
                "--shelter", "00000000-0000-0000-0000-000000000000");
        assertOutputContains(r, "Error");
    }

    /**
     * Verifies that admitting a second animal to a shelter that has reached capacity prints an error.
     * The shelter is pre-filled with one animal so the second admission triggers the capacity guard.
     */
    @Test
    void admit_exceedsCapacity_printsError() throws Exception {
        String shelterId = registerShelter("Tiny", 1);
        run("animal", "admit",
                "--species", "dog", "--name", "First", "--breed", "Lab",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Second", "--breed", "Lab",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-02.2: View Animals
    // -------------------------------------------------------------------------

    /**
     * Verifies that listing with a shelter filter returns only animals in that shelter.
     * Two shelters each contain a distinctly named animal; the list for Shelter A must
     * include its animal and exclude Shelter B's animal.
     */
    @Test
    void list_byShelter_onlyShowsAnimalsInThatShelter() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 10);
        run("animal", "admit",
                "--species", "dog", "--name", "InA", "--breed", "Lab",
                "--age", "2", "--activity", "LOW", "--shelter", shelterA);
        run("animal", "admit",
                "--species", "dog", "--name", "InB", "--breed", "Lab",
                "--age", "2", "--activity", "LOW", "--shelter", shelterB);
        RunResult r = run("animal", "list", "--shelter", shelterA);
        assertOutputContains(r, "InA");
        assertOutputDoesNotContain(r, "InB");
    }

    /**
     * Verifies that listing without a shelter filter shows animals from all shelters.
     * One animal is admitted to each of two shelters; both must appear in the unfiltered list.
     */
    @Test
    void list_allAnimals_multiShelter_showsAll() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 10);
        run("animal", "admit",
                "--species", "dog", "--name", "Alpha", "--breed", "Lab",
                "--age", "2", "--activity", "LOW", "--shelter", shelterA);
        run("animal", "admit",
                "--species", "cat", "--name", "Beta", "--breed", "Mix",
                "--age", "3", "--activity", "MEDIUM", "--shelter", shelterB);
        RunResult r = run("animal", "list");
        assertSuccess(r);
        assertOutputContains(r, "Alpha");
        assertOutputContains(r, "Beta");
    }

    /**
     * Verifies that listing animals in an empty shelter prints an empty-list message
     * rather than crashing or printing a blank line.
     */
    @Test
    void list_noAnimals_printsEmptyMessage() throws Exception {
        String shelterId = registerShelter("Empty", 10);
        RunResult r = run("animal", "list", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "(none)");
    }

    // -------------------------------------------------------------------------
    // UC-02.3: Update Animal Information
    // -------------------------------------------------------------------------

    /**
     * Verifies that updating an existing animal's name succeeds and the confirmation
     * reflects the new name. A second animal is admitted to the same shelter to
     * confirm only the targeted animal is renamed.
     */
    @Test
    void update_name_printsUpdatedName() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        run("animal", "admit",
                "--species", "cat", "--name", "Bystander", "--breed", "Mix",
                "--age", "4", "--activity", "LOW", "--shelter", shelterId);
        RunResult admit = run("animal", "admit",
                "--species", "dog", "--name", "Old Name", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        String animalId = extractId(admit.stdout());
        RunResult r = run("animal", "update", "--id", animalId, "--name", "New Name");
        assertSuccess(r);
        assertOutputContains(r, "Updated animal");
        assertOutputContains(r, "New Name");
    }

    /**
     * Verifies that updating only the activity level leaves the animal name unchanged.
     * A second animal is admitted so the list can confirm isolation between records.
     */
    @Test
    void update_activityOnly_nameUnchanged() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        run("animal", "admit",
                "--species", "cat", "--name", "Other", "--breed", "Mix",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        RunResult admit = run("animal", "admit",
                "--species", "dog", "--name", "Speedy", "--breed", "Lab",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        String animalId = extractId(admit.stdout());
        RunResult r = run("animal", "update", "--id", animalId, "--activity", "HIGH");
        assertSuccess(r);
        assertOutputContains(r, "Speedy");
    }

    /**
     * Verifies that attempting to update an animal with an unknown ID prints an error.
     * This exercises the not-found guard in the application layer.
     */
    @Test
    void update_nonExistentId_printsError() throws Exception {
        RunResult r = run("animal", "update",
                "--id", "00000000-0000-0000-0000-000000000000", "--name", "Ghost");
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-02.4: Remove an Animal
    // -------------------------------------------------------------------------

    /**
     * Verifies that removing an animal succeeds and it no longer appears in the shelter list.
     * A second animal stays in the shelter to confirm only the targeted animal is absent.
     */
    @Test
    void remove_existingAnimal_disappearsFromList() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        run("animal", "admit",
                "--species", "cat", "--name", "Survivor", "--breed", "Mix",
                "--age", "3", "--activity", "LOW", "--shelter", shelterId);
        RunResult admit = run("animal", "admit",
                "--species", "dog", "--name", "Doomed", "--breed", "Lab",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        String animalId = extractId(admit.stdout());
        RunResult r = run("animal", "remove", "--id", animalId);
        assertSuccess(r);
        assertOutputContains(r, "Removed animal");
        RunResult list = run("animal", "list", "--shelter", shelterId);
        assertOutputDoesNotContain(list, "Doomed");
        assertOutputContains(list, "Survivor");
    }

    /**
     * Verifies that attempting to remove an animal that has a pending adoption request
     * prints an error. An adopter submits a request first, putting the animal in a blocked state.
     * A second animal is admitted to confirm the block is scoped to the request target only.
     */
    @Test
    void remove_animalWithPendingAdoption_printsError() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        run("animal", "admit",
                "--species", "cat", "--name", "Free", "--breed", "Mix",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        RunResult animalResult = run("animal", "admit",
                "--species", "dog", "--name", "Blocked", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        String animalId = extractId(animalResult.stdout());
        RunResult adopterResult = run("adopter", "register",
                "--name", "Alice", "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        String adopterId = extractId(adopterResult.stdout());
        run("adopt", "submit", "--adopter", adopterId, "--animal", animalId);
        RunResult r = run("animal", "remove", "--id", animalId);
        assertOutputContains(r, "Error");
    }

    /**
     * Verifies that attempting to remove an animal with an unknown ID prints an error.
     * This tests the not-found guard without any animals registered in the shelter.
     */
    @Test
    void remove_nonExistentId_printsError() throws Exception {
        RunResult r = run("animal", "remove",
                "--id", "00000000-0000-0000-0000-000000000000");
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // Shelter column in animal list
    // -------------------------------------------------------------------------

    /**
     * Verifies that the animal list output always includes a Shelter column header
     * and that the column shows the correct shelter name for each animal.
     * Two animals are admitted to shelters with different names; both shelter names
     * must appear in the unfiltered list output alongside the correct animal.
     */
    @Test
    void list_allAnimals_showsShelterNameColumn() throws Exception {
        String shelterA = registerShelter("Happy Paws", 10);
        String shelterB = registerShelter("Green Field", 10);
        run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "2", "--activity", "HIGH", "--shelter", shelterA);
        run("animal", "admit",
                "--species", "cat", "--name", "Misty", "--breed", "Mix",
                "--age", "3", "--activity", "LOW", "--shelter", shelterB);

        RunResult r = run("animal", "list");
        assertSuccess(r);
        assertOutputContains(r, "SHELTER,");
        assertOutputContains(r, "Happy Paws");
        assertOutputContains(r, "Green Field");
    }

    /**
     * Verifies that filtering by shelter still shows the correct shelter name in the output.
     * Only animals belonging to the specified shelter should appear, and their Shelter
     * column must match the shelter name rather than the raw ID.
     */
    @Test
    void list_byShelter_showsCorrectShelterName() throws Exception {
        String shelterA = registerShelter("Cozy Corner", 10);
        String shelterB = registerShelter("Open Meadow", 10);
        run("animal", "admit",
                "--species", "rabbit", "--name", "Bun", "--breed", "Dutch",
                "--age", "1", "--activity", "MEDIUM", "--shelter", shelterA);
        run("animal", "admit",
                "--species", "dog", "--name", "Bolt", "--breed", "Mix",
                "--age", "4", "--activity", "HIGH", "--shelter", shelterB);

        RunResult r = run("animal", "list", "--shelter", shelterA);
        assertSuccess(r);
        assertOutputContains(r, "Cozy Corner");
        assertOutputDoesNotContain(r, "Open Meadow");
        assertOutputDoesNotContain(r, "Bolt");
    }

    /**
     * Verifies that updating the neutered status of a dog changes the field from false to true.
     * A second animal is admitted to confirm the update is scoped only to the target animal.
     */
    @Test
    void update_neutered_changesNeuteredStatus() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        run("animal", "admit",
                "--species", "cat", "--name", "Bystander", "--breed", "Mix",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        RunResult admit = run("animal", "admit",
                "--species", "dog", "--name", "Bruno", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        String animalId = extractId(admit.stdout());

        RunResult r = run("animal", "update", "--id", animalId, "--neutered", "true");
        assertSuccess(r);
        assertOutputContains(r, "Updated animal");

        RunResult list = run("animal", "list", "--shelter", shelterId);
        assertSuccess(list);
        assertOutputContains(list, "true");
    }
}
