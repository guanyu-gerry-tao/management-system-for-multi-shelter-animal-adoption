package shelter.application;

import shelter.domain.Adopter;
import shelter.domain.ActivityLevel;
import shelter.domain.DailySchedule;
import shelter.domain.LivingSpace;
import shelter.domain.Species;

import java.util.List;

/**
 * Application service for adopter management use cases.
 * Orchestrates adopter registration, updates, and removals,
 * ensuring that preferences are constructed correctly and audit logging is performed.
 */
public interface AdopterApplicationService {

    /**
     * Registers a new adopter with the given personal details and preferences.
     * Preference fields ({@code preferredSpecies}, {@code preferredBreed}, {@code preferredActivityLevel})
     * may be {@code null} to indicate no preference. Throws an exception if the adopter already exists.
     *
     * @param name                   the adopter's name; must not be null or blank
     * @param livingSpace            the adopter's living space type; must not be null
     * @param dailySchedule          the adopter's daily schedule; must not be null
     * @param preferredSpecies       the preferred species, or {@code null} for no preference
     * @param preferredBreed         the preferred breed, or {@code null} for no preference
     * @param preferredActivityLevel the preferred activity level, or {@code null} for no preference
     * @param minAge                 the minimum preferred animal age; must be non-negative
     * @param maxAge                 the maximum preferred animal age; must be &gt;= {@code minAge}
     * @return the newly created {@link Adopter}
     */
    Adopter registerAdopter(String name, LivingSpace livingSpace, DailySchedule dailySchedule,
                             Species preferredSpecies, String preferredBreed,
                             ActivityLevel preferredActivityLevel, int minAge, int maxAge);

    /**
     * Returns a list of all registered adopters in the system.
     * Returns an empty list if no adopters have been registered.
     *
     * @return a list of all adopters
     */
    List<Adopter> listAdopters();

    /**
     * Updates an existing adopter's personal details and preferences with the provided values.
     * Only non-null parameters are applied; omitted (null) fields retain their current values.
     * Throws an exception if the adopter is not found.
     *
     * @param adopterId              the ID of the adopter to update; must not be null or blank
     * @param name                   the new name, or {@code null} to keep the current value
     * @param livingSpace            the new living space, or {@code null} to keep the current value
     * @param dailySchedule          the new daily schedule, or {@code null} to keep the current value
     * @param preferredSpecies       the new preferred species, or {@code null} to keep the current value
     * @param preferredBreed         the new preferred breed, or {@code null} to keep the current value
     * @param preferredActivityLevel the new preferred activity level, or {@code null} to keep the current value
     * @param minAge                 the new minimum preferred age, or {@code null} to keep the current value
     * @param maxAge                 the new maximum preferred age, or {@code null} to keep the current value
     * @return the updated {@link Adopter}
     */
    Adopter updateAdopter(String adopterId, String name, LivingSpace livingSpace,
                          DailySchedule dailySchedule, Species preferredSpecies,
                          String preferredBreed, ActivityLevel preferredActivityLevel,
                          Integer minAge, Integer maxAge);

    /**
     * Removes an adopter from the system by ID.
     * Throws an exception if the adopter is not found or has a pending adoption request.
     *
     * @param adopterId the ID of the adopter to remove; must not be null or blank
     */
    void removeAdopter(String adopterId);
}
