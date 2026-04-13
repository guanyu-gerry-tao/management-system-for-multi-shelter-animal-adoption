package shelter.domain;

/**
 * Represents the type of living space an adopter occupies.
 * This value is used by lifestyle-based matching strategies to assess whether
 * a given animal is suitable for the adopter's home environment.
 */
public enum LivingSpace {

    /** A small apartment without private outdoor access. */
    APARTMENT,

    /** A house without a yard; limited outdoor space. */
    HOUSE_NO_YARD,

    /** A house with a yard, providing outdoor space for animals. */
    HOUSE_WITH_YARD
}
