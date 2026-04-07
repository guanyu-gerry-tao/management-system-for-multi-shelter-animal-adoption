package shelter.domain;

import java.util.Objects;

/**
 * Represents the adoption preferences of a prospective adopter.
 * Preferences include desired species, breed, activity level, and age range;
 * these are consumed by matching strategies to score compatibility with available animals.
 * All fields except {@code minAge} and {@code maxAge} may be {@code null} to indicate no preference.
 */
public class AdopterPreferences {

    private final String preferredSpecies;
    private final String preferredBreed;
    private final ActivityLevel preferredActivityLevel;
    private final int minAge;
    private final int maxAge;

    /**
     * Constructs an AdopterPreferences instance with the given criteria.
     * {@code preferredSpecies}, {@code preferredBreed}, and {@code preferredActivityLevel}
     * may be {@code null} to express no preference; age bounds must be valid.
     *
     * @param preferredSpecies        the desired species name (e.g., "Dog"), or {@code null}
     * @param preferredBreed          the desired breed, or {@code null} for no preference
     * @param preferredActivityLevel  the desired activity level, or {@code null} for no preference
     * @param minAge                  the minimum preferred age in years; must be non-negative
     * @param maxAge                  the maximum preferred age in years; must be &gt;= {@code minAge}
     * @throws IllegalArgumentException if {@code minAge} is negative or {@code maxAge} &lt; {@code minAge}
     */
    public AdopterPreferences(String preferredSpecies, String preferredBreed,
                               ActivityLevel preferredActivityLevel, int minAge, int maxAge) {
        if (minAge < 0) {
            throw new IllegalArgumentException("Minimum age must be non-negative.");
        }
        if (maxAge < minAge) {
            throw new IllegalArgumentException(
                    "Maximum age must be greater than or equal to minimum age.");
        }
        this.preferredSpecies = preferredSpecies;
        this.preferredBreed = preferredBreed;
        this.preferredActivityLevel = preferredActivityLevel;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    /**
     * Constructs a copy of the given preferences, preserving all field values.
     * This copy constructor creates an independent snapshot of existing preferences.
     *
     * @param other the preferences to copy; must not be null
     */
    public AdopterPreferences(AdopterPreferences other) {
        this.preferredSpecies = other.preferredSpecies;
        this.preferredBreed = other.preferredBreed;
        this.preferredActivityLevel = other.preferredActivityLevel;
        this.minAge = other.minAge;
        this.maxAge = other.maxAge;
    }

    /**
     * Returns the preferred species, or {@code null} if no species preference was set.
     *
     * @return the preferred species name, or {@code null}
     */
    public String getPreferredSpecies() {
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
     * Returns the minimum preferred age in years.
     *
     * @return the minimum preferred age, always non-negative
     */
    public int getMinAge() {
        return minAge;
    }

    /**
     * Returns the maximum preferred age in years.
     *
     * @return the maximum preferred age, always &gt;= {@code minAge}
     */
    public int getMaxAge() {
        return maxAge;
    }

    /**
     * Returns a string representation of these preferences including all criteria fields.
     *
     * @return a human-readable description of these adoption preferences
     */
    @Override
    public String toString() {
        return "AdopterPreferences[species=" + preferredSpecies
                + ", breed=" + preferredBreed
                + ", activity=" + preferredActivityLevel
                + ", ageRange=" + minAge + "-" + maxAge + "]";
    }

    /**
     * Returns true if the given object is an AdopterPreferences with identical field values.
     * As a value object, equality is based on all fields rather than a unique identifier.
     *
     * @param o the object to compare
     * @return true if all fields are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdopterPreferences)) return false;
        AdopterPreferences other = (AdopterPreferences) o;
        return minAge == other.minAge
                && maxAge == other.maxAge
                && Objects.equals(preferredSpecies, other.preferredSpecies)
                && Objects.equals(preferredBreed, other.preferredBreed)
                && preferredActivityLevel == other.preferredActivityLevel;
    }

    /**
     * Returns a hash code based on all fields of these preferences.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(preferredSpecies, preferredBreed, preferredActivityLevel, minAge, maxAge);
    }
}
