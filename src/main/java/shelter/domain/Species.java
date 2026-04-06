package shelter.domain;

/**
 * Represents the species of an animal in the shelter system.
 * Used to enforce type-safe species comparisons across matching, vaccination,
 * and preference logic, replacing error-prone string literals.
 */
public enum Species {

    /** A domestic dog. */
    DOG,

    /** A domestic cat. */
    CAT,

    /** A domestic rabbit. */
    RABBIT,

    /** Any other species not explicitly categorized. */
    OTHER
}
