package shelter.domain;

import java.util.Objects;

/**
 * Represents the adoption preferences of a prospective adopter.
 * Preferences include desired species, breed, activity level, age range,
 * and whether the adopter requires already-vaccinated animals;
 * these are consumed by matching strategies to score compatibility with available animals.
 * All fields may be {@code null} to indicate no preference, including {@code minAge} and {@code maxAge}.
 */
public class AdopterPreferences {

    private final Species preferredSpecies;
    private final String preferredBreed;
    private final ActivityLevel preferredActivityLevel;
    private final Boolean requiresVaccinated;
    private final Integer minAge;
    private final Integer maxAge;

    /**
     * Constructs an AdopterPreferences instance with the given criteria.
     * Any field may be {@code null} to express no preference.
     * When {@code minAge} or {@code maxAge} are {@code null}, the age criterion is not applied during matching.
     *
     * @param preferredSpecies        the desired {@link Species}, or {@code null} for no preference
     * @param preferredBreed          the desired breed, or {@code null} for no preference
     * @param preferredActivityLevel  the desired activity level, or {@code null} for no preference
     * @param requiresVaccinated      {@code true} if the adopter requires vaccinated animals,
     *                                or {@code null} for no vaccination preference
     * @param minAge                  the minimum preferred age in years; must be non-negative if provided
     * @param maxAge                  the maximum preferred age in years; must be &gt;= {@code minAge} if both provided
     * @throws IllegalArgumentException if {@code minAge} is negative or {@code maxAge} &lt; {@code minAge}
     */
    public AdopterPreferences(Species preferredSpecies, String preferredBreed,
                               ActivityLevel preferredActivityLevel, Boolean requiresVaccinated,
                               Integer minAge, Integer maxAge) {
        if (minAge != null && minAge < 0) {
            throw new IllegalArgumentException("Minimum age must be non-negative.");
        }
        if (minAge != null && maxAge != null && maxAge < minAge) {
            throw new IllegalArgumentException(
                    "Maximum age must be greater than or equal to minimum age.");
        }
        this.preferredSpecies = preferredSpecies;
        this.preferredBreed = preferredBreed;
        this.preferredActivityLevel = preferredActivityLevel;
        this.requiresVaccinated = requiresVaccinated;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    /**
     * Returns the preferred species, or {@code null} if no species preference was set.
     *
     * @return the preferred {@link Species}, or {@code null}
     */
    public Species getPreferredSpecies() {
        return preferredSpecies;
    }

    /**
     * Returns the preferred breed, or {@code null} if no breed preference was set.
     *
     * @return the preferred breed, or {@code null}
     */
    public String getPreferredBreed() {
        return preferredBreed;
    }

    /**
     * Returns the preferred activity level, or {@code null} if no preference was set.
     *
     * @return the preferred {@link ActivityLevel}, or {@code null}
     */
    public ActivityLevel getPreferredActivityLevel() {
        return preferredActivityLevel;
    }

    /**
     * Returns whether the adopter requires vaccinated animals.
     *
     * @return {@code true} if vaccinated animals are required, or {@code null} for no preference
     */
    public Boolean getRequiresVaccinated() {
        return requiresVaccinated;
    }

    /**
     * Returns the minimum preferred age in years, or {@code null} if no minimum was set.
     *
     * @return the minimum preferred age, or {@code null} for no preference
     */
    public Integer getMinAge() {
        return minAge;
    }

    /**
     * Returns the maximum preferred age in years, or {@code null} if no maximum was set.
     *
     * @return the maximum preferred age, or {@code null} for no preference
     */
    public Integer getMaxAge() {
        return maxAge;
    }

    /**
     * Copy constructor that creates a new AdopterPreferences with all field values copied from {@code other}.
     * Since all fields are final and immutable (enums, primitives, String), a shallow copy is sufficient.
     *
     * @param other the AdopterPreferences instance to copy; must not be null
     */
    public AdopterPreferences(AdopterPreferences other) {
        this(other.preferredSpecies, other.preferredBreed, other.preferredActivityLevel,
                other.requiresVaccinated, other.minAge, other.maxAge);
    }

    /**
     * Returns true if the given object is an AdopterPreferences with equal field values.
     * Equality is based on all fields since this is a value object with no unique ID.
     *
     * @param o the object to compare
     * @return true if all preference fields are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdopterPreferences)) return false;
        AdopterPreferences other = (AdopterPreferences) o;
        return Objects.equals(minAge, other.minAge)
                && Objects.equals(maxAge, other.maxAge)
                && preferredSpecies == other.preferredSpecies
                && preferredActivityLevel == other.preferredActivityLevel
                && Objects.equals(requiresVaccinated, other.requiresVaccinated)
                && Objects.equals(preferredBreed, other.preferredBreed);
    }

    /**
     * Returns a hash code based on all preference fields.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(preferredSpecies, preferredBreed, preferredActivityLevel,
                requiresVaccinated, minAge, maxAge);
    }

    /**
     * Returns a string representation of these preferences including species, breed, activity level, and age range.
     *
     * @return a human-readable description of these preferences
     */
    @Override
    public String toString() {
        return "AdopterPreferences[species=" + preferredSpecies + ", breed=" + preferredBreed
                + ", activity=" + preferredActivityLevel
                + ", requiresVaccinated=" + requiresVaccinated
                + ", age=" + (minAge != null ? minAge : "any") + "-" + (maxAge != null ? maxAge : "any") + "]";
    }
}
