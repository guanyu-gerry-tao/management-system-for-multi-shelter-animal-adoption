package shelter.application.impl;

import shelter.application.AnimalApplicationService;
import shelter.domain.ActivityLevel;
import shelter.domain.Animal;
import shelter.domain.Cat;
import shelter.domain.Dog;
import shelter.domain.Other;
import shelter.domain.Rabbit;
import shelter.domain.Shelter;
import shelter.service.AnimalService;
import shelter.service.AuditService;
import shelter.service.ShelterService;

import java.time.LocalDate;
import java.util.List;

/**
 * Default implementation of {@link AnimalApplicationService} that orchestrates
 * animal admission, listing, updates, and removal across the service layer.
 * Each admit method constructs the correct Animal subtype with its species-specific attributes,
 * and records an audit entry for every mutating operation.
 */
public class AnimalApplicationServiceImpl implements AnimalApplicationService {

    private final AnimalService animalService;
    private final ShelterService shelterService;
    private final AuditService<Animal> auditService;

    /**
     * Constructs an AnimalApplicationServiceImpl with the required service dependencies.
     * All three services are mandatory; none may be null.
     *
     * @param animalService  the service for animal persistence; must not be null
     * @param shelterService the service for shelter lookups and capacity checks; must not be null
     * @param auditService   the service for recording audit log entries; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public AnimalApplicationServiceImpl(AnimalService animalService,
                                        ShelterService shelterService,
                                        AuditService<Animal> auditService) {
        if (animalService == null)  throw new IllegalArgumentException("AnimalService must not be null.");
        if (shelterService == null) throw new IllegalArgumentException("ShelterService must not be null.");
        if (auditService == null)   throw new IllegalArgumentException("AuditService must not be null.");
        this.animalService  = animalService;
        this.shelterService = shelterService;
        this.auditService   = auditService;
    }

    /**
     * {@inheritDoc}
     * Constructs a Dog with the given dog-specific attributes, assigns it to the shelter,
     * and persists the record. Throws if the shelter is not found.
     */
    @Override
    public Dog admitDog(String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
                        String shelterId, Dog.Size size, boolean neutered) {
        Shelter shelter = shelterService.findById(shelterId);

        // Construct the dog and assign it to the shelter
        Dog dog = new Dog(name, breed, birthday, activityLevel, false, size, neutered);
        dog.setShelterId(shelterId);

        animalService.register(dog, shelter);
        auditService.log("admitted animal", dog);
        return dog;
    }

    /**
     * {@inheritDoc}
     * Constructs a Cat with the given cat-specific attributes, assigns it to the shelter,
     * and persists the record. Throws if the shelter is not found.
     */
    @Override
    public Cat admitCat(String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
                        String shelterId, boolean indoor, boolean neutered) {
        Shelter shelter = shelterService.findById(shelterId);

        // Construct the cat and assign it to the shelter
        Cat cat = new Cat(name, breed, birthday, activityLevel, false, indoor, neutered);
        cat.setShelterId(shelterId);

        animalService.register(cat, shelter);
        auditService.log("admitted animal", cat);
        return cat;
    }

    /**
     * {@inheritDoc}
     * Constructs a Rabbit with the given rabbit-specific attributes, assigns it to the shelter,
     * and persists the record. Throws if the shelter is not found.
     */
    @Override
    public Rabbit admitRabbit(String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
                               String shelterId, Rabbit.FurLength furLength) {
        Shelter shelter = shelterService.findById(shelterId);

        // Construct the rabbit and assign it to the shelter
        Rabbit rabbit = new Rabbit(name, breed, birthday, activityLevel, false, furLength);
        rabbit.setShelterId(shelterId);

        animalService.register(rabbit, shelter);
        auditService.log("admitted animal", rabbit);
        return rabbit;
    }

    /**
     * {@inheritDoc}
     * Constructs an Other animal with the given free-form species name, assigns it to the shelter,
     * and persists the record. Throws if the shelter is not found.
     */
    @Override
    public Other admitOther(String name, String breed, LocalDate birthday, ActivityLevel activityLevel,
                             String shelterId, String speciesName) {
        Shelter shelter = shelterService.findById(shelterId);

        // Construct the other animal and assign it to the shelter
        Other other = new Other(name, breed, birthday, activityLevel, false, speciesName);
        other.setShelterId(shelterId);

        animalService.register(other, shelter);
        auditService.log("admitted animal", other);
        return other;
    }

    /**
     * {@inheritDoc}
     * Returns all animals in the specified shelter, or all animals system-wide if {@code shelterId} is null.
     */
    @Override
    public List<Animal> listAnimals(String shelterId) {
        if (shelterId == null) {
            // No shelter filter — return all animals system-wide
            return animalService.registeredAfter(java.time.LocalDate.of(1900, 1, 1));
        }
        Shelter shelter = shelterService.findById(shelterId);
        return animalService.getAnimalsByShelter(shelter);
    }

    /**
     * {@inheritDoc}
     * Fetches the current animal, applies only the non-null fields via setters, then persists.
     * Breed and birthday are immutable; only name and activity level can be changed.
     */
    @Override
    public Animal updateAnimal(String animalId, String name, ActivityLevel activityLevel) {
        Animal existing = animalService.findById(animalId);

        // Apply only the provided (non-null) fields via setters
        if (name != null) existing.setName(name);
        if (activityLevel != null) existing.setActivityLevel(activityLevel);

        animalService.update(existing);
        auditService.log("updated animal", existing);
        return existing;
    }

    /**
     * {@inheritDoc}
     * Throws if the animal is not found.
     */
    @Override
    public void removeAnimal(String animalId) {
        Animal animal = animalService.findById(animalId);
        animalService.remove(animal);
        auditService.log("removed animal", animal);
    }
}
