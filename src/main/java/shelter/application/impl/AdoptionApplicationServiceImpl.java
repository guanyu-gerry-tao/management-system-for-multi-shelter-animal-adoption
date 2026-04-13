package shelter.application.impl;

import shelter.application.AdoptionApplicationService;
import shelter.domain.Adopter;
import shelter.domain.AdoptionRequest;
import shelter.domain.Animal;
import shelter.exception.AnimalNotAvailableException;
import shelter.service.AdopterService;
import shelter.service.AdoptionService;
import shelter.service.AnimalService;
import shelter.service.AuditService;
import shelter.service.RequestNotificationService;

/**
 * Default implementation of {@link AdoptionApplicationService} that orchestrates
 * the full adoption request lifecycle: submit, approve, reject, and cancel.
 * On approval, updates both the animal's adopter reference and the adopter's history.
 * Dispatches notifications and records audit entries for every operation.
 */
public class AdoptionApplicationServiceImpl implements AdoptionApplicationService {

    private final AdoptionService adoptionService;
    private final AnimalService animalService;
    private final AdopterService adopterService;
    private final RequestNotificationService notificationService;
    private final AuditService<AdoptionRequest> auditService;

    /**
     * Constructs an AdoptionApplicationServiceImpl with all required service dependencies.
     * All five services are mandatory; none may be null.
     *
     * @param adoptionService     the service managing adoption request state; must not be null
     * @param animalService       the service for animal lookups and updates; must not be null
     * @param adopterService      the service for adopter lookups and updates; must not be null
     * @param notificationService the service for dispatching status-change notifications; must not be null
     * @param auditService        the service for recording audit log entries; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public AdoptionApplicationServiceImpl(AdoptionService adoptionService,
                                           AnimalService animalService,
                                           AdopterService adopterService,
                                           RequestNotificationService notificationService,
                                           AuditService<AdoptionRequest> auditService) {
        if (adoptionService     == null) throw new IllegalArgumentException("AdoptionService must not be null.");
        if (animalService       == null) throw new IllegalArgumentException("AnimalService must not be null.");
        if (adopterService      == null) throw new IllegalArgumentException("AdopterService must not be null.");
        if (notificationService == null) throw new IllegalArgumentException("RequestNotificationService must not be null.");
        if (auditService        == null) throw new IllegalArgumentException("AuditService must not be null.");
        this.adoptionService     = adoptionService;
        this.animalService       = animalService;
        this.adopterService      = adopterService;
        this.notificationService = notificationService;
        this.auditService        = auditService;
    }

    /**
     * {@inheritDoc}
     * Verifies the animal is available before creating and submitting the request.
     */
    @Override
    public AdoptionRequest submitRequest(String adopterId, String animalId) {
        Adopter adopter = adopterService.findById(adopterId);
        Animal animal   = animalService.findById(animalId);

        // Animal must be available (not already adopted) before a request can be submitted
        if (!animal.isAvailable()) {
            throw new AnimalNotAvailableException("Animal " + animalId + " is not available for adoption.");
        }

        AdoptionRequest request = new AdoptionRequest(adopter, animal);
        adoptionService.submit(request);
        auditService.log("submitted adoption request", request);
        return request;
    }

    /**
     * {@inheritDoc}
     * Marks the animal as adopted and records the adoption in the adopter's history.
     */
    @Override
    public void approveRequest(String requestId) {
        AdoptionRequest request = findRequestById(requestId);
        adoptionService.approve(request);

        // Update animal and adopter domain state after approval
        Animal animal   = animalService.findById(request.getAnimal().getId());
        Adopter adopter = adopterService.findById(request.getAdopter().getId());
        animal.setAdopterId(adopter.getId());
        adopter.addAdoptedAnimalId(animal.getId());
        animalService.update(animal);
        adopterService.update(adopter);

        notificationService.notifyAdoptionStatusChange(request);
        auditService.log("approved adoption request", request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rejectRequest(String requestId) {
        AdoptionRequest request = findRequestById(requestId);
        adoptionService.reject(request);
        notificationService.notifyAdoptionStatusChange(request);
        auditService.log("rejected adoption request", request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelRequest(String requestId) {
        AdoptionRequest request = findRequestById(requestId);
        adoptionService.cancel(request);
        notificationService.notifyAdoptionStatusChange(request);
        auditService.log("cancelled adoption request", request);
    }

    /**
     * Looks up an AdoptionRequest by its ID by scanning all requests in the system.
     * Throws {@link shelter.exception.EntityNotFoundException} if not found.
     *
     * @param requestId the ID of the request to find
     * @return the matching AdoptionRequest
     */
    private AdoptionRequest findRequestById(String requestId) {
        return adopterService.listAll().stream()
                .flatMap(a -> adoptionService.getRequestsByAdopter(a).stream())
                .filter(r -> r.getId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new shelter.exception.EntityNotFoundException("Request not found: " + requestId));
    }

}

