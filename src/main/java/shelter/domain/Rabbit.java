package shelter.domain;

/**
 * Represents a rabbit available for adoption at a shelter.
 * In addition to the base {@link Animal} attributes, a rabbit has a fur length
 * classification that may be relevant for adopters with grooming preferences or allergies.
 */
public class Rabbit extends Animal {

    /**
     * Represents the fur length of a rabbit.
     * Fur length affects grooming requirements and may influence adopter decisions
     * regarding allergies or maintenance effort.
     */
    public enum FurLength {
        /** Short fur requiring minimal grooming. */
        SHORT,
        /** Long fur requiring regular grooming to prevent matting. */
        LONG
    }

    private final FurLength furLength;

    /**
     * Constructs a new Rabbit with the given attributes.
     * All parameters are required and validated at construction time.
     *
     * @param name          the rabbit's name; must not be null or blank
     * @param breed         the rabbit's breed; must not be null or blank
     * @param age           the rabbit's age in years; must be non-negative
     * @param activityLevel the rabbit's activity level; must not be null
     * @param vaccinated    whether the rabbit has been vaccinated
     * @param furLength     the rabbit's fur length; must not be null
     * @throws IllegalArgumentException if furLength is null or any {@link Animal} parameter is invalid
     */
    public Rabbit(String name, String breed, int age, ActivityLevel activityLevel,
                  boolean vaccinated, FurLength furLength) {
        super(name, breed, age, activityLevel, vaccinated);
        if (furLength == null) {
            throw new IllegalArgumentException("Rabbit fur length must not be null.");
        }
        this.furLength = furLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Species getSpecies() {
        return Species.RABBIT;
    }

    /**
     * Returns the fur length of this rabbit.
     *
     * @return the rabbit's {@link FurLength}
     */
    public FurLength getFurLength() {
        return furLength;
    }
}
