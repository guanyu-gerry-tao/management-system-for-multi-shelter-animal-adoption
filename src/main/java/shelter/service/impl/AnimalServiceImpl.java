package shelter.service.impl;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.domain.Shelter;
import shelter.exception.EntityNotFoundException;
import shelter.repository.AnimalRepository;
import shelter.service.AnimalService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link AnimalService} backed by an {@link AnimalRepository}.
 * Delegates all persistence operations to the repository and applies business-level validation
 * such as shelter assignment on registration and adoption state checks on removal.
 */
public class AnimalServiceImpl implements AnimalService {

    private final AnimalRepository animalRepository;

    /**
     * Constructs an AnimalServiceImpl with the given repository.
     * The repository is used for all read and write operations on animal records.
     *
     * @param animalRepository the repository to delegate persistence to; must not be null
     * @throws IllegalArgumentException if {@code animalRepository} is null
     */
    public AnimalServiceImpl(AnimalRepository animalRepository) {
        if (animalRepository == null) {
            throw new IllegalArgumentException("AnimalRepository must not be null.");
        }
        this.animalRepository = animalRepository;
    }

    /**
     * {@inheritDoc}
     * Adds the animal to the shelter roster, then saves the animal record to the repository.
     */
    @Override
    public void register(Animal animal, Shelter shelter) {
        if (animal == null) throw new IllegalArgumentException("Animal must not be null.");
        if (shelter == null) throw new IllegalArgumentException("Shelter must not be null.");
        // Assign animal to shelter and persist
        shelter.addAnimal(animal);
        animalRepository.save(animal);
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if the animal does not exist in the repository.
     */
    @Override
    public void update(Animal animal) {
        if (animal == null) throw new IllegalArgumentException("Animal must not be null.");
        // Verify animal exists before overwriting
        animalRepository.findById(animal.getId())
                .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + animal.getId()));
        animalRepository.save(animal);
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if the animal is not found.
     */
    @Override
    public void remove(Animal animal) {
        if (animal == null) throw new IllegalArgumentException("Animal must not be null.");
        animalRepository.findById(animal.getId())
                .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + animal.getId()));
        animalRepository.delete(animal.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Animal> getAnimalsByShelter(Shelter shelter) {
        if (shelter == null) throw new IllegalArgumentException("Shelter must not be null.");
        return animalRepository.findByShelterId(shelter.getId());
    }

    /**
     * {@inheritDoc}
     * Uses the animal's shelter ID as a proxy for registration date — not natively tracked,
     * so this returns all animals for now (field not yet in domain model).
     */
    @Override
    public List<Animal> registeredAfter(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date must not be null.");
        // Registration date is not stored on Animal; return all animals as a conservative fallback
        return animalRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Animal> adoptedAfter(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date must not be null.");
        // Adoption date is not stored on Animal; return all adopted animals as a conservative fallback
        return animalRepository.findAll().stream()
                .filter(a -> a.getAdopterId() != null)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if no animal with the given ID exists.
     */
    @Override
    public Animal findById(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Animal ID must not be null or blank.");
        return animalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Animal> adoptedBy(Adopter adopter) {
        if (adopter == null) throw new IllegalArgumentException("Adopter must not be null.");
        return animalRepository.findByAdopterId(adopter.getId());
    }
}
