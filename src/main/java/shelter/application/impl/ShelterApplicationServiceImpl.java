package shelter.application.impl;

import shelter.application.ShelterApplicationService;
import shelter.domain.Shelter;
import shelter.service.AuditService;
import shelter.service.ShelterService;

import java.util.List;

/**
 * Default implementation of {@link ShelterApplicationService} that orchestrates
 * shelter registration, updates, removal, and listing across the service layer.
 * Every mutating operation records an audit entry via {@link AuditService}.
 */
public class ShelterApplicationServiceImpl implements ShelterApplicationService {

    private final ShelterService shelterService;
    private final AuditService<Shelter> auditService;

    /**
     * Constructs a ShelterApplicationServiceImpl with the required service dependencies.
     * Both services are mandatory; neither may be null.
     *
     * @param shelterService the service used to persist and retrieve shelter records; must not be null
     * @param auditService   the service used to record audit log entries; must not be null
     * @throws IllegalArgumentException if either argument is null
     */
    public ShelterApplicationServiceImpl(ShelterService shelterService,
                                          AuditService<Shelter> auditService) {
        if (shelterService == null) throw new IllegalArgumentException("ShelterService must not be null.");
        if (auditService == null) throw new IllegalArgumentException("AuditService must not be null.");
        this.shelterService = shelterService;
        this.auditService = auditService;
    }

    /**
     * {@inheritDoc}
     * Constructs a new Shelter and delegates registration to {@link ShelterService}.
     */
    @Override
    public Shelter registerShelter(String name, String location, int capacity) {
        // Construct and register the new shelter
        Shelter shelter = new Shelter(name, location, capacity);
        shelterService.register(shelter);
        auditService.log("registered shelter", shelter);
        return shelter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Shelter> listShelters() {
        return shelterService.listAll();
    }

    /**
     * {@inheritDoc}
     * Fetches the current shelter, applies only the non-null fields, then persists the update.
     */
    @Override
    public Shelter updateShelter(String shelterId, String name, String location, Integer capacity) {
        // Load existing shelter, then merge only the provided (non-null) fields
        Shelter existing = shelterService.findById(shelterId);
        String newName     = name     != null ? name     : existing.getName();
        String newLocation = location != null ? location : existing.getLocation();
        int    newCapacity = capacity != null ? capacity : existing.getCapacity();

        // Reconstruct with updated values (Shelter fields are final, so a new instance is needed)
        Shelter updated = new Shelter(existing.getId(), newName, newLocation, newCapacity);
        existing.getAnimals().forEach(updated::addAnimal);

        shelterService.update(updated);
        auditService.log("updated shelter", updated);
        return updated;
    }

    /**
     * {@inheritDoc}
     * Throws if the shelter still holds animals or does not exist.
     */
    @Override
    public void removeShelter(String shelterId) {
        Shelter shelter = shelterService.findById(shelterId);
        shelterService.remove(shelter);
        auditService.log("removed shelter", shelter);
    }
}
