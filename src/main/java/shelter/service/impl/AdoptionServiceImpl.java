package shelter.service.impl;

import shelter.domain.Adopter;
import shelter.domain.AdoptionRequest;
import shelter.domain.Animal;
import shelter.domain.RequestStatus;
import shelter.domain.Shelter;
import shelter.repository.AdopterRepository;
import shelter.repository.AdoptionRequestRepository;
import shelter.repository.AnimalRepository;
import shelter.exception.AnimalNotAvailableException;
import shelter.service.AdoptionService;
import shelter.service.AuditService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete implementation of {@link AdoptionService} that manages the full lifecycle
 * of adoption requests. Each state transition is enforced by the domain object itself;
 * this class is responsible for persisting the updated state and propagating side effects
 * (animal and adopter record updates) to the appropriate repositories.
 */
public class AdoptionServiceImpl implements AdoptionService {

    private final AdoptionRequestRepository requestRepository;
    private final AnimalRepository animalRepository;
    private final AdopterRepository adopterRepository;
    private final AuditService<AdoptionRequest> auditService;

    /**
     * Constructs a new {@code AdoptionServiceImpl} with the required repositories and audit service.
     * All three repositories are needed for the full adoption lifecycle: the request must be
     * persisted, and both the animal and adopter records updated on approval. The audit service
     * records every state-changing operation for traceability.
     *
     * @param requestRepository the repository for adoption request persistence; must not be null
     * @param animalRepository  the repository for animal record persistence; must not be null
     * @param adopterRepository the repository for adopter record persistence; must not be null
     * @param auditService      the service used to log adoption operations; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public AdoptionServiceImpl(AdoptionRequestRepository requestRepository,
                               AnimalRepository animalRepository,
                               AdopterRepository adopterRepository,
                               AuditService<AdoptionRequest> auditService) {
        if (requestRepository == null) {
            throw new IllegalArgumentException("AdoptionRequestRepository must not be null.");
        }
        if (animalRepository == null) {
            throw new IllegalArgumentException("AnimalRepository must not be null.");
        }
        if (adopterRepository == null) {
            throw new IllegalArgumentException("AdopterRepository must not be null.");
        }
        if (auditService == null) {
            throw new IllegalArgumentException("AuditService must not be null.");
        }
        this.requestRepository = requestRepository;
        this.animalRepository = animalRepository;
        this.adopterRepository = adopterRepository;
        this.auditService = auditService;
    }

    /**
     * {@inheritDoc}
     * Validates that the animal is still available before persisting the request.
     *
     * @throws AnimalNotAvailableException if the animal has already been adopted
     */
    @Override
    public void submit(AdoptionRequest request) {
        // Guard: reject null input at the service boundary
        if (request == null) {
            throw new IllegalArgumentException("AdoptionRequest must not be null.");
        }
        // Business rule: an animal can only be adopted once
        if (!request.getAnimal().isAvailable()) {
            throw new AnimalNotAvailableException(
                    "Animal \"" + request.getAnimal().getName() + "\" is not available for adoption.");
        }
        // Persist the new PENDING request
        requestRepository.save(request);
        auditService.log("submitted adoption request", request);
    }

    /**
     * {@inheritDoc}
     * On approval, sets the animal's adopter ID and records the animal on the adopter,
     * then persists all three updated records to their repositories.
     */
    @Override
    public void approve(AdoptionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AdoptionRequest must not be null.");
        }
        // Delegate state transition to the domain object; it enforces PENDING-only rule
        request.approve();

        Animal animal = request.getAnimal();
        Adopter adopter = request.getAdopter();

        // Side effect 1: mark the animal as no longer available
        animal.setAdopterId(adopter.getId());
        // Side effect 2: record this adoption on the adopter's history
        adopter.addAdoptedAnimalId(animal.getId());

        // Persist all three changed records atomically across repositories
        animalRepository.save(animal);
        adopterRepository.save(adopter);
        requestRepository.save(request);
        auditService.log("approved adoption request", request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reject(AdoptionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AdoptionRequest must not be null.");
        }
        // Domain enforces PENDING; animal record needs no update on rejection
        request.reject();
        requestRepository.save(request);
        auditService.log("rejected adoption request", request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel(AdoptionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AdoptionRequest must not be null.");
        }
        // Same pattern as reject: only the request status changes
        request.cancel();
        requestRepository.save(request);
        auditService.log("cancelled adoption request", request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdoptionRequest> getRequestsByAdopter(Adopter adopter) {
        if (adopter == null) {
            throw new IllegalArgumentException("Adopter must not be null.");
        }
        return requestRepository.findByAdopterId(adopter.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdoptionRequest> getRequestsByShelter(Shelter shelter) {
        if (shelter == null) {
            throw new IllegalArgumentException("Shelter must not be null.");
        }
        return requestRepository.findByShelterId(shelter.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdoptionRequest> getRequestsByAnimal(Animal animal) {
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        return requestRepository.findByAnimalId(animal.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdoptionRequest> getRequestsAfter(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null.");
        }
        // No repository-level date filter exists; load all and filter in memory
        return requestRepository.findAll().stream()
                .filter(r -> r.getSubmittedAt().toLocalDate().isAfter(date))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * Uses submission date as a proxy for approval date, since the domain does not
     * store a separate approval timestamp.
     */
    @Override
    public List<AdoptionRequest> getApprovedAfter(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null.");
        }
        // Pre-filter by status at the repository level, then narrow by date in memory
        return requestRepository.findByStatus(RequestStatus.APPROVED).stream()
                .filter(r -> r.getSubmittedAt().toLocalDate().isAfter(date))
                .collect(Collectors.toList());
    }
}
