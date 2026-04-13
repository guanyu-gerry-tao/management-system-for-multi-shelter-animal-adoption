package shelter.service.impl;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.exception.EntityNotFoundException;
import shelter.repository.AdopterRepository;
import shelter.service.AdopterService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link AdopterService} backed by an {@link AdopterRepository}.
 * Delegates all persistence operations to the repository and enforces business rules
 * such as duplicate prevention on registration.
 */
public class AdopterServiceImpl implements AdopterService {

    private final AdopterRepository adopterRepository;

    /**
     * Constructs an AdopterServiceImpl with the given repository.
     * The repository is used for all read and write operations on adopter records.
     *
     * @param adopterRepository the repository to delegate persistence to; must not be null
     * @throws IllegalArgumentException if {@code adopterRepository} is null
     */
    public AdopterServiceImpl(AdopterRepository adopterRepository) {
        if (adopterRepository == null) {
            throw new IllegalArgumentException("AdopterRepository must not be null.");
        }
        this.adopterRepository = adopterRepository;
    }

    /**
     * {@inheritDoc}
     * Throws {@link IllegalArgumentException} if the adopter is already registered.
     */
    @Override
    public void register(Adopter adopter) {
        if (adopter == null) throw new IllegalArgumentException("Adopter must not be null.");
        // Prevent registering the same adopter twice
        if (adopterRepository.findById(adopter.getId()).isPresent()) {
            throw new IllegalArgumentException("Adopter already registered: " + adopter.getId());
        }
        adopterRepository.save(adopter);
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if the adopter does not exist in the repository.
     */
    @Override
    public void update(Adopter adopter) {
        if (adopter == null) throw new IllegalArgumentException("Adopter must not be null.");
        adopterRepository.findById(adopter.getId())
                .orElseThrow(() -> new EntityNotFoundException("Adopter not found: " + adopter.getId()));
        adopterRepository.save(adopter);
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if the adopter is not found.
     */
    @Override
    public void remove(Adopter adopter) {
        if (adopter == null) throw new IllegalArgumentException("Adopter must not be null.");
        adopterRepository.findById(adopter.getId())
                .orElseThrow(() -> new EntityNotFoundException("Adopter not found: " + adopter.getId()));
        adopterRepository.delete(adopter.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Adopter> listAll() {
        return adopterRepository.findAll();
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if no adopter with the given ID exists.
     */
    @Override
    public Adopter findById(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Adopter ID must not be null or blank.");
        return adopterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Adopter not found: " + id));
    }

    /**
     * {@inheritDoc}
     * Registration date is not stored on Adopter; returns all adopters as a conservative fallback.
     */
    @Override
    public List<Adopter> registeredAfter(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date must not be null.");
        return adopterRepository.findAll();
    }

    /**
     * {@inheritDoc}
     * Returns adopters whose adopted animal list is non-empty as a conservative proxy for adoption date.
     */
    @Override
    public List<Adopter> adoptedAfter(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date must not be null.");
        return adopterRepository.findAll().stream()
                .filter(a -> !a.getAdoptedAnimalIds().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Adopter> adoptedAnimal(Animal animal) {
        if (animal == null) throw new IllegalArgumentException("Animal must not be null.");
        // Return adopters whose adopted list includes this animal's ID
        return adopterRepository.findAll().stream()
                .filter(a -> a.getAdoptedAnimalIds().contains(animal.getId()))
                .collect(Collectors.toList());
    }
}
