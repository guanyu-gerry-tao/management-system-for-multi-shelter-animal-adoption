package shelter.application.impl;

import shelter.application.AdopterApplicationService;
import shelter.domain.ActivityLevel;
import shelter.domain.Adopter;
import shelter.domain.AdopterPreferences;
import shelter.domain.DailySchedule;
import shelter.domain.LivingSpace;
import shelter.domain.Species;
import shelter.domain.AdoptionRequest;
import shelter.domain.RequestStatus;
import shelter.service.AdopterService;
import shelter.service.AdoptionService;
import shelter.service.AuditService;

import java.util.List;

/**
 * Default implementation of {@link AdopterApplicationService} that orchestrates
 * adopter registration, listing, updates, and removal across the service layer.
 * Constructs {@link Adopter} and {@link AdopterPreferences} from raw parameters,
 * and records an audit entry for every mutating operation.
 */
public class AdopterApplicationServiceImpl implements AdopterApplicationService {

    private final AdopterService adopterService;
    private final AdoptionService adoptionService;
    private final AuditService<Adopter> auditService;

    /**
     * Constructs an AdopterApplicationServiceImpl with the required service dependencies.
     * All three services are mandatory; none may be null.
     *
     * @param adopterService  the service for adopter persistence; must not be null
     * @param adoptionService the service for querying adoption requests; must not be null
     * @param auditService    the service for recording audit log entries; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public AdopterApplicationServiceImpl(AdopterService adopterService,
                                          AdoptionService adoptionService,
                                          AuditService<Adopter> auditService) {
        if (adopterService == null)  throw new IllegalArgumentException("AdopterService must not be null.");
        if (adoptionService == null) throw new IllegalArgumentException("AdoptionService must not be null.");
        if (auditService == null)    throw new IllegalArgumentException("AuditService must not be null.");
        this.adopterService  = adopterService;
        this.adoptionService = adoptionService;
        this.auditService    = auditService;
    }

    /**
     * {@inheritDoc}
     * Constructs an {@link AdopterPreferences} and {@link Adopter} from the provided parameters, then registers.
     */
    @Override
    public Adopter registerAdopter(String name, LivingSpace livingSpace, DailySchedule dailySchedule,
                                    Species preferredSpecies, String preferredBreed,
                                    ActivityLevel preferredActivityLevel, Boolean requiresVaccinated,
                                    Integer minAge, Integer maxAge) {
        // Build preferences object from individual fields
        AdopterPreferences preferences = new AdopterPreferences(
                preferredSpecies, preferredBreed, preferredActivityLevel,
                requiresVaccinated, minAge, maxAge);
        Adopter adopter = new Adopter(name, livingSpace, dailySchedule, null, preferences);
        adopterService.register(adopter);
        auditService.log("registered adopter", adopter);
        return adopter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Adopter> listAdopters() {
        return adopterService.listAll();
    }

    /**
     * {@inheritDoc}
     * Fetches the current adopter, merges only the non-null fields, then rebuilds and persists.
     */
    @Override
    public Adopter updateAdopter(String adopterId, String name, LivingSpace livingSpace,
                                  DailySchedule dailySchedule, Species preferredSpecies,
                                  String preferredBreed, ActivityLevel preferredActivityLevel,
                                  Boolean requiresVaccinated, Integer minAge, Integer maxAge) {
        Adopter existing = adopterService.findById(adopterId);

        // Merge only provided (non-null) fields; fall back to current values
        String       newName     = name          != null ? name          : existing.getName();
        LivingSpace  newSpace    = livingSpace    != null ? livingSpace   : existing.getLivingSpace();
        DailySchedule newSched   = dailySchedule  != null ? dailySchedule : existing.getDailySchedule();
        AdopterPreferences oldPrefs = existing.getPreferences();
        Species      newSpecies  = preferredSpecies       != null ? preferredSpecies       : oldPrefs.getPreferredSpecies();
        String       newBreed    = preferredBreed         != null ? preferredBreed         : oldPrefs.getPreferredBreed();
        ActivityLevel newActivity = preferredActivityLevel != null ? preferredActivityLevel : oldPrefs.getPreferredActivityLevel();
        Boolean      newRequiresVaccinated =
                requiresVaccinated != null ? requiresVaccinated : oldPrefs.getRequiresVaccinated();
        Integer      newMin      = minAge != null ? minAge : oldPrefs.getMinAge();
        Integer      newMax      = maxAge != null ? maxAge : oldPrefs.getMaxAge();

        AdopterPreferences newPrefs = new AdopterPreferences(
                newSpecies, newBreed, newActivity, newRequiresVaccinated, newMin, newMax);

        // Reconstruct adopter with updated values, preserving ID and adopted animal list
        Adopter updated = new Adopter(existing.getId(), newName, newSpace, newSched,
                existing.getPersonalNotes(), newPrefs, existing.getAdoptedAnimalIds());

        adopterService.update(updated);
        auditService.log("updated adopter", updated);
        return updated;
    }

    /**
     * {@inheritDoc}
     * Throws if the adopter is not found or has a pending adoption request.
     */
    @Override
    public void removeAdopter(String adopterId) {
        Adopter adopter = adopterService.findById(adopterId);

        // Guard: an adopter with a pending adoption request cannot be removed
        boolean hasPending = adoptionService.getRequestsByAdopter(adopter).stream()
                .anyMatch(r -> r.getStatus() == RequestStatus.PENDING);
        if (hasPending) {
            throw new IllegalStateException(
                    "Cannot remove adopter with a pending adoption request: " + adopterId);
        }

        adopterService.remove(adopter);
        auditService.log("removed adopter", adopter);
    }
}
