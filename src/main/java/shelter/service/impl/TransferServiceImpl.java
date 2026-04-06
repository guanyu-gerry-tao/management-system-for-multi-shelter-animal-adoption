package shelter.service.impl;

import shelter.domain.Animal;
import shelter.domain.RequestStatus;
import shelter.domain.Shelter;
import shelter.domain.TransferRequest;
import shelter.repository.AnimalRepository;
import shelter.repository.ShelterRepository;
import shelter.repository.TransferRequestRepository;
import shelter.exception.AnimalNotInShelterException;
import shelter.exception.ShelterAtCapacityException;
import shelter.service.AuditService;
import shelter.service.TransferService;

import java.util.List;

/**
 * Concrete implementation of {@link TransferService} that orchestrates inter-shelter
 * animal transfers. Pre-conditions are validated before each state transition, the actual
 * status change is delegated to the domain object, and all affected records are persisted
 * back to their repositories.
 */
public class TransferServiceImpl implements TransferService {

    private final TransferRequestRepository requestRepository;
    private final AnimalRepository animalRepository;
    private final ShelterRepository shelterRepository;
    private final AuditService<TransferRequest> auditService;

    /**
     * Constructs a new {@code TransferServiceImpl} with the required repositories and audit service.
     * All three repositories are needed: requests track workflow state, the animal carries the
     * authoritative shelter reference, and both shelters maintain their in-memory animal lists.
     * The audit service records every state-changing operation for traceability.
     *
     * @param requestRepository the repository for transfer request persistence; must not be null
     * @param animalRepository  the repository for animal record persistence; must not be null
     * @param shelterRepository the repository for shelter record persistence; must not be null
     * @param auditService      the service used to log transfer operations; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public TransferServiceImpl(TransferRequestRepository requestRepository,
                               AnimalRepository animalRepository,
                               ShelterRepository shelterRepository,
                               AuditService<TransferRequest> auditService) {
        if (requestRepository == null) {
            throw new IllegalArgumentException("TransferRequestRepository must not be null.");
        }
        if (animalRepository == null) {
            throw new IllegalArgumentException("AnimalRepository must not be null.");
        }
        if (shelterRepository == null) {
            throw new IllegalArgumentException("ShelterRepository must not be null.");
        }
        if (auditService == null) {
            throw new IllegalArgumentException("AuditService must not be null.");
        }
        this.requestRepository = requestRepository;
        this.animalRepository = animalRepository;
        this.shelterRepository = shelterRepository;
        this.auditService = auditService;
    }

    /**
     * {@inheritDoc}
     * Verifies that the animal is currently in the source shelter and that the
     * destination shelter has remaining capacity before creating the request.
     */
    @Override
    public TransferRequest requestTransfer(Animal animal, Shelter from, Shelter to) {
        // Guard: reject null inputs at the service boundary
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        if (from == null) {
            throw new IllegalArgumentException("Source shelter must not be null.");
        }
        if (to == null) {
            throw new IllegalArgumentException("Destination shelter must not be null.");
        }
        // Business rule: the animal must actually be in the source shelter
        if (!from.containsAnimal(animal.getId())) {
            throw new AnimalNotInShelterException(
                    "Animal \"" + animal.getName() + "\" is not in shelter \"" + from.getName() + "\".");
        }
        // Business rule: the destination must have room before committing the transfer
        if (!to.hasCapacity()) {
            throw new ShelterAtCapacityException(
                    "Destination shelter \"" + to.getName() + "\" is at full capacity.");
        }
        // Create and persist the PENDING request
        TransferRequest request = new TransferRequest(animal, from, to);
        requestRepository.save(request);
        auditService.log("submitted transfer request", request);
        return request;
    }

    /**
     * {@inheritDoc}
     * On approval, moves the animal from source to destination in memory, updates the
     * animal's shelterId, and persists all affected records.
     */
    @Override
    public void approve(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("TransferRequest must not be null.");
        }
        // Delegate state transition to the domain object; it enforces PENDING-only rule
        request.approve();

        Animal animal = request.getAnimal();
        Shelter from = request.getFrom();
        Shelter to = request.getTo();

        // Update in-memory animal lists on both shelter objects
        from.removeAnimal(animal.getId());
        to.addAnimal(animal);
        // Update the animal's authoritative shelter reference (this is what gets persisted)
        animal.setShelterId(to.getId());

        // Persist all four changed records: animal, both shelters, and the request
        animalRepository.save(animal);
        shelterRepository.save(from);
        shelterRepository.save(to);
        requestRepository.save(request);
        auditService.log("approved transfer request", request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reject(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("TransferRequest must not be null.");
        }
        // Animal does not move; only the request status changes
        request.reject();
        requestRepository.save(request);
        auditService.log("rejected transfer request", request);
    }

    /**
     * {@inheritDoc}
     * The domain represents dismissal as the {@code CANCELLED} state.
     */
    @Override
    public void dismiss(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("TransferRequest must not be null.");
        }
        // "dismiss" is the service-level term; "cancel" is the domain-level state name
        request.cancel();
        requestRepository.save(request);
        auditService.log("dismissed transfer request", request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TransferRequest> getPendingRequests(Shelter shelter) {
        if (shelter == null) {
            throw new IllegalArgumentException("Shelter must not be null.");
        }
        // The repository query covers both from-shelter and to-shelter in one call
        return requestRepository.findByShelterIdAndStatus(shelter.getId(), RequestStatus.PENDING);
    }
}
