package shelter.service.impl;

import shelter.domain.VaccineType;
import shelter.exception.EntityNotFoundException;
import shelter.repository.VaccineTypeRepository;
import shelter.service.VaccineTypeCatalogService;

import java.util.List;

/**
 * Default implementation of {@link VaccineTypeCatalogService} backed by a {@link VaccineTypeRepository}.
 * Delegates all persistence operations to the repository and enforces catalog-level business rules
 * such as duplicate name prevention on add and update.
 */
public class VaccineTypeCatalogServiceImpl implements VaccineTypeCatalogService {

    private final VaccineTypeRepository vaccineTypeRepository;

    /**
     * Constructs a VaccineTypeCatalogServiceImpl with the given repository.
     * The repository is used for all read and write operations on vaccine type records.
     *
     * @param vaccineTypeRepository the repository to delegate persistence to; must not be null
     * @throws IllegalArgumentException if {@code vaccineTypeRepository} is null
     */
    public VaccineTypeCatalogServiceImpl(VaccineTypeRepository vaccineTypeRepository) {
        if (vaccineTypeRepository == null) {
            throw new IllegalArgumentException("VaccineTypeRepository must not be null.");
        }
        this.vaccineTypeRepository = vaccineTypeRepository;
    }

    /**
     * {@inheritDoc}
     * Throws {@link IllegalArgumentException} if a vaccine type with the same name already exists.
     */
    @Override
    public void add(VaccineType vaccineType) {
        if (vaccineType == null) throw new IllegalArgumentException("VaccineType must not be null.");
        // Prevent duplicate vaccine type names in the catalog
        if (vaccineTypeRepository.findByName(vaccineType.getName()).isPresent()) {
            throw new IllegalArgumentException("Vaccine type already exists: " + vaccineType.getName());
        }
        vaccineTypeRepository.save(vaccineType);
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if the vaccine type is not found.
     * Throws {@link IllegalArgumentException} if the new name conflicts with an existing entry.
     */
    @Override
    public void update(VaccineType vaccineType) {
        if (vaccineType == null) throw new IllegalArgumentException("VaccineType must not be null.");
        vaccineTypeRepository.findById(vaccineType.getId())
                .orElseThrow(() -> new EntityNotFoundException("VaccineType not found: " + vaccineType.getId()));
        // Guard against renaming to a name already used by a different entry
        vaccineTypeRepository.findByName(vaccineType.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(vaccineType.getId())) {
                throw new IllegalArgumentException("Vaccine type name already in use: " + vaccineType.getName());
            }
        });
        vaccineTypeRepository.save(vaccineType);
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if no vaccine type with the given ID exists.
     */
    @Override
    public void remove(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("VaccineType ID must not be null or blank.");
        vaccineTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("VaccineType not found: " + id));
        vaccineTypeRepository.delete(id);
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if no vaccine type with the given ID exists.
     */
    @Override
    public VaccineType findById(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("VaccineType ID must not be null or blank.");
        return vaccineTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("VaccineType not found: " + id));
    }

    /**
     * {@inheritDoc}
     * Throws {@link EntityNotFoundException} if no vaccine type with the given name exists.
     */
    @Override
    public VaccineType findByName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Vaccine type name must not be null or blank.");
        return vaccineTypeRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("VaccineType not found: " + name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VaccineType> listAll() {
        return vaccineTypeRepository.findAll();
    }
}
