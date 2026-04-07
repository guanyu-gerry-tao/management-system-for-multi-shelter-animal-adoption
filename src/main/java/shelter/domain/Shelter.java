package shelter.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an animal shelter that holds a collection of animals available for adoption.
 * A shelter enforces a maximum capacity and provides operations to add, remove, query,
 * and list the animals it currently holds.
 */
public class Shelter implements Comparable<Shelter> {

    private final String id;
    private final String name;
    private final String location;
    private final int capacity;
    private final List<Animal> animals;

    /**
     * Constructs a new Shelter with the given name, location, and capacity.
     * Name and location must be non-null and non-blank; capacity must be a positive integer.
     *
     * @param name     the shelter's name; must not be null or blank
     * @param location the shelter's physical address or city; must not be null or blank
     * @param capacity the maximum number of animals the shelter can hold; must be positive
     * @throws IllegalArgumentException if any parameter is null, blank, or capacity is not positive
     */
    public Shelter(String name, String location, int capacity) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Shelter name must not be null or blank.");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Shelter location must not be null or blank.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Shelter capacity must be a positive integer.");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.animals = new ArrayList<>();
    }

    /**
     * Constructs a copy of the given shelter, preserving the same ID and all field values.
     * The animal list is defensively copied so modifications to the copy do not affect the original.
     *
     * @param other the shelter to copy; must not be null
     */
    public Shelter(Shelter other) {
        this.id = other.id;
        this.name = other.name;
        this.location = other.location;
        this.capacity = other.capacity;
        this.animals = new ArrayList<>(other.animals);
    }

    /**
     * Adds an animal to this shelter if capacity allows.
     * The same animal (identified by ID) cannot be added twice to the same shelter.
     *
     * @param animal the animal to add; must not be null
     * @throws IllegalArgumentException if {@code animal} is null or is already present in this shelter
     * @throws IllegalStateException    if the shelter is at full capacity
     */
    public void addAnimal(Animal animal) {
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        if (animals.stream().anyMatch(a -> a.getId().equals(animal.getId()))) {
            throw new IllegalArgumentException(
                    "Animal with ID " + animal.getId() + " is already in this shelter.");
        }
        if (animals.size() >= capacity) {
            throw new IllegalStateException(
                    "Shelter \"" + name + "\" is at full capacity (" + capacity + ").");
        }
        animals.add(animal);
    }

    /**
     * Removes the animal with the given ID from this shelter.
     * Throws if no animal with the given ID is currently in the shelter.
     *
     * @param animalId the ID of the animal to remove; must not be null
     * @throws IllegalArgumentException if {@code animalId} is null or no matching animal is found
     */
    public void removeAnimal(String animalId) {
        if (animalId == null) {
            throw new IllegalArgumentException("Animal ID must not be null.");
        }
        boolean removed = animals.removeIf(a -> a.getId().equals(animalId));
        if (!removed) {
            throw new IllegalArgumentException(
                    "No animal with ID " + animalId + " found in shelter \"" + name + "\".");
        }
    }

    /**
     * Returns whether an animal with the given ID is currently in this shelter.
     *
     * @param animalId the ID to search for; must not be null
     * @return {@code true} if the animal is present, {@code false} otherwise
     * @throws IllegalArgumentException if {@code animalId} is null
     */
    public boolean containsAnimal(String animalId) {
        if (animalId == null) {
            throw new IllegalArgumentException("Animal ID must not be null.");
        }
        return animals.stream().anyMatch(a -> a.getId().equals(animalId));
    }

    /**
     * Returns whether this shelter has remaining capacity for at least one more animal.
     *
     * @return {@code true} if the shelter can accept more animals, {@code false} if at capacity
     */
    public boolean hasCapacity() {
        return animals.size() < capacity;
    }

    /**
     * Returns an unmodifiable view of all animals currently in this shelter.
     * The returned list reflects the state of the shelter at the time of the call;
     * callers must not attempt to modify the returned list.
     *
     * @return an unmodifiable {@link List} of {@link Animal} objects
     */
    public List<Animal> getAnimals() {
        return Collections.unmodifiableList(animals);
    }

    /**
     * Returns the unique identifier of this shelter.
     *
     * @return the UUID string identifying this shelter
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of this shelter.
     *
     * @return the shelter's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the physical location of this shelter.
     *
     * @return the shelter's location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the maximum number of animals this shelter can hold.
     *
     * @return the shelter's capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the current number of animals in this shelter.
     *
     * @return the current animal count, between 0 and {@link #getCapacity()} inclusive
     */
    public int getCurrentCount() {
        return animals.size();
    }

    /**
     * Returns a string representation of this shelter including its ID, name, location, and occupancy.
     *
     * @return a human-readable description of this shelter
     */
    @Override
    public String toString() {
        return "Shelter[id=" + id + ", name=" + name + ", location=" + location
                + ", capacity=" + capacity + ", currentCount=" + animals.size() + "]";
    }

    /**
     * Returns true if the given object is a Shelter with the same unique ID.
     * Shelter identity is determined solely by its UUID, consistent with entity semantics.
     *
     * @param o the object to compare
     * @return true if {@code o} is a Shelter with the same ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shelter)) return false;
        Shelter other = (Shelter) o;
        return Objects.equals(id, other.id);
    }

    /**
     * Returns a hash code based on this shelter's unique ID.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Compares this shelter to another by name in alphabetical order.
     * This natural ordering is useful for displaying shelters in sorted lists.
     *
     * @param other the other shelter to compare to
     * @return a negative number if this name comes first, positive if after, zero if equal
     */
    @Override
    public int compareTo(Shelter other) {
        return this.name.compareTo(other.name);
    }
}
