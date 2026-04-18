package shelter.application.impl;

import shelter.application.TransferApplicationService;
import shelter.domain.Animal;
import shelter.domain.Shelter;
import shelter.domain.TransferRequest;
import shelter.exception.AnimalNotAvailableException;
import shelter.exception.AnimalNotInShelterException;
import shelter.service.AnimalService;
import shelter.service.AuditService;
import shelter.service.RequestNotificationService;
import shelter.service.ShelterService;
import shelter.service.TransferService;

import java.util.List;

/**
 * Default implementation of {@link TransferApplicationService} that orchestrates
 * inter-shelter transfer requests across the service layer.
 * Validates animal availability and shelter membership before creating a request,
 * and dispatches notifications and audit entries for every operation.
 */
public class TransferApplicationServiceImpl implements TransferApplicationService {

    private final TransferService transferService;
    private final AnimalService animalService;
    private final ShelterService shelterService;
    private final RequestNotificationService notificationService;
    private final AuditService<TransferRequest> auditService;

    /**
     * Constructs a TransferApplicationServiceImpl with all required service dependencies.
     * All five services are mandatory; none may be null.
     *
     * @param transferService     the service managing transfer request state; must not be null
     * @param animalService       the service for animal lookups; must not be null
     * @param shelterService      the service for shelter lookups; must not be null
     * @param notificationService the service for dispatching status-change notifications; must not be null
     * @param auditService        the service for recording audit log entries; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public TransferApplicationServiceImpl(TransferService transferService,
                                           AnimalService animalService,
                                           ShelterService shelterService,
                                           RequestNotificationService notificationService,
                                           AuditService<TransferRequest> auditService) {
        if (transferService     == null) throw new IllegalArgumentException("TransferService must not be null.");
        if (animalService       == null) throw new IllegalArgumentException("AnimalService must not be null.");
        if (shelterService      == null) throw new IllegalArgumentException("ShelterService must not be null.");
        if (notificationService == null) throw new IllegalArgumentException("RequestNotificationService must not be null.");
        if (auditService        == null) throw new IllegalArgumentException("AuditService must not be null.");
        this.transferService     = transferService;
        this.animalService       = animalService;
        this.shelterService      = shelterService;
        this.notificationService = notificationService;
        this.auditService        = auditService;
    }

    /**
     * {@inheritDoc}
     * Verifies the animal is in the source shelter and is available before creating the request.
     */
    @Override
    public TransferRequest requestTransfer(String animalId, String fromShelterId, String toShelterId) {
        Animal animal    = animalService.findById(animalId);
        Shelter from     = shelterService.findById(fromShelterId);
        Shelter to       = shelterService.findById(toShelterId);

        // Animal must be in the source shelter
        if (!from.containsAnimal(animalId)) {
            throw new AnimalNotInShelterException(
                    "Animal " + animalId + " is not in shelter " + fromShelterId);
        }

        // Animal must be available (not adopted) to be transferred
        if (!animal.isAvailable()) {
            throw new AnimalNotAvailableException("Animal " + animalId + " is not available for transfer.");
        }

        TransferRequest request = transferService.requestTransfer(animal, from, to);
        notificationService.notifyTransferStatusChange(request);
        auditService.log("requested transfer", request);
        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void approveTransfer(String requestId) {
        TransferRequest request = findRequestById(requestId);
        transferService.approve(request);
        notificationService.notifyTransferStatusChange(request);
        auditService.log("approved transfer", request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rejectTransfer(String requestId) {
        TransferRequest request = findRequestById(requestId);
        transferService.reject(request);
        notificationService.notifyTransferStatusChange(request);
        auditService.log("rejected transfer", request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelTransfer(String requestId) {
        TransferRequest request = findRequestById(requestId);
        transferService.dismiss(request);
        auditService.log("cancelled transfer", request);
    }

    /**
     * {@inheritDoc}
     * Delegates to the underlying {@link shelter.service.TransferService}.
     */
    @Override
    public List<TransferRequest> listAllTransfers() {
        return transferService.listAll();
    }

    /**
     * Looks up a TransferRequest by ID by scanning all pending requests across all shelters.
     * Throws {@link shelter.exception.EntityNotFoundException} if not found.
     *
     * @param requestId the ID of the request to find
     * @return the matching TransferRequest
     */
    private TransferRequest findRequestById(String requestId) {
        return shelterService.listAll().stream()
                .flatMap(s -> transferService.getPendingRequests(s).stream())
                .filter(r -> r.getId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new shelter.exception.EntityNotFoundException(
                        "TransferRequest not found: " + requestId));
    }
}
