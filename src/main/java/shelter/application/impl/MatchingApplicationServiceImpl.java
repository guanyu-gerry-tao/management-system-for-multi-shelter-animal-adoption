package shelter.application.impl;

import shelter.application.MatchingApplicationService;
import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.domain.Shelter;
import shelter.exception.AnimalNotAvailableException;
import shelter.service.AdopterBasedMatchingService;
import shelter.service.AdopterService;
import shelter.service.AnimalBasedMatchingService;
import shelter.service.AnimalService;
import shelter.service.ExplanationService;
import shelter.service.ShelterService;
import shelter.service.model.MatchResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link MatchingApplicationService} that orchestrates
 * animal-adopter matching in both directions.
 * Filters available animals by shelter, delegates scoring to the matching services,
 * and optionally generates natural-language explanations via {@link ExplanationService}.
 */
public class MatchingApplicationServiceImpl implements MatchingApplicationService {

    private final AdopterBasedMatchingService adopterBasedMatchingService;
    private final AnimalBasedMatchingService animalBasedMatchingService;
    private final AnimalService animalService;
    private final AdopterService adopterService;
    private final ShelterService shelterService;
    private final ExplanationService explanationService;

    /**
     * Constructs a MatchingApplicationServiceImpl with all required service dependencies.
     * All six services are mandatory; none may be null.
     *
     * @param adopterBasedMatchingService the service that ranks animals for an adopter; must not be null
     * @param animalBasedMatchingService  the service that ranks adopters for an animal; must not be null
     * @param animalService               the service for animal lookups; must not be null
     * @param adopterService              the service for adopter lookups; must not be null
     * @param shelterService              the service for shelter lookups; must not be null
     * @param explanationService          the service for generating match explanations; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public MatchingApplicationServiceImpl(AdopterBasedMatchingService adopterBasedMatchingService,
                                           AnimalBasedMatchingService animalBasedMatchingService,
                                           AnimalService animalService,
                                           AdopterService adopterService,
                                           ShelterService shelterService,
                                           ExplanationService explanationService) {
        if (adopterBasedMatchingService == null) throw new IllegalArgumentException("AdopterBasedMatchingService must not be null.");
        if (animalBasedMatchingService  == null) throw new IllegalArgumentException("AnimalBasedMatchingService must not be null.");
        if (animalService               == null) throw new IllegalArgumentException("AnimalService must not be null.");
        if (adopterService              == null) throw new IllegalArgumentException("AdopterService must not be null.");
        if (shelterService              == null) throw new IllegalArgumentException("ShelterService must not be null.");
        if (explanationService          == null) throw new IllegalArgumentException("ExplanationService must not be null.");
        this.adopterBasedMatchingService = adopterBasedMatchingService;
        this.animalBasedMatchingService  = animalBasedMatchingService;
        this.animalService               = animalService;
        this.adopterService              = adopterService;
        this.shelterService              = shelterService;
        this.explanationService          = explanationService;
    }

    /**
     * {@inheritDoc}
     * Filters animals in the shelter to only those available (adopterId == null),
     * then delegates scoring to the adopter-based matching service.
     */
    @Override
    public List<MatchResult> matchAnimalsForAdopter(String adopterId, String shelterId,
                                                     boolean withExplanation) {
        Adopter adopter = adopterService.findById(adopterId);
        Shelter shelter = shelterService.findById(shelterId);

        // Only available (unadopted) matchable animals are eligible; non-matchable types are excluded
        List<Animal> available = animalService.getAnimalsByShelter(shelter).stream()
                .filter(Animal::isAvailable)
                .filter(Animal::isMatchable)
                .collect(Collectors.toList());

        List<MatchResult> results = adopterBasedMatchingService.match(adopter, available);

        // Optionally generate natural-language explanation for the ranked results
        if (withExplanation && !results.isEmpty()) {
            explanationService.explain(results);
        }

        return results;
    }

    /**
     * {@inheritDoc}
     * Throws if the animal is already adopted. Scores all registered adopters.
     */
    @Override
    public List<MatchResult> matchAdoptersForAnimal(String animalId, boolean withExplanation) {
        Animal animal = animalService.findById(animalId);

        // Non-matchable animals (e.g., Other) are not supported by the matching system
        if (!animal.isMatchable()) {
            throw new IllegalArgumentException("Matching is not supported for this animal type.");
        }

        // Cannot match adopters for an animal that has already been adopted
        if (!animal.isAvailable()) {
            throw new AnimalNotAvailableException("Animal " + animalId + " has already been adopted.");
        }

        List<Adopter> adopters = adopterService.listAll();
        List<MatchResult> results = animalBasedMatchingService.match(animal, adopters);

        // Optionally generate natural-language explanation for the ranked results
        if (withExplanation && !results.isEmpty()) {
            explanationService.explain(results);
        }

        return results;
    }
}
