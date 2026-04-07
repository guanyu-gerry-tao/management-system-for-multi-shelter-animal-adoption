package shelter.domain;

/**
 * Represents a dog available for adoption at a shelter.
 * In addition to the base {@link Animal} attributes, a dog has a physical size
 * classification and a neutered status, both of which can affect matching recommendations.
 */
public class Dog extends Animal {

    /**
     * Represents the physical size of a dog.
     * Size is used by matching strategies to assess suitability for different living spaces
     * and adopter lifestyles.
     */
    public enum Size {
        /** A small dog, generally adaptable to apartment living. */
        SMALL,
        /** A medium-sized dog, suitable for most household types. */
        MEDIUM,
        /** A large dog, requiring more space and physical activity. */
        LARGE
    }

    private final Size size;
    private boolean neutered;

    /**
     * Constructs a new Dog with the given attributes.
     * All parameters are required and validated at construction time.
     *
     * @param name          the dog's name; must not be null or blank
     * @param breed         the dog's breed; must not be null or blank
     * @param age           the dog's age in years; must be non-negative
     * @param activityLevel the dog's activity level; must not be null
     * @param vaccinated    whether the dog has been vaccinated
     * @param size          the dog's size classification; must not be null
     * @param neutered      whether the dog has been neutered
     * @throws IllegalArgumentException if size is null or any {@link Animal} parameter is invalid
     */
    public Dog(String name, String breed, int age, ActivityLevel activityLevel,
               boolean vaccinated, Size size, boolean neutered) {
        super(name, breed, age, activityLevel, vaccinated);
        if (size == null) {
            throw new IllegalArgumentException("Dog size must not be null.");
        }
        this.size = size;
        this.neutered = neutered;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpecies() {
        return "Dog";
    }

    /**
     * Returns the size classification of this dog.
     *
     * @return the dog's {@link Size}
     */
    public Size getSize() {
        return size;
    }

    /**
     * Returns whether this dog has been neutered.
     *
     * @return {@code true} if the dog is neutered, {@code false} otherwise
     */
    public boolean isNeutered() {
        return neutered;
    }

    /**
     * Updates the neutered status of this dog.
     * Typically called when shelter medical records are updated after a procedure.
     *
     * @param neutered the new neutered status
     */
    public void setNeutered(boolean neutered) {
        this.neutered = neutered;
    }

    /**
     * Constructs a copy of the given dog, preserving the same ID and all field values.
     * This copy constructor creates an independent snapshot of an existing dog instance.
     *
     * @param other the dog to copy; must not be null
     */
    public Dog(Dog other) {
        super(other);
        this.size = other.size;
        this.neutered = other.neutered;
    }

    /**
     * Returns a string representation of this dog including all animal and dog-specific attributes.
     *
     * @return a human-readable description of this dog
     */
    @Override
    public String toString() {
        return super.toString().replace("]", ", size=" + size + ", neutered=" + neutered + "]");
    }
}
