package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter transfer} command group.
 * Verifies request, approve (animal moves shelter), and reject flows.
 */
class TransferIntegrationTest extends CliIntegrationTest {

    private String registerShelter(String name) throws Exception {
        RunResult r = run("shelter", "register",
                "--name", name, "--location", "Boston", "--capacity", "10");
        return extractId(r.stdout());
    }

    private String admitDog(String shelterId) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    @Test
    void request_validTransfer_printsRequestId() throws Exception {
        String shelterA = registerShelter("Shelter A");
        String shelterB = registerShelter("Shelter B");
        String animalId = admitDog(shelterA);
        RunResult r = run("transfer", "request",
                "--animal", animalId, "--from", shelterA, "--to", shelterB);
        assertSuccess(r);
        assertOutputContains(r, "id=");
    }

    @Test
    void approve_validTransfer_animalAppearsInNewShelter() throws Exception {
        String shelterA = registerShelter("Shelter A");
        String shelterB = registerShelter("Shelter B");
        String animalId = admitDog(shelterA);
        RunResult requestResult = run("transfer", "request",
                "--animal", animalId, "--from", shelterA, "--to", shelterB);
        String requestId = extractId(requestResult.stdout());
        RunResult r = run("transfer", "approve", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Approved transfer request");
        RunResult list = run("animal", "list", "--shelter", shelterB);
        assertOutputContains(list, "Rex");
    }

    @Test
    void reject_validTransfer_printsConfirmation() throws Exception {
        String shelterA = registerShelter("Shelter A");
        String shelterB = registerShelter("Shelter B");
        String animalId = admitDog(shelterA);
        RunResult requestResult = run("transfer", "request",
                "--animal", animalId, "--from", shelterA, "--to", shelterB);
        String requestId = extractId(requestResult.stdout());
        RunResult r = run("transfer", "reject", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Rejected transfer request");
    }
}
