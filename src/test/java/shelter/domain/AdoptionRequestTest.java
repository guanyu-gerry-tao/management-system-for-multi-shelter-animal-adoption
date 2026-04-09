package shelter.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdoptionRequest}.
 * Covers initial state, all status transitions, invalid double-transition attempts,
 * and constructor validation.
 */
class AdoptionRequestTest {

    private Adopter adopter;
    private Animal animal;

    @BeforeEach
    void setUp() {
        AdopterPreferences prefs = new AdopterPreferences(Species.DOG, null, ActivityLevel.HIGH, 0, 10);
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null, prefs);
        animal = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, Dog.Size.LARGE, false);
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    void adoptionRequest_startsAsPending() {
        AdoptionRequest request = new AdoptionRequest(adopter, animal);
        assertEquals(RequestStatus.PENDING, request.getStatus());
        assertNotNull(request.getId());
        assertNotNull(request.getSubmittedAt());
        assertEquals(adopter, request.getAdopter());
        assertEquals(animal, request.getAnimal());
    }

    // -------------------------------------------------------------------------
    // Valid transitions
    // -------------------------------------------------------------------------

    @Test
    void approve_transitionsTo_approved() {
        AdoptionRequest request = new AdoptionRequest(adopter, animal);
        request.approve();
        assertEquals(RequestStatus.APPROVED, request.getStatus());
    }

    @Test
    void reject_transitionsTo_rejected() {
        AdoptionRequest request = new AdoptionRequest(adopter, animal);
        request.reject();
        assertEquals(RequestStatus.REJECTED, request.getStatus());
    }

    @Test
    void cancel_transitionsTo_cancelled() {
        AdoptionRequest request = new AdoptionRequest(adopter, animal);
        request.cancel();
        assertEquals(RequestStatus.CANCELLED, request.getStatus());
    }

    // -------------------------------------------------------------------------
    // Invalid transitions (already in terminal state)
    // -------------------------------------------------------------------------

    @Test
    void approve_throwsIllegalStateException_whenAlreadyApproved() {
        AdoptionRequest request = new AdoptionRequest(adopter, animal);
        request.approve();
        assertThrows(IllegalStateException.class, request::approve);
    }

    @Test
    void reject_throwsIllegalStateException_whenAlreadyRejected() {
        AdoptionRequest request = new AdoptionRequest(adopter, animal);
        request.reject();
        assertThrows(IllegalStateException.class, request::reject);
    }

    @Test
    void approve_throwsIllegalStateException_whenAlreadyCancelled() {
        AdoptionRequest request = new AdoptionRequest(adopter, animal);
        request.cancel();
        assertThrows(IllegalStateException.class, request::approve);
    }

    @Test
    void cancel_throwsIllegalStateException_whenAlreadyRejected() {
        AdoptionRequest request = new AdoptionRequest(adopter, animal);
        request.reject();
        assertThrows(IllegalStateException.class, request::cancel);
    }

    // -------------------------------------------------------------------------
    // Constructor validation
    // -------------------------------------------------------------------------

    @Test
    void constructor_throwsIllegalArgumentException_whenAdopterIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new AdoptionRequest(null, animal));
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenAnimalIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new AdoptionRequest(adopter, null));
    }

    @Test
    void eachRequest_hasUniqueId() {
        AdoptionRequest r1 = new AdoptionRequest(adopter, animal);
        AdoptionRequest r2 = new AdoptionRequest(adopter, animal);
        assertNotEquals(r1.getId(), r2.getId());
    }
}
