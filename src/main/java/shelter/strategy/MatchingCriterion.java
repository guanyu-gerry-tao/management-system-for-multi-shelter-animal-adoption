package shelter.strategy;

/**
 * Represents the supported criteria that may be used in the matching system.
 * Each criterion corresponds to one strategy implementation and may later be
 * prioritized by an adopter or shelter staff member.
 */
public enum MatchingCriterion {
    /** Matches animals by species preference. */
    SPECIES,

    /** Matches animals by breed preference. */
    BREED,

    /** Matches animals by activity level compatibility. */
    ACTIVITY_LEVEL,

    /** Matches animals by preferred age or age range. */
    AGE,

    /** Matches animals by lifestyle fit, such as living space and schedule. */
    LIFESTYLE,

    /** Matches animals by vaccination status preference. */
    VACCINATION
}
