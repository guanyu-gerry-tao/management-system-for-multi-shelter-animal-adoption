package shelter.domain;

import java.time.LocalDate;

/**
 * Represents an animal of an unclassified or uncommon species available for adoption.
 * Extends {@link Animal} with a free-form {@code speciesName} field (e.g., "fish", "parrot",
 * "iguana") that records exactly what kind of animal this is when no dedicated subclass exists.
 */
public class Other extends Animal {

    private final String speciesName;

    /**
     * Reconstruction constructor for deserializing an Other animal from persistent storage.
     * Preserves the original {@code id} and all fields including the free-form species name,
     * allowing exact round-trip restore from CSV data.
     *
     * @param id            the pre-existing unique identifier; must not be null or blank
     * @param name          the animal's name; must not be null or blank
     * @param breed         the animal's breed or description; must not be null or blank
     * @param birthday      the animal's date of birth; must not be null
     * @param activityLevel the animal's activity level; must not be null
     * @param vaccinated    whether the animal has been vaccinated
     * @param adopterId     the ID of the adopter, or {@code null} if not adopted
     * @param shelterId     the ID of the shelter, or {@code null} if unassigned
     * @param speciesName   a free-form name describing the species (e.g., "fish"); must not be null or blank
     * @throws IllegalArgumentException if {@code speciesName} is null or blank, or any base parameter is invalid
     */
    public Other(String id, String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
                 boolean vaccinated, String adopterId, String shelterId, String speciesName) {
        super(id, name, breed, birthday, activityLevel, vaccinated, adopterId, shelterId);
        if (speciesName == null || speciesName.isBlank()) {
            throw new IllegalArgumentException("speciesName must not be null or blank.");
        }
        this.speciesName = speciesName;
    }

    /**
     * Constructs a new Other animal with the given attributes.
     * Use this constructor when admitting a new animal that does not have a dedicated subclass.
     *
     * @param name          the animal's name; must not be null or blank
     * @param breed         the animal's breed or description; must not be null or blank
     * @param birthday      the animal's date of birth; must not be null
     * @param activityLevel the animal's activity level; must not be null
     * @param vaccinated    whether the animal has been vaccinated
     * @param speciesName   a free-form name describing the species (e.g., "fish"); must not be null or blank
     * @throws IllegalArgumentException if {@code speciesName} is null or blank, or any base parameter is invalid
     */
    public Other(String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
                 boolean vaccinated, String speciesName) {
        super(name, breed, birthday, activityLevel, vaccinated);
        if (speciesName == null || speciesName.isBlank()) {
            throw new IllegalArgumentException("speciesName must not be null or blank.");
        }
        this.speciesName = speciesName;
    }

    /**
     * Copy constructor that creates a new Other animal with all field values copied from {@code other}.
     * Inherits base Animal fields via {@link Animal#Animal(Animal)} and copies the species name.
     *
     * @param other the Other instance to copy; must not be null
     */
    public Other(Other other) {
        super(other);
        this.speciesName = other.speciesName;
    }

    /**
     * {@inheritDoc}
     * Always returns {@link Species#OTHER} since this class represents unclassified species.
     */
    @Override
    public Species getSpecies() {
        return Species.OTHER;
    }

    /**
     * {@inheritDoc}
     * Returns {@code false} because Other animals are not supported by the matching system.
     * Matching strategies are designed for dogs, cats, and rabbits only.
     */
    @Override
    public boolean isMatchable() {
        return false;
    }

    /**
     * Returns the free-form species name describing what kind of animal this is.
     * Examples: "fish", "parrot", "iguana".
     *
     * @return the species name string; never null or blank
     */
    public String getSpeciesName() {
        return speciesName;
    }
}
