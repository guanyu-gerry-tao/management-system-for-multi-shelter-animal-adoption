package shelter.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransferRequest}.
 * Covers initial state, all status transitions, invalid transitions,
 * and constructor validation including same-shelter rejection.
 */
class TransferRequestTest {

    private Animal animal;
    private Shelter shelterA;
    private Shelter shelterB;

    @BeforeEach
    void setUp() {
        animal = new Dog("Rex", "Labrador", 3, ActivityLevel.HIGH, true, Dog.Size.LARGE, false);
        shelterA = new Shelter("Shelter A", "Boston, MA", 10);
        shelterB = new Shelter("Shelter B", "Cambridge, MA", 10);
        shelterA.addAnimal(animal);
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    void transferRequest_startsAsPending() {
        TransferRequest request = new TransferRequest(animal, shelterA, shelterB);
        assertEquals(RequestStatus.PENDING, request.getStatus());
        assertNotNull(request.getId());
        assertNotNull(request.getRequestedAt());
        assertEquals(animal, request.getAnimal());
        assertEquals(shelterA, request.getFrom());
        assertEquals(shelterB, request.getTo());
    }

    // -------------------------------------------------------------------------
    // Valid transitions
    // -------------------------------------------------------------------------

    @Test
    void approve_transitionsTo_approved() {
        TransferRequest request = new TransferRequest(animal, shelterA, shelterB);
        request.approve();
        assertEquals(RequestStatus.APPROVED, request.getStatus());
    }

    @Test
    void reject_transitionsTo_rejected() {
        TransferRequest request = new TransferRequest(animal, shelterA, shelterB);
        request.reject();
        assertEquals(RequestStatus.REJECTED, request.getStatus());
    }

    @Test
    void cancel_transitionsTo_cancelled() {
        TransferRequest request = new TransferRequest(animal, shelterA, shelterB);
        request.cancel();
        assertEquals(RequestStatus.CANCELLED, request.getStatus());
    }

    // -------------------------------------------------------------------------
    // Invalid transitions
    // -------------------------------------------------------------------------

    @Test
    void approve_throwsIllegalStateException_whenAlreadyRejected() {
        TransferRequest request = new TransferRequest(animal, shelterA, shelterB);
        request.reject();
        assertThrows(IllegalStateException.class, request::approve);
    }

    @Test
    void reject_throwsIllegalStateException_whenAlreadyApproved() {
        TransferRequest request = new TransferRequest(animal, shelterA, shelterB);
        request.approve();
        assertThrows(IllegalStateException.class, request::reject);
    }

    @Test
    void cancel_throwsIllegalStateException_whenAlreadyCancelled() {
        TransferRequest request = new TransferRequest(animal, shelterA, shelterB);
        request.cancel();
        assertThrows(IllegalStateException.class, request::cancel);
    }

    // -------------------------------------------------------------------------
    // Constructor validation
    // -------------------------------------------------------------------------

    @Test
    void constructor_throwsIllegalArgumentException_whenAnimalIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new TransferRequest(null, shelterA, shelterB));
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenFromIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new TransferRequest(animal, null, shelterB));
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenToIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new TransferRequest(animal, shelterA, null));
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenSameShelter() {
        assertThrows(IllegalArgumentException.class, () ->
                new TransferRequest(animal, shelterA, shelterA));
    }

    @Test
    void eachRequest_hasUniqueId() {
        TransferRequest r1 = new TransferRequest(animal, shelterA, shelterB);
        TransferRequest r2 = new TransferRequest(animal, shelterA, shelterB);
        assertNotEquals(r1.getId(), r2.getId());
    }
}
