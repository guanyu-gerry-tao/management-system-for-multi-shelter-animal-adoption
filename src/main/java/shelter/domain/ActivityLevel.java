package shelter.domain;

/**
 * Represents the activity level of an animal available for adoption.
 * Activity level is used by matching strategies to assess compatibility
 * between an animal and an adopter's lifestyle and living situation.
 */
public enum ActivityLevel {

    /** Low activity; suitable for calm, sedentary, or small-space households. */
    LOW,

    /** Moderate activity; adaptable to most household types and schedules. */
    MEDIUM,

    /** High activity; requires an active adopter lifestyle and ample space. */
    HIGH
}
