package shelter.domain;

import java.time.LocalDate;

/**
 * Represents a cat available for adoption at a shelter.
 * In addition to the base {@link Animal} attributes, a cat carries an indoor-only flag
 * and a neutered status, which are relevant to lifestyle matching and medical records.
 */
public class Cat extends Animal {

    private final boolean indoor;
    private boolean neutered;

    /**
     * Reconstruction constructor for deserializing a Cat from persistent storage.
     * This constructor preserves the original {@code id} and all fields including
     * the indoor flag and neutered status, allowing exact round-trip restore from CSV data.
     *
     * @param id            the pre-existing unique identifier; must not be null or blank
     * @param name          the cat's name; must not be null or blank
     * @param breed         the cat's breed; must not be null or blank
     * @param birthday      the cat's date of birth; must not be null
     * @param activityLevel the cat's activity level; must not be null
     * @param vaccinated    whether the cat has been vaccinated
     * @param adopterId     the ID of the adopter, or {@code null} if not adopted
     * @param shelterId     the ID of the shelter, or {@code null} if unassigned
     * @param indoor        whether the cat is suited for indoor-only living
     * @param neutered      whether the cat has been neutered
     * @throws IllegalArgumentException if any {@link Animal} parameter is invalid
     */
    public Cat(String id, String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
               boolean vaccinated, String adopterId, String shelterId, boolean indoor, boolean neutered) {
        super(id, name, breed, birthday, activityLevel, vaccinated, adopterId, shelterId);
        this.indoor = indoor;
        this.neutered = neutered;
    }

    /**
     * Constructs a new Cat with the given attributes.
     * All parameters are validated at construction time per {@link Animal} rules.
     *
     * @param name          the cat's name; must not be null or blank
     * @param breed         the cat's breed; must not be null or blank
     * @param birthday      the cat's date of birth; must not be null
     * @param activityLevel the cat's activity level; must not be null
     * @param vaccinated    whether the cat has been vaccinated
     * @param indoor        whether the cat is suited for indoor-only living
     * @param neutered      whether the cat has been neutered
     * @throws IllegalArgumentException if any {@link Animal} parameter is invalid
     */
    public Cat(String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
               boolean vaccinated, boolean indoor, boolean neutered) {
        super(name, breed, birthday, activityLevel, vaccinated);
        this.indoor = indoor;
        this.neutered = neutered;
    }

    /**
     * Copy constructor that creates a new Cat with all field values copied from {@code other}.
     * Inherits base Animal fields via {@link Animal#Animal(Animal)} and copies Cat-specific fields.
     *
     * @param other the Cat instance to copy; must not be null
     */
    public Cat(Cat other) {
        super(other);
        this.indoor = other.indoor;
        this.neutered = other.neutered;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Species getSpecies() {
        return Species.CAT;
    }

    /**
     * Returns whether this cat is an indoor-only cat.
     * Indoor cats are generally better suited for apartment or enclosed-space living.
     *
     * @return {@code true} if the cat prefers indoor living, {@code false} otherwise
     */
    public boolean isIndoor() {
        return indoor;
    }

    /**
     * Returns whether this cat has been neutered.
     *
     * @return {@code true} if the cat is neutered, {@code false} otherwise
     */
    public boolean isNeutered() {
        return neutered;
    }

    /**
     * Updates the neutered status of this cat.
     * Typically called when shelter medical records are updated after a procedure.
     *
     * @param neutered the new neutered status
     */
    public void setNeutered(boolean neutered) {
        this.neutered = neutered;
    }
}
