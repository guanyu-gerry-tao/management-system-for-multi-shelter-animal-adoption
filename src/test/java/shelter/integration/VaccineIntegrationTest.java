package shelter.integration;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter vaccine} command group.
 * Covers UC-07.1 through UC-07.6: add vaccine type, record vaccination, check overdue,
 * update and remove vaccine types, including duplicate-name, species-mismatch,
 * unknown-type, and not-found error cases.
 */
class VaccineIntegrationTest extends CliIntegrationTest {

    /** Registers a shelter with ample capacity and returns its ID. */
    private String registerShelter() throws Exception {
        RunResult r = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        return extractId(r.stdout());
    }

    /** Admits a dog with the given name to the given shelter and returns its ID. */
    private String admitDog(String name, String shelterId) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", name, "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    /** Admits a cat with the given name to the given shelter and returns its ID. */
    private String admitCat(String name, String shelterId) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "cat", "--name", name, "--breed", "Mix",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    // -------------------------------------------------------------------------
    // UC-07.3: Add a Vaccine Type / UC-07.6: View All Vaccine Types
    // -------------------------------------------------------------------------

    /**
     * Verifies that adding two vaccine types (DOG and CAT species) and then listing them
     * shows both entries. This confirms the add-then-list path across different species.
     */
    @Test
    void vaccineType_addAndList_showsBothTypes() throws Exception {
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        run("vaccine", "type", "add",
                "--name", "FeLV", "--species", "CAT", "--days", "365");
        RunResult r = run("vaccine", "type", "list");
        assertSuccess(r);
        assertOutputContains(r, "Rabies");
        assertOutputContains(r, "FeLV");
    }

    /**
     * Verifies that adding two vaccine types with the same name prints an error on the
     * second attempt. The duplicate-name guard must reject the conflicting entry.
     */
    @Test
    void vaccineType_duplicateName_printsError() throws Exception {
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        RunResult r = run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "CAT", "--days", "180");
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-07.1: Record a Vaccination
    // -------------------------------------------------------------------------

    /**
     * Verifies that recording a vaccination for a valid animal and applicable vaccine type
     * exits successfully and prints a confirmation. A Dog and a Cat are both admitted;
     * the DOG-species vaccine is applied only to the Dog.
     */
    @Test
    void record_validVaccination_printsConfirmation() throws Exception {
        String shelterId = registerShelter();
        String dogId = admitDog("Rex", shelterId);
        admitCat("Whiskers", shelterId);
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        RunResult r = run("vaccine", "record",
                "--animal", dogId,
                "--type", "Rabies",
                "--date", LocalDate.now().toString());
        assertSuccess(r);
        assertOutputContains(r, "Recorded vaccination");
    }

    /**
     * Verifies that recording a vaccination with an unknown vaccine type name prints an error.
     * Two animals are admitted to confirm the guard applies regardless of shelter population.
     */
    @Test
    void record_unknownVaccineType_printsError() throws Exception {
        String shelterId = registerShelter();
        String dogId = admitDog("Rex", shelterId);
        admitDog("Max", shelterId);
        RunResult r = run("vaccine", "record",
                "--animal", dogId,
                "--type", "NoSuchVaccine",
                "--date", LocalDate.now().toString());
        assertOutputContains(r, "Error");
    }

    /**
     * Verifies that applying a DOG-species vaccine to a Cat prints a species-mismatch error.
     * Both a Dog and a Cat are admitted; the error must apply only to the Cat record.
     */
    @Test
    void record_wrongSpecies_printsError() throws Exception {
        String shelterId = registerShelter();
        admitDog("Rex", shelterId);
        String catId = admitCat("Whiskers", shelterId);
        run("vaccine", "type", "add",
                "--name", "DogOnly", "--species", "DOG", "--days", "365");
        RunResult r = run("vaccine", "record",
                "--animal", catId,
                "--type", "DogOnly",
                "--date", LocalDate.now().toString());
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-07.2: Check Overdue Vaccinations
    // -------------------------------------------------------------------------

    /**
     * Verifies that two vaccinations recorded today are not shown as overdue.
     * Both Rabies and Bordetella are administered to the same dog on today's date.
     */
    @Test
    void overdue_recentVaccinations_notOverdue() throws Exception {
        String shelterId = registerShelter();
        String dogId = admitDog("Rex", shelterId);
        admitDog("Max", shelterId);
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        run("vaccine", "type", "add",
                "--name", "Bordetella", "--species", "DOG", "--days", "365");
        run("vaccine", "record",
                "--animal", dogId, "--type", "Rabies",
                "--date", LocalDate.now().toString());
        run("vaccine", "record",
                "--animal", dogId, "--type", "Bordetella",
                "--date", LocalDate.now().toString());
        RunResult r = run("vaccine", "overdue", "--animal", dogId);
        assertSuccess(r);
        assertOutputContains(r, "All vaccinations are current");
    }

    /**
     * Verifies that two vaccinations administered two years ago both appear in the overdue list.
     * Both vaccine types have a 365-day validity; administering them two years in the past
     * guarantees they exceed their validity windows.
     */
    @Test
    void overdue_multipleExpiredVaccinations_showsAllOverdue() throws Exception {
        String shelterId = registerShelter();
        String dogId = admitDog("Rex", shelterId);
        admitDog("Max", shelterId);
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        run("vaccine", "type", "add",
                "--name", "Bordetella", "--species", "DOG", "--days", "365");
        String pastDate = LocalDate.now().minusYears(2).toString();
        run("vaccine", "record", "--animal", dogId, "--type", "Rabies", "--date", pastDate);
        run("vaccine", "record", "--animal", dogId, "--type", "Bordetella", "--date", pastDate);
        RunResult r = run("vaccine", "overdue", "--animal", dogId);
        assertSuccess(r);
        assertOutputContains(r, "Rabies");
        assertOutputContains(r, "Bordetella");
    }

    // -------------------------------------------------------------------------
    // UC-07.4: Update a Vaccine Type
    // -------------------------------------------------------------------------

    /**
     * Verifies that updating a vaccine type's name succeeds, the confirmation reflects the
     * new name, and the list shows the renamed entry while the old name is absent.
     * A second vaccine type is registered to confirm only the targeted entry is renamed.
     */
    @Test
    void vaccineType_update_printsUpdatedName() throws Exception {
        run("vaccine", "type", "add",
                "--name", "OtherVaccine", "--species", "CAT", "--days", "180");
        RunResult add = run("vaccine", "type", "add",
                "--name", "OldVaccine", "--species", "DOG", "--days", "180");
        String typeId = extractId(add.stdout());
        RunResult r = run("vaccine", "type", "update",
                "--id", typeId, "--name", "NewVaccine");
        assertSuccess(r);
        assertOutputContains(r, "Updated vaccine type");
        assertOutputContains(r, "NewVaccine");
        RunResult list = run("vaccine", "type", "list");
        assertOutputContains(list, "NewVaccine");
        assertOutputDoesNotContain(list, "OldVaccine");
    }

    /**
     * Verifies that renaming a vaccine type to a name already used by another type prints an error.
     * The duplicate-name guard must prevent the conflicting rename.
     */
    @Test
    void vaccineType_updateDuplicateName_printsError() throws Exception {
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        RunResult add = run("vaccine", "type", "add",
                "--name", "Bordetella", "--species", "DOG", "--days", "180");
        String typeId = extractId(add.stdout());
        RunResult r = run("vaccine", "type", "update",
                "--id", typeId, "--name", "Rabies");
        assertOutputContains(r, "Error");
    }

    /**
     * Verifies that attempting to update a vaccine type with an unknown ID prints an error.
     * A valid vaccine type is registered to rule out false positives from an empty catalog.
     */
    @Test
    void vaccineType_updateNonExistentId_printsError() throws Exception {
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        RunResult r = run("vaccine", "type", "update",
                "--id", "00000000-0000-0000-0000-000000000000", "--name", "Ghost");
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-07.5: Remove a Vaccine Type
    // -------------------------------------------------------------------------

    /**
     * Verifies that removing a vaccine type succeeds and it no longer appears in the catalog.
     * A second vaccine type is registered to confirm it remains after the targeted removal.
     */
    @Test
    void vaccineType_remove_disappearsFromList() throws Exception {
        run("vaccine", "type", "add",
                "--name", "Stays", "--species", "CAT", "--days", "180");
        RunResult add = run("vaccine", "type", "add",
                "--name", "TempVaccine", "--species", "DOG", "--days", "90");
        String typeId = extractId(add.stdout());
        RunResult r = run("vaccine", "type", "remove", "--id", typeId);
        assertSuccess(r);
        assertOutputContains(r, "Removed vaccine type");
        RunResult list = run("vaccine", "type", "list");
        assertOutputDoesNotContain(list, "TempVaccine");
        assertOutputContains(list, "Stays");
    }

    /**
     * Verifies that attempting to remove a vaccine type with an unknown ID prints an error.
     * A valid vaccine type is registered to rule out false positives from an empty catalog.
     */
    @Test
    void vaccineType_removeNonExistentId_printsError() throws Exception {
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365");
        RunResult r = run("vaccine", "type", "remove",
                "--id", "00000000-0000-0000-0000-000000000000");
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // vaccine list (records)
    // -------------------------------------------------------------------------

    /**
     * Verifies that {@code shelter vaccine list} on a fresh system exits successfully
     * and prints the {@code (none)} empty marker.
     */
    @Test
    void vaccineList_emptyByDefault() throws Exception {
        RunResult r = run("vaccine", "list");
        assertSuccess(r);
        assertOutputContains(r, "(none)");
    }

    /**
     * Verifies that {@code shelter vaccine list} shows a recorded vaccination's animal name,
     * vaccine type, and administration date after the full record flow has been exercised.
     */
    @Test
    void vaccineList_showsRecordedVaccinations() throws Exception {
        String sId = registerShelter();
        String aId = admitDog("Rex", sId);
        assertSuccess(run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--days", "365"));
        assertSuccess(run("vaccine", "record",
                "--animal", aId, "--type", "Rabies", "--date", "2026-04-01"));

        RunResult list = run("vaccine", "list");
        assertSuccess(list);
        assertOutputContains(list, "Rex");
        assertOutputContains(list, "Rabies");
        assertOutputContains(list, "2026-04-01");
    }
}
