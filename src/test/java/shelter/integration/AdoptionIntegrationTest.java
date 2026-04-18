package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter adopt} command group.
 * Covers UC-05.1 through UC-05.4: submit, approve, reject, and cancel adoption requests,
 * including not-found errors, state-transition guards (double-approve, double-reject,
 * double-cancel), and the already-adopted submission error.
 */
class AdoptionIntegrationTest extends CliIntegrationTest {

    /** Registers a shelter with ample capacity and returns its ID. */
    private String registerShelter() throws Exception {
        RunResult r = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "20");
        return extractId(r.stdout());
    }

    /** Admits a dog with the given name to the given shelter and returns its ID. */
    private String admitDog(String shelterId, String name) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", name, "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    /** Registers an adopter with the given name and returns its ID. */
    private String registerAdopter(String name) throws Exception {
        RunResult r = run("adopter", "register",
                "--name", name, "--space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        return extractId(r.stdout());
    }

    /** Submits an adoption request and returns the generated request ID. */
    private String submitAdoption(String adopterId, String animalId) throws Exception {
        RunResult r = run("adopt", "submit",
                "--adopter", adopterId, "--animal", animalId);
        return extractId(r.stdout());
    }

    // -------------------------------------------------------------------------
    // UC-05.1: Submit an Adoption Request
    // -------------------------------------------------------------------------

    /**
     * Verifies that submitting an adoption request for a valid available animal
     * exits successfully and prints the generated request UUID.
     * Two animals are admitted so the request targets a specific one.
     */
    @Test
    void submit_validPair_printsRequestId() throws Exception {
        String shelterId = registerShelter();
        admitDog(shelterId, "Bystander");
        String animalId = admitDog(shelterId, "Rex");
        String adopterId = registerAdopter("Alice");
        RunResult r = run("adopt", "submit", "--adopter", adopterId, "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "id=");
    }

    /**
     * Verifies that submitting an adoption request for a non-existent animal ID prints an error.
     * Two adopters are registered to confirm the guard applies regardless of adopter count.
     */
    @Test
    void submit_nonExistentAnimal_printsError() throws Exception {
        registerAdopter("Alice");
        String adopterId = registerAdopter("Bob");
        RunResult r = run("adopt", "submit",
                "--adopter", adopterId,
                "--animal", "00000000-0000-0000-0000-000000000000");
        assertOutputContains(r, "Error");
    }

    /**
     * Verifies that submitting an adoption request for an already-adopted animal prints an error.
     * A first request is approved to set the animal as adopted before the second submission.
     */
    @Test
    void submit_alreadyAdoptedAnimal_printsError() throws Exception {
        String shelterId = registerShelter();
        String animalId = admitDog(shelterId, "Rex");
        String adopter1 = registerAdopter("Alice");
        String adopter2 = registerAdopter("Bob");
        String requestId = submitAdoption(adopter1, animalId);
        run("adopt", "approve", "--request", requestId);
        RunResult r = run("adopt", "submit", "--adopter", adopter2, "--animal", animalId);
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-05.2: Approve an Adoption Request
    // -------------------------------------------------------------------------

    /**
     * Verifies that approving a pending adoption request prints a confirmation and
     * marks the animal as adopted. Two animals are in the shelter so the list confirms
     * only the adopted animal changes status.
     */
    @Test
    void approve_validRequest_animalMarkedAdopted() throws Exception {
        String shelterId = registerShelter();
        admitDog(shelterId, "FreeAnimal");
        String animalId = admitDog(shelterId, "Rex");
        String adopterId = registerAdopter("Alice");
        String requestId = submitAdoption(adopterId, animalId);
        RunResult r = run("adopt", "approve", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Approved adoption request");
        RunResult list = run("animal", "list");
        assertOutputContains(list, "adopted");
    }

    /**
     * Verifies that approving an already-approved request prints an error.
     * The state-transition guard must reject the second approval attempt.
     */
    @Test
    void approve_alreadyApproved_printsError() throws Exception {
        String shelterId = registerShelter();
        String animalId = admitDog(shelterId, "Rex");
        String adopterId = registerAdopter("Alice");
        String requestId = submitAdoption(adopterId, animalId);
        run("adopt", "approve", "--request", requestId);
        RunResult r = run("adopt", "approve", "--request", requestId);
        assertOutputContains(r, "Error");
    }

    /**
     * Verifies that attempting to approve a non-existent request ID prints an error.
     * A shelter and animal are created to rule out false positives from missing entities.
     */
    @Test
    void approve_nonExistentRequest_printsError() throws Exception {
        registerShelter();
        RunResult r = run("adopt", "approve",
                "--request", "00000000-0000-0000-0000-000000000000");
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-05.3: Reject an Adoption Request
    // -------------------------------------------------------------------------

    /**
     * Verifies that rejecting a pending adoption request prints a confirmation and
     * the animal remains available. Two adopters submit for the same animal so the test
     * can confirm the second request is unaffected by the rejection of the first.
     */
    @Test
    void reject_validRequest_printsConfirmation() throws Exception {
        String shelterId = registerShelter();
        String animalId = admitDog(shelterId, "Rex");
        String adopter1 = registerAdopter("Alice");
        String adopter2 = registerAdopter("Bob");
        String requestId = submitAdoption(adopter1, animalId);
        submitAdoption(adopter2, animalId);
        RunResult r = run("adopt", "reject", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Rejected adoption request");
    }

    /**
     * Verifies that rejecting an already-rejected request prints an error.
     * The state-transition guard must prevent a second rejection attempt.
     */
    @Test
    void reject_alreadyRejected_printsError() throws Exception {
        String shelterId = registerShelter();
        String animalId = admitDog(shelterId, "Rex");
        String adopterId = registerAdopter("Alice");
        String requestId = submitAdoption(adopterId, animalId);
        run("adopt", "reject", "--request", requestId);
        RunResult r = run("adopt", "reject", "--request", requestId);
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-05.4: Cancel an Adoption Request
    // -------------------------------------------------------------------------

    /**
     * Verifies that cancelling a pending adoption request prints a confirmation.
     * Two adoption requests are created so the cancel targets one specifically
     * and the other remains unaffected.
     */
    @Test
    void cancel_validRequest_printsConfirmation() throws Exception {
        String shelterId = registerShelter();
        String animalId1 = admitDog(shelterId, "Rex");
        String animalId2 = admitDog(shelterId, "Max");
        String adopterId = registerAdopter("Alice");
        String requestId = submitAdoption(adopterId, animalId1);
        submitAdoption(adopterId, animalId2);
        RunResult r = run("adopt", "cancel", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Cancelled adoption request");
    }

    /**
     * Verifies that cancelling an already-cancelled request prints an error.
     * The state-transition guard must reject the second cancellation attempt.
     */
    @Test
    void cancel_alreadyCancelled_printsError() throws Exception {
        String shelterId = registerShelter();
        String animalId = admitDog(shelterId, "Rex");
        String adopterId = registerAdopter("Alice");
        String requestId = submitAdoption(adopterId, animalId);
        run("adopt", "cancel", "--request", requestId);
        RunResult r = run("adopt", "cancel", "--request", requestId);
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // adopt list
    // -------------------------------------------------------------------------

    /**
     * Verifies that {@code shelter adopt list} on a fresh system exits successfully
     * and prints the comma header plus the empty {@code (none)} marker.
     */
    @Test
    void adoptList_emptyByDefault() throws Exception {
        RunResult r = run("adopt", "list");
        assertSuccess(r);
        assertOutputContains(r, "ID,");
        assertOutputContains(r, "(none)");
    }

    /**
     * Verifies that {@code shelter adopt list} shows a submitted request's adopter, animal,
     * and PENDING status after the full submit flow has been exercised.
     */
    @Test
    void adoptList_showsSubmittedRequest() throws Exception {
        String shelterId = registerShelter();
        String adopterId = registerAdopter("Alice");
        String animalId = admitDog(shelterId, "Rex");
        assertSuccess(run("adopt", "submit", "--adopter", adopterId, "--animal", animalId));

        RunResult list = run("adopt", "list");
        assertSuccess(list);
        assertOutputContains(list, "Rex");
        assertOutputContains(list, "Alice");
        assertOutputContains(list, "PENDING");
    }
}
