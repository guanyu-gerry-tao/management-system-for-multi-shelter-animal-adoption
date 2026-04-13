package shelter.application;

import shelter.application.model.AnimalView;
import shelter.domain.ActivityLevel;
import shelter.domain.Animal;
import shelter.domain.Cat;
import shelter.domain.Dog;
import shelter.domain.Other;
import shelter.domain.Rabbit;

import java.time.LocalDate;
import java.util.List;

/**
 * Application service for animal management use cases.
 * Orchestrates shelter capacity checks, animal registration, updates, and removals,
 * ensuring audit logging is performed for every state-changing operation.
 */
public interface AnimalApplicationService {

    /**
     * Admits a new dog into the specified shelter with dog-specific attributes.
     * Throws an exception if the shelter is not found or is at full capacity.
     *
     * @param name          the dog's name; must not be null or blank
     * @param breed         the dog's breed; must not be null or blank
     * @param birthday      the dog's date of birth; must not be null
     * @param activityLevel the dog's activity level; must not be null
     * @param shelterId     the ID of the shelter to admit the dog into; must not be null or blank
     * @param size          the dog's size; must not be null
     * @param neutered      whether the dog is neutered
     * @return the newly created {@link Dog}
     */
    Dog admitDog(String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
                 String shelterId, Dog.Size size, boolean neutered);

    /**
     * Admits a new cat into the specified shelter with cat-specific attributes.
     * Throws an exception if the shelter is not found or is at full capacity.
     *
     * @param name          the cat's name; must not be null or blank
     * @param breed         the cat's breed; must not be null or blank
     * @param birthday      the cat's date of birth; must not be null
     * @param activityLevel the cat's activity level; must not be null
     * @param shelterId     the ID of the shelter to admit the cat into; must not be null or blank
     * @param indoor        whether the cat is an indoor cat
     * @param neutered      whether the cat is neutered
     * @return the newly created {@link Cat}
     */
    Cat admitCat(String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
                 String shelterId, boolean indoor, boolean neutered);

    /**
     * Admits a new rabbit into the specified shelter with rabbit-specific attributes.
     * Throws an exception if the shelter is not found or is at full capacity.
     *
     * @param name          the rabbit's name; must not be null or blank
     * @param breed         the rabbit's breed; must not be null or blank
     * @param birthday      the rabbit's date of birth; must not be null
     * @param activityLevel the rabbit's activity level; must not be null
     * @param shelterId     the ID of the shelter to admit the rabbit into; must not be null or blank
     * @param furLength     the rabbit's fur length; must not be null
     * @return the newly created {@link Rabbit}
     */
    Rabbit admitRabbit(String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
                       String shelterId, Rabbit.FurLength furLength);

    /**
     * Admits a new animal of an unclassified species into the specified shelter.
     * Use this method for animals that do not have a dedicated subclass (e.g., fish, parrot, iguana).
     * Throws an exception if the shelter is not found or is at full capacity.
     *
     * @param name          the animal's name; must not be null or blank
     * @param breed         the animal's breed or description; must not be null or blank
     * @param birthday      the animal's date of birth; must not be null
     * @param activityLevel the animal's activity level; must not be null
     * @param shelterId     the ID of the shelter to admit the animal into; must not be null or blank
     * @param speciesName   a free-form species description (e.g., "fish"); must not be null or blank
     * @return the newly created {@link Other}
     */
    Other admitOther(String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
                     String shelterId, String speciesName);

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
     * Updates an existing animal's mutable fields with the provided values.
     * Only non-null parameters are applied; omitted (null) fields retain their current values.
     * Breed and birthday are immutable and cannot be changed after admission.
     * The {@code neutered} flag is only applied when the animal is a {@link Dog} or {@link Cat};
     * passing a non-null value for other species has no effect.
     * Throws an exception if the animal is not found.
     *
     * @param animalId      the ID of the animal to update; must not be null or blank
     * @param name          the new name, or {@code null} to keep the current value
     * @param activityLevel the new activity level, or {@code null} to keep the current value
     * @param neutered      the new neutered status, or {@code null} to keep the current value
     * @return the updated {@link Animal}
     */
    Animal updateAnimal(String animalId, String name, ActivityLevel activityLevel, Boolean neutered);

    /**
     * Returns a list of {@link AnimalView} objects, each pairing an animal with its shelter's
     * display name. Filters by shelter when {@code shelterId} is provided, or returns all animals
     * system-wide when {@code shelterId} is {@code null}. The shelter name is resolved by the
     * Application layer so that callers do not need to perform cross-domain lookups.
     *
     * @param shelterId the ID of the shelter to filter by, or {@code null} for all animals
     * @return a list of enriched animal views matching the filter
     */
    List<AnimalView> listAnimalsWithShelterName(String shelterId);

    /**
     * Removes an animal from the system by ID.
     * Throws an exception if the animal is not found or has a pending adoption request.
     *
     * @param animalId the ID of the animal to remove; must not be null or blank
     */
    void removeAnimal(String animalId);
}
