package shelter.domain;

import java.util.UUID;

/**
 * Abstract base class representing an animal available for adoption at a shelter.
 * Concrete subclasses (e.g., {@link Dog}, {@link Cat}, {@link Rabbit}) must implement
 * {@link #getSpecies()} to identify their species and may add species-specific attributes.
 */
public abstract class Animal {

    private final String id;
    private final String name;
    private final String breed;
    private final int age;
    private final ActivityLevel activityLevel;
    private boolean vaccinated;
    private String adopterId;

    /**
     * Constructs a new Animal with the given core attributes.
     * All parameters are required and validated; age must be non-negative.
     * The animal is initialized as available for adoption ({@code adopterId} is null).
     *
     * @param name          the animal's name; must not be null or blank
     * @param breed         the animal's breed; must not be null or blank
     * @param age           the animal's age in years; must be non-negative
     * @param activityLevel the animal's activity level; must not be null
     * @param vaccinated    whether the animal has been vaccinated
     * @throws IllegalArgumentException if any parameter is null, blank, or invalid
     */
    protected Animal(String name, String breed, int age,
                     ActivityLevel activityLevel, boolean vaccinated) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Animal name must not be null or blank.");
        }
        if (breed == null || breed.isBlank()) {
            throw new IllegalArgumentException("Animal breed must not be null or blank.");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Animal age must be non-negative.");
        }
        if (activityLevel == null) {
            throw new IllegalArgumentException("Activity level must not be null.");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.activityLevel = activityLevel;
        this.vaccinated = vaccinated;
        this.adopterId = null;
    }

    /**
     * Returns the species name of this animal (e.g., "Dog", "Cat", "Rabbit").
     * Each concrete subclass must provide its own immutable species identifier.
     *
     * @return the species name as a non-null, non-blank string
     */
    public abstract String getSpecies();

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
     * Returns the breed of this animal.
     *
     * @return the animal's breed
     */
    public String getBreed() {
        return breed;
    }

    /**
     * Returns the age of this animal in years.
     *
     * @return the animal's age, always non-negative
     */
    public int getAge() {
        return age;
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
     * Returns a string representation of this animal including its species, name, breed, and age.
     *
     * @return a human-readable description of this animal
     */
    @Override
    public String toString() {
        return getSpecies() + "[id=" + id + ", name=" + name + ", breed=" + breed
                + ", age=" + age + ", activity=" + activityLevel
                + ", vaccinated=" + vaccinated
                + ", adopterId=" + (adopterId != null ? adopterId : "none") + "]";
    }
}
