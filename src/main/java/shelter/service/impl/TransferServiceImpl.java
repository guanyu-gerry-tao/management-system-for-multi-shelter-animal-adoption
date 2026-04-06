package shelter.service.impl;

import shelter.domain.Animal;
import shelter.domain.RequestStatus;
import shelter.domain.Shelter;
import shelter.domain.TransferRequest;
import shelter.repository.AnimalRepository;
import shelter.repository.ShelterRepository;
import shelter.repository.TransferRequestRepository;
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

    /**
     * Constructs a new {@code TransferServiceImpl} with the required repositories.
     * All three are needed: requests track workflow state, the animal carries the
     * authoritative shelter reference, and both shelters maintain their in-memory animal lists.
     *
     * @param requestRepository the repository for transfer request persistence; must not be null
     * @param animalRepository  the repository for animal record persistence; must not be null
     * @param shelterRepository the repository for shelter record persistence; must not be null
     * @throws IllegalArgumentException if any repository is null
     */
    public TransferServiceImpl(TransferRequestRepository requestRepository,
                               AnimalRepository animalRepository,
                               ShelterRepository shelterRepository) {
        if (requestRepository == null) {
            throw new IllegalArgumentException("TransferRequestRepository must not be null.");
        }
        if (animalRepository == null) {
            throw new IllegalArgumentException("AnimalRepository must not be null.");
        }
        if (shelterRepository == null) {
            throw new IllegalArgumentException("ShelterRepository must not be null.");
        }
        this.requestRepository = requestRepository;
        this.animalRepository = animalRepository;
        this.shelterRepository = shelterRepository;
    }

    /**
     * {@inheritDoc}
     * Verifies that the animal is currently in the source shelter and that the
     * destination shelter has remaining capacity before creating the request.
     */
    @Override
    public TransferRequest requestTransfer(Animal animal, Shelter from, Shelter to) {
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        if (from == null) {
            throw new IllegalArgumentException("Source shelter must not be null.");
        }
        if (to == null) {
            throw new IllegalArgumentException("Destination shelter must not be null.");
        }
        if (!from.containsAnimal(animal.getId())) {
            throw new IllegalArgumentException(
                    "Animal \"" + animal.getName() + "\" is not in shelter \"" + from.getName() + "\".");
        }
        if (!to.hasCapacity()) {
            throw new IllegalStateException(
                    "Destination shelter \"" + to.getName() + "\" is at full capacity.");
        }
        TransferRequest request = new TransferRequest(animal, from, to);
        requestRepository.save(request);
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
        request.approve(); // enforces PENDING; throws IllegalStateException if not

        Animal animal = request.getAnimal();
        Shelter from = request.getFrom();
        Shelter to = request.getTo();

        from.removeAnimal(animal.getId());
        to.addAnimal(animal);
        animal.setShelterId(to.getId());

        animalRepository.save(animal);
        shelterRepository.save(from);
        shelterRepository.save(to);
        requestRepository.save(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reject(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("TransferRequest must not be null.");
        }
        request.reject();
        requestRepository.save(request);
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
        request.cancel();
        requestRepository.save(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TransferRequest> getPendingRequests(Shelter shelter) {
        if (shelter == null) {
            throw new IllegalArgumentException("Shelter must not be null.");
        }
        return requestRepository.findByShelterIdAndStatus(shelter.getId(), RequestStatus.PENDING);
    }
}
