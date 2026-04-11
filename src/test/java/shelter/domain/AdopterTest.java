package shelter.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Adopter} and {@link AdopterPreferences}.
 * Covers construction validation, getter correctness, null-allowed fields,
 * and unique ID generation.
 */
class AdopterTest {

    private AdopterPreferences defaultPrefs() {
        return new AdopterPreferences(Species.DOG, "Labrador", ActivityLevel.HIGH, null, 1, 5);
    }

    // -------------------------------------------------------------------------
    // Adopter — happy path
    // -------------------------------------------------------------------------

    @Test
    void adopter_createsSuccessfully_withValidArguments() {
        AdopterPreferences prefs = defaultPrefs();
        Adopter adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, "Loves big dogs", prefs);

        assertNotNull(adopter.getId());
        assertEquals("Alice", adopter.getName());
        assertEquals(LivingSpace.HOUSE_WITH_YARD, adopter.getLivingSpace());
        assertEquals(DailySchedule.HOME_MOST_OF_DAY, adopter.getDailySchedule());
        assertEquals("Loves big dogs", adopter.getPersonalNotes());
        assertEquals(prefs, adopter.getPreferences());
    }

    @Test
    void adopter_allowsNullPersonalNotes() {
        Adopter adopter = new Adopter("Bob", LivingSpace.APARTMENT,
                DailySchedule.AWAY_MOST_OF_DAY, null, defaultPrefs());
        assertNull(adopter.getPersonalNotes());
    }

    @Test
    void eachAdopter_hasUniqueId() {
        AdopterPreferences prefs = defaultPrefs();
        Adopter a1 = new Adopter("Alice", LivingSpace.APARTMENT,
                DailySchedule.HOME_MOST_OF_DAY, null, prefs);
        Adopter a2 = new Adopter("Bob", LivingSpace.APARTMENT,
                DailySchedule.HOME_MOST_OF_DAY, null, prefs);
        assertNotEquals(a1.getId(), a2.getId());
    }

    // -------------------------------------------------------------------------
    // Adopter — validation failures
    // -------------------------------------------------------------------------

    @Test
    void adopter_throwsIllegalArgumentException_whenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Adopter(null, LivingSpace.APARTMENT,
                        DailySchedule.AWAY_PART_OF_DAY, null, defaultPrefs()));
    }

    @Test
    void adopter_throwsIllegalArgumentException_whenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                new Adopter("  ", LivingSpace.APARTMENT,
                        DailySchedule.AWAY_PART_OF_DAY, null, defaultPrefs()));
    }

    @Test
    void adopter_throwsIllegalArgumentException_whenLivingSpaceIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Adopter("Alice", null,
                        DailySchedule.HOME_MOST_OF_DAY, null, defaultPrefs()));
    }

    @Test
    void adopter_throwsIllegalArgumentException_whenDailyScheduleIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Adopter("Alice", LivingSpace.APARTMENT, null, null, defaultPrefs()));
    }

    @Test
    void adopter_throwsIllegalArgumentException_whenPreferencesIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Adopter("Alice", LivingSpace.APARTMENT,
                        DailySchedule.HOME_MOST_OF_DAY, null, null));
    }

    // -------------------------------------------------------------------------
    // AdopterPreferences — happy path
    // -------------------------------------------------------------------------

    @Test
    void adopterPreferences_storesAllFields() {
        AdopterPreferences prefs = new AdopterPreferences(Species.CAT, "Persian",
                ActivityLevel.LOW, true, 0, 8);
        assertEquals(Species.CAT, prefs.getPreferredSpecies());
        assertEquals("Persian", prefs.getPreferredBreed());
        assertEquals(ActivityLevel.LOW, prefs.getPreferredActivityLevel());
        assertEquals(true, prefs.getRequiresVaccinated());
        assertEquals(0, prefs.getMinAge());
        assertEquals(8, prefs.getMaxAge());
    }

    @Test
    void adopterPreferences_allowsNullSpeciesBreedAndActivity() {
        AdopterPreferences prefs = new AdopterPreferences(null, null, null, null, 0, 10);
        assertNull(prefs.getPreferredSpecies());
        assertNull(prefs.getPreferredBreed());
        assertNull(prefs.getPreferredActivityLevel());
        assertNull(prefs.getRequiresVaccinated());
    }

    @Test
    void adopterPreferences_minEqualToMaxAgeIsValid() {
        AdopterPreferences prefs = new AdopterPreferences(null, null, null, null, 3, 3);
        assertEquals(3, prefs.getMinAge());
        assertEquals(3, prefs.getMaxAge());
    }

    // -------------------------------------------------------------------------
    // AdopterPreferences — validation failures
    // -------------------------------------------------------------------------

    @Test
    void adopterPreferences_throwsIllegalArgumentException_whenMinAgeIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new AdopterPreferences(Species.DOG, "Labrador", ActivityLevel.HIGH, null, -1, 5));
    }

    @Test
    void adopterPreferences_throwsIllegalArgumentException_whenMaxAgeLessThanMinAge() {
        assertThrows(IllegalArgumentException.class, () ->
                new AdopterPreferences(Species.DOG, "Labrador", ActivityLevel.HIGH, null, 5, 3));
    }
}
