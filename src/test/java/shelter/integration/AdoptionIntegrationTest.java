package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter adopt} command group.
 * Tests the full adoption lifecycle: submit → approve/reject/cancel, plus error paths.
 */
class AdoptionIntegrationTest extends CliIntegrationTest {

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

    private String registerAdopter() throws Exception {
        RunResult r = run("adopter", "register",
                "--name", "Alice", "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        return extractId(r.stdout());
    }

    private String submitAdoption(String adopterId, String animalId) throws Exception {
        RunResult r = run("adopt", "submit",
                "--adopter", adopterId, "--animal", animalId);
        return extractId(r.stdout());
    }

    @Test
    void submit_validPair_printsRequestId() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        String adopterId = registerAdopter();
        RunResult r = run("adopt", "submit",
                "--adopter", adopterId, "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "id=");
    }

    @Test
    void approve_validRequest_printsConfirmationAndAnimalIsAdopted() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        String adopterId = registerAdopter();
        String requestId = submitAdoption(adopterId, animalId);
        RunResult r = run("adopt", "approve", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Approved adoption request");
        RunResult list = run("animal", "list", "--shelter", shelterId);
        assertOutputContains(list, "adopted");
    }

    @Test
    void reject_validRequest_printsConfirmation() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        String adopterId = registerAdopter();
        String requestId = submitAdoption(adopterId, animalId);
        RunResult r = run("adopt", "reject", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Rejected adoption request");
    }

    @Test
    void cancel_validRequest_printsConfirmation() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        String adopterId = registerAdopter();
        String requestId = submitAdoption(adopterId, animalId);
        RunResult r = run("adopt", "cancel", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Cancelled adoption request");
    }

    @Test
    void approve_nonExistentRequest_printsError() throws Exception {
        RunResult r = run("adopt", "approve",
                "--request", "00000000-0000-0000-0000-000000000000");
        assertOutputContains(r, "Error");
    }

    @Test
    void submit_alreadyAdoptedAnimal_printsError() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        String adopterId = registerAdopter();
        String requestId = submitAdoption(adopterId, animalId);
        run("adopt", "approve", "--request", requestId);
        RunResult r = run("adopt", "submit",
                "--adopter", adopterId, "--animal", animalId);
        assertOutputContains(r, "Error");
    }
}
