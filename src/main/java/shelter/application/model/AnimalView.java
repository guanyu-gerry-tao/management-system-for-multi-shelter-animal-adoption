package shelter.application.model;

import shelter.domain.Animal;

import java.util.Objects;

/**
 * A read-only view of an animal enriched with its shelter's display name.
 * Produced by the Application layer so that the CLI does not need to perform
 * cross-domain lookups (e.g. calling ShelterService from inside a list command).
 * The shelter name is resolved once by the Application layer and bundled here.
 */
public class AnimalView {

    private final Animal animal;
    private final String shelterName;

    /**
     * Constructs an AnimalView pairing the given animal with its resolved shelter name.
     * Neither argument may be null.
     *
     * @param animal      the animal to wrap; must not be null
     * @param shelterName the display name of the shelter the animal belongs to; must not be null
     * @throws IllegalArgumentException if either argument is null
     */
    public AnimalView(Animal animal, String shelterName) {
        if (animal == null)      throw new IllegalArgumentException("Animal must not be null.");
        if (shelterName == null) throw new IllegalArgumentException("Shelter name must not be null.");
        this.animal      = animal;
        this.shelterName = shelterName;
    }

    /**
     * Copy constructor that creates a new AnimalView identical to {@code other}.
     * Both the animal reference and the shelter name string are copied by value.
     *
     * @param other the AnimalView to copy; must not be null
     */
    public AnimalView(AnimalView other) {
        this(other.animal, other.shelterName);
    }

    /**
     * Returns the wrapped animal.
     * All animal-specific getters (ID, name, species, etc.) are accessible through this object.
     *
     * @return the animal; never null
     */
    public Animal getAnimal() {
        return animal;
    }

    /**
     * Returns the display name of the shelter this animal currently belongs to.
     * Resolved at construction time by the Application layer.
     *
     * @return the shelter name; never null
     */
    public String getShelterName() {
        return shelterName;
    }

    /**
     * Returns a string representation showing the animal name and shelter name.
     * Intended for logging and debugging; not for user-facing output.
     *
     * @return a human-readable description of this view
     */
    @Override
    public String toString() {
        return "AnimalView[animal=" + animal.getName() + ", shelter=" + shelterName + "]";
    }

    /**
     * Returns true if the other object is an AnimalView with the same animal and shelter name.
     * Equality is value-based since this is a read-only view object with no unique identity.
     *
     * @param o the object to compare
     * @return true if both fields are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnimalView other)) return false;
        return Objects.equals(animal, other.animal)
                && Objects.equals(shelterName, other.shelterName);
    }

    /**
     * Returns a hash code based on the wrapped animal and shelter name.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(animal, shelterName);
    }
}
