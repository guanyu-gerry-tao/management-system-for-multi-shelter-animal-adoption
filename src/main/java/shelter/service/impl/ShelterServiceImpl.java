package shelter.service.impl;

import shelter.domain.Shelter;
import shelter.exception.EntityNotFoundException;
import shelter.repository.ShelterRepository;
import shelter.service.ShelterService;

import java.util.List;

/**
 * Default implementation of {@link ShelterService} backed by a {@link ShelterRepository}.
 * Delegates all persistence operations to the repository and enforces business rules
 * such as duplicate-name prevention on registration.
 */
public class ShelterServiceImpl implements ShelterService {

    private final ShelterRepository shelterRepository;

    /**
     * Constructs a ShelterServiceImpl with the given repository.
     * The repository is used for all read and write operations on shelter records.
     *
     * @param shelterRepository the repository to delegate persistence to; must not be null
     * @throws IllegalArgumentException if {@code shelterRepository} is null
     */
    public ShelterServiceImpl(ShelterRepository shelterRepository) {
        if (shelterRepository == null) {
            throw new IllegalArgumentException("ShelterRepository must not be null.");
        }
        this.shelterRepository = shelterRepository;
    }

    /**
     * {@inheritDoc}
     * Throws {@link IllegalArgumentException} if a shelter with the same name and location already exists.
     */
    @Override
    public void register(Shelter shelter) {
        if (shelter == null) throw new IllegalArgumentException("Shelter must not be null.");
        // Prevent duplicate shelters with the same name and location
        boolean duplicate = shelterRepository.findAll().stream()
                .anyMatch(s -> s.getName().equals(shelter.getName())
                        && s.getLocation().equals(shelter.getLocation()));
        if (duplicate) {
            throw new IllegalArgumentException(
                    "A shelter named \"" + shelter.getName() + "\" at \"" + shelter.getLocation() + "\" already exists.");
        }
        shelterRepository.save(shelter);
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if the shelter does not exist in the repository.
     */
    @Override
    public void update(Shelter shelter) {
        if (shelter == null) throw new IllegalArgumentException("Shelter must not be null.");
        shelterRepository.findById(shelter.getId())
                .orElseThrow(() -> new EntityNotFoundException("Shelter not found: " + shelter.getId()));
        shelterRepository.save(shelter);
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if the shelter is not found.
     */
    @Override
    public void remove(Shelter shelter) {
        if (shelter == null) throw new IllegalArgumentException("Shelter must not be null.");
        shelterRepository.findById(shelter.getId())
                .orElseThrow(() -> new EntityNotFoundException("Shelter not found: " + shelter.getId()));
        // Business rule: shelter must be empty before removal
        if (!shelter.getAnimals().isEmpty()) {
            throw new IllegalStateException(
                    "Shelter \"" + shelter.getName() + "\" still holds " + shelter.getCurrentCount() + " animal(s).");
        }
        shelterRepository.delete(shelter.getId());
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if no shelter with the given ID exists.
     */
    @Override
    public Shelter findById(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Shelter ID must not be null or blank.");
        return shelterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shelter not found: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Shelter> listAll() {
        return shelterRepository.findAll();
    }
}
