package shelter.domain;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class representing an animal available for adoption at a shelter.
 * Concrete subclasses (e.g., {@link Dog}, {@link Cat}, {@link Rabbit}) must implement
 * {@link #getSpecies()} to identify their species and may add species-specific attributes.
 */
public abstract class Animal implements Comparable<Animal> {

    private final String id;
    private String name;
    private final String breed;
    private final LocalDate birthday;
    private ActivityLevel activityLevel;
    private boolean vaccinated;
    private String adopterId;
    private String shelterId;

    /**
     * Reconstruction constructor for deserializing an Animal from persistent storage.
     * This constructor accepts an explicit {@code id} so that the original identifier
     * is preserved when reloading from CSV or other external sources.
     *
     * @param id            the pre-existing unique identifier; must not be null or blank
     * @param name          the animal's name; must not be null or blank
     * @param breed         the animal's breed; must not be null or blank
     * @param birthday      the animal's date of birth; must not be null
     * @param activityLevel the animal's activity level; must not be null
     * @param vaccinated    whether the animal has been vaccinated
     * @param adopterId     the ID of the adopter who adopted this animal, or {@code null} if available
     * @param shelterId     the ID of the shelter this animal belongs to, or {@code null} if unassigned
     * @throws IllegalArgumentException if any required parameter is null, blank, or invalid
     */
    protected Animal(String id, String name, String breed, LocalDate birthday,
                     ActivityLevel activityLevel, boolean vaccinated,
                     String adopterId, String shelterId) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Animal ID must not be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Animal name must not be null or blank.");
        }
        if (breed == null || breed.isBlank()) {
            throw new IllegalArgumentException("Animal breed must not be null or blank.");
        }
        if (birthday == null) {
            throw new IllegalArgumentException("Animal birthday must not be null.");
        }
        if (activityLevel == null) {
            throw new IllegalArgumentException("Activity level must not be null.");
        }
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.birthday = birthday;
        this.activityLevel = activityLevel;
        this.vaccinated = vaccinated;
        this.adopterId = adopterId;
        this.shelterId = shelterId;
    }

    /**
     * Copy constructor that creates a new Animal with all field values copied from {@code other}.
     * The copy shares the same {@code id} so it represents the same entity in persistent storage.
     * Mutable fields ({@code vaccinated}, {@code adopterId}, {@code shelterId}) are copied by value.
     *
     * @param other the Animal instance to copy; must not be null
     * @throws IllegalArgumentException if {@code other} is null
     */
    protected Animal(Animal other) {
        this(other.id, other.name, other.breed, other.birthday, other.activityLevel,
                other.vaccinated, other.adopterId, other.shelterId);
    }

    /**
     * Constructs a new Animal with the given core attributes.
     * All parameters are required and validated; birthday must not be null.
     * The animal is initialized as available for adoption ({@code adopterId} is null).
     *
     * @param name          the animal's name; must not be null or blank
     * @param breed         the animal's breed; must not be null or blank
     * @param birthday      the animal's date of birth; must not be null
     * @param activityLevel the animal's activity level; must not be null
     * @param vaccinated    whether the animal has been vaccinated
     * @throws IllegalArgumentException if any parameter is null, blank, or invalid
     */
    protected Animal(String name, String breed, LocalDate birthday,
                     ActivityLevel activityLevel, boolean vaccinated) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Animal name must not be null or blank.");
        }
        if (breed == null || breed.isBlank()) {
            throw new IllegalArgumentException("Animal breed must not be null or blank.");
        }
        if (birthday == null) {
            throw new IllegalArgumentException("Animal birthday must not be null.");
        }
        if (activityLevel == null) {
            throw new IllegalArgumentException("Activity level must not be null.");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.breed = breed;
        this.birthday = birthday;
        this.activityLevel = activityLevel;
        this.vaccinated = vaccinated;
        this.adopterId = null;
    }

    /**
     * Returns the species of this animal.
     * Each concrete subclass must return its own immutable {@link Species} constant.
     *
     * @return the {@link Species} of this animal
     */
    public abstract Species getSpecies();

    /**
     * Returns whether this animal is eligible for the matching system.
     * Returns {@code true} by default; subclasses that cannot be matched (e.g., {@code Other})
     * should override this to return {@code false}.
     *
     * @return {@code true} if this animal can be matched with adopters
     */
    public boolean isMatchable() {
        return true;
    }

    /**
     * Returns the unique identifier of this animal.
     * The ID is generated automatically at construction time and never changes.
     *
     * @return the UUID string identifying this animal
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of this animal.
     *
     * @return the animal's name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the name of this animal.
     * The new name must not be null or blank.
     *
     * @param name the new name; must not be null or blank
     * @throws IllegalArgumentException if {@code name} is null or blank
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Animal name must not be null or blank.");
        }
        this.name = name;
    }

    /**
     * Returns the breed of this animal.
     *
     * @return the animal's breed
     */
    public String getBreed() {
        return breed;
    }

    /**
     * Returns the date of birth of this animal.
     * The birthday is immutable and set at construction time.
     *
     * @return the animal's birthday as a {@link LocalDate}
     */
    public LocalDate getBirthday() {
        return birthday;
    }

    /**
     * Returns the age of this animal in whole years, calculated from birthday to today.
     * The result is always non-negative and reflects the current date.
     *
     * @return the animal's age in years
     */
    public int getAge() {
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    /**
     * Returns the activity level of this animal.
     *
     * @return the animal's {@link ActivityLevel}
     */
    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    /**
     * Updates the activity level of this animal.
     * The new activity level must not be null.
     *
     * @param activityLevel the new activity level; must not be null
     * @throws IllegalArgumentException if {@code activityLevel} is null
     */
    public void setActivityLevel(ActivityLevel activityLevel) {
        if (activityLevel == null) {
            throw new IllegalArgumentException("Activity level must not be null.");
        }
        this.activityLevel = activityLevel;
    }

    /**
     * Returns whether this animal has been vaccinated.
     *
     * @return {@code true} if the animal is vaccinated, {@code false} otherwise
     */
    public boolean isVaccinated() {
        return vaccinated;
    }

    /**
     * Updates the vaccination status of this animal.
     * Typically called by {@code VaccinationService} after administering vaccines.
     *
     * @param vaccinated the new vaccination status
     */
    public void setVaccinated(boolean vaccinated) {
        this.vaccinated = vaccinated;
    }

    /**
     * Returns whether this animal is currently available for adoption.
     * An animal is available if it has not yet been adopted by anyone.
     *
     * @return {@code true} if no adopter has claimed this animal, {@code false} otherwise
     */
    public boolean isAvailable() {
        return adopterId == null;
    }

    /**
     * Returns the ID of the adopter who adopted this animal, or {@code null} if not yet adopted.
     *
     * @return the adopter's ID string, or {@code null}
     */
    public String getAdopterId() {
        return adopterId;
    }

    /**
     * Returns the ID of the shelter this animal currently belongs to, or {@code null} if unassigned.
     * This field is the persistent reference used to associate an animal with its shelter.
     *
     * @return the shelter's ID string, or {@code null}
     */
    public String getShelterId() {
        return shelterId;
    }

    /**
     * Assigns this animal to the shelter with the given ID.
     * Typically called by {@code AnimalService} when an animal is admitted or transferred.
     *
     * @param shelterId the ID of the shelter; must not be null or blank
     * @throws IllegalArgumentException if {@code shelterId} is null or blank
     */
    public void setShelterId(String shelterId) {
        if (shelterId == null || shelterId.isBlank()) {
            throw new IllegalArgumentException("Shelter ID must not be null or blank.");
        }
        this.shelterId = shelterId;
    }

    /**
     * Records that this animal has been adopted by the given adopter.
     * Typically called by {@code AdoptionService} when an adoption request is approved.
     *
     * @param adopterId the ID of the adopter; must not be null or blank
     * @throws IllegalArgumentException if {@code adopterId} is null or blank
     */
    public void setAdopterId(String adopterId) {
        if (adopterId == null || adopterId.isBlank()) {
            throw new IllegalArgumentException("Adopter ID must not be null or blank.");
        }
        this.adopterId = adopterId;
    }

    /**
     * Compares this animal to another by name alphabetically.
     * Animals with the same name are considered equal for ordering purposes.
     *
     * @param other the other Animal to compare to
     * @return a negative number if this name comes first, positive if later, zero if equal
     */
    @Override
    public int compareTo(Animal other) {
        return this.name.compareTo(other.name);
    }

    /**
     * Returns true if the given object is an Animal with the same ID.
     * Animal identity is determined solely by UUID since names and other attributes are mutable.
     *
     * @param o the object to compare
     * @return true if {@code o} is an Animal with the same ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Animal)) return false;
        Animal other = (Animal) o;
        return Objects.equals(id, other.id);
    }

    /**
     * Returns a hash code based on this animal's unique ID.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this animal including its species, name, breed, and birthday.
     *
     * @return a human-readable description of this animal
     */
    @Override
    public String toString() {
        return getSpecies() + "[id=" + id + ", name=" + name + ", breed=" + breed
                + ", birthday=" + birthday + ", activity=" + activityLevel
                + ", vaccinated=" + vaccinated
                + ", adopterId=" + (adopterId != null ? adopterId : "none") + "]";
    }
}
