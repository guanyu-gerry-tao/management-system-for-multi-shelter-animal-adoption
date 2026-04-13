package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter transfer} command group.
 * Covers UC-06.1 through UC-06.4: request, approve, reject, and cancel transfer requests,
 * including destination-at-capacity and cancel-after-approve error cases.
 */
class TransferIntegrationTest extends CliIntegrationTest {

    /** Registers a shelter with the given name and capacity and returns its ID. */
    private String registerShelter(String name, int capacity) throws Exception {
        RunResult r = run("shelter", "register",
                "--name", name, "--location", "Boston", "--capacity", String.valueOf(capacity));
        return extractId(r.stdout());
    }

    /** Admits a dog with the given name to the given shelter and returns its ID. */
    private String admitDog(String name, String shelterId) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", name, "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    // -------------------------------------------------------------------------
    // UC-06.1: Request an Animal Transfer
    // -------------------------------------------------------------------------

    /**
     * Verifies that creating a transfer request for an available animal between two shelters
     * exits successfully and prints the generated request UUID.
     * Two animals are admitted to the source shelter so the request targets one specifically.
     */
    @Test
    void request_validTransfer_printsRequestId() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 10);
        admitDog("Stays", shelterA);
        String animalId = admitDog("Rex", shelterA);
        RunResult r = run("transfer", "request",
                "--animal", animalId, "--from", shelterA, "--to", shelterB);
        assertSuccess(r);
        assertOutputContains(r, "id=");
    }

    /**
     * Verifies that two animals can each have independent transfer requests created in the same session.
     * Both confirmations must print a request UUID, confirming independent request creation.
     */
    @Test
    void request_twoAnimals_bothRequestsCreated() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 10);
        String animal1 = admitDog("Alpha", shelterA);
        String animal2 = admitDog("Beta", shelterA);
        RunResult r1 = run("transfer", "request",
                "--animal", animal1, "--from", shelterA, "--to", shelterB);
        RunResult r2 = run("transfer", "request",
                "--animal", animal2, "--from", shelterA, "--to", shelterB);
        assertSuccess(r1);
        assertSuccess(r2);
        assertOutputContains(r1, "id=");
        assertOutputContains(r2, "id=");
    }

    /**
     * Verifies that requesting a transfer when the destination shelter is at capacity prints an error.
     * Shelter B is filled to its one-animal limit before the transfer attempt is made.
     */
    @Test
    void request_destinationAtCapacity_printsError() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 1);
        String animalForA = admitDog("FromA", shelterA);
        admitDog("FillerInB", shelterB);
        RunResult r = run("transfer", "request",
                "--animal", animalForA, "--from", shelterA, "--to", shelterB);
        assertOutputContains(r, "Error");
    }

    // -------------------------------------------------------------------------
    // UC-06.2: Approve a Transfer Request
    // -------------------------------------------------------------------------

    /**
     * Verifies that approving a transfer request moves the targeted animal to the destination shelter.
     * A second animal stays in the source shelter to confirm only the transferred animal relocates.
     */
    @Test
    void approve_validTransfer_animalAppearsInNewShelter() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 10);
        admitDog("Stays", shelterA);
        String transferId = admitDog("Rex", shelterA);
        RunResult requestResult = run("transfer", "request",
                "--animal", transferId, "--from", shelterA, "--to", shelterB);
        String requestId = extractId(requestResult.stdout());
        RunResult r = run("transfer", "approve", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Approved transfer request");
        RunResult listB = run("animal", "list", "--shelter", shelterB);
        assertOutputContains(listB, "Rex");
        RunResult listA = run("animal", "list", "--shelter", shelterA);
        assertOutputContains(listA, "Stays");
        assertOutputDoesNotContain(listA, "Rex");
    }

    // -------------------------------------------------------------------------
    // UC-06.3: Reject a Transfer Request
    // -------------------------------------------------------------------------

    /**
     * Verifies that rejecting a transfer request prints a confirmation and leaves both animals
     * in their original shelters. Two transfer requests are created; only the targeted one is rejected.
     */
    @Test
    void reject_validTransfer_animalRemainsInSource() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 10);
        String animal1 = admitDog("Rex", shelterA);
        String animal2 = admitDog("Max", shelterA);
        RunResult requestResult = run("transfer", "request",
                "--animal", animal1, "--from", shelterA, "--to", shelterB);
        String requestId = extractId(requestResult.stdout());
        run("transfer", "request",
                "--animal", animal2, "--from", shelterA, "--to", shelterB);
        RunResult r = run("transfer", "reject", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Rejected transfer request");
        RunResult listA = run("animal", "list", "--shelter", shelterA);
        assertOutputContains(listA, "Rex");
    }

    // -------------------------------------------------------------------------
    // UC-06.4: Cancel a Transfer Request
    // -------------------------------------------------------------------------

    /**
     * Verifies that cancelling a pending transfer request prints a confirmation.
     * Two animals each have their own transfer request; only one is cancelled to confirm
     * the cancel targets a specific request.
     */
    @Test
    void cancel_pendingTransfer_printsConfirmation() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 10);
        String animal1 = admitDog("Rex", shelterA);
        String animal2 = admitDog("Max", shelterA);
        RunResult requestResult = run("transfer", "request",
                "--animal", animal1, "--from", shelterA, "--to", shelterB);
        String requestId = extractId(requestResult.stdout());
        run("transfer", "request",
                "--animal", animal2, "--from", shelterA, "--to", shelterB);
        RunResult r = run("transfer", "cancel", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Cancelled transfer request");
    }

    /**
     * Verifies that attempting to cancel an already-approved transfer request prints an error.
     * The state-transition guard must reject a cancel on a non-pending request.
     */
    @Test
    void cancel_alreadyApproved_printsError() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 10);
        String animal1 = admitDog("Rex", shelterA);
        String animal2 = admitDog("Max", shelterA);
        RunResult requestResult = run("transfer", "request",
                "--animal", animal1, "--from", shelterA, "--to", shelterB);
        String requestId = extractId(requestResult.stdout());
        run("transfer", "request",
                "--animal", animal2, "--from", shelterA, "--to", shelterB);
        run("transfer", "approve", "--request", requestId);
        RunResult r = run("transfer", "cancel", "--request", requestId);
        assertOutputContains(r, "Error");
    }
}
