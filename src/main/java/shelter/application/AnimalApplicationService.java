package shelter.application;

import shelter.domain.Animal;
import shelter.domain.ActivityLevel;

import java.util.List;

/**
 * Application service for animal management use cases.
 * Orchestrates shelter capacity checks, animal registration, updates, and removals,
 * ensuring audit logging is performed for every state-changing operation.
 */
public interface AnimalApplicationService {

    /**
     * Admits a new animal into the specified shelter.
     * Throws an exception if the shelter is not found or is at full capacity.
     *
     * @param species       the species of the animal (e.g., "dog", "cat", "rabbit"); must not be null or blank
     * @param name          the animal's name; must not be null or blank
     * @param breed         the animal's breed; must not be null or blank
     * @param age           the animal's age in years; must be non-negative
     * @param activityLevel the animal's activity level; must not be null
     * @param shelterId     the ID of the shelter to admit the animal into; must not be null or blank
     * @return the newly created {@link Animal}
     */
    Animal admitAnimal(String species, String name, String breed, int age,
                       ActivityLevel activityLevel, String shelterId);

    /**
     * Returns a list of animals, optionally filtered by shelter.
     * If {@code shelterId} is {@code null}, all animals in the system are returned.
     * Returns an empty list if no matching animals are found.
     *
     * @param shelterId the ID of the shelter to filter by, or {@code null} for all animals
     * @return a list of animals matching the filter
     */
    List<Animal> listAnimals(String shelterId);

    /**
     * Updates an existing animal's information with the provided values.
     * Only non-null parameters are applied; omitted (null) fields retain their current values.
     * Throws an exception if the animal is not found.
     *
     * @param animalId      the ID of the animal to update; must not be null or blank
     * @param name          the new name, or {@code null} to keep the current value
     * @param breed         the new breed, or {@code null} to keep the current value
     * @param age           the new age, or {@code null} to keep the current value
     * @param activityLevel the new activity level, or {@code null} to keep the current value
     * @return the updated {@link Animal}
     */
    Animal updateAnimal(String animalId, String name, String breed,
                        Integer age, ActivityLevel activityLevel);

    /**
     * Removes an animal from the system by ID.
     * Throws an exception if the animal is not found or has a pending adoption request.
     *
     * @param animalId the ID of the animal to remove; must not be null or blank
     */
    void removeAnimal(String animalId);
}
