package shelter.startup;

import shelter.application.AdopterApplicationService;
import shelter.application.AdoptionApplicationService;
import shelter.application.AnimalApplicationService;
import shelter.application.AuditApplicationService;
import shelter.application.MatchingApplicationService;
import shelter.application.ShelterApplicationService;
import shelter.application.TransferApplicationService;
import shelter.application.VaccinationApplicationService;
import shelter.application.impl.AdopterApplicationServiceImpl;
import shelter.application.impl.AdoptionApplicationServiceImpl;
import shelter.application.impl.AnimalApplicationServiceImpl;
import shelter.application.impl.AuditApplicationServiceImpl;
import shelter.application.impl.MatchingApplicationServiceImpl;
import shelter.application.impl.ShelterApplicationServiceImpl;
import shelter.application.impl.TransferApplicationServiceImpl;
import shelter.application.impl.VaccinationApplicationServiceImpl;
import shelter.domain.Adopter;
import shelter.domain.AdoptionRequest;
import shelter.domain.Animal;
import shelter.domain.Shelter;
import shelter.domain.Staff;
import shelter.domain.TransferRequest;
import shelter.domain.VaccinationRecord;
import shelter.repository.AdopterRepository;
import shelter.repository.AdoptionRequestRepository;
import shelter.repository.AnimalRepository;
import shelter.repository.AuditRepository;
import shelter.repository.ShelterRepository;
import shelter.repository.TransferRequestRepository;
import shelter.repository.VaccinationRecordRepository;
import shelter.repository.VaccineTypeRepository;
import shelter.service.AdopterBasedMatchingService;
import shelter.service.AnimalBasedMatchingService;
import shelter.service.ExplanationService;
import shelter.service.impl.AdopterBasedMatchingServiceImpl;
import shelter.service.impl.AdopterServiceImpl;
import shelter.service.impl.AdoptionServiceImpl;
import shelter.service.impl.AnimalBasedMatchingServiceImpl;
import shelter.service.impl.AnimalServiceImpl;
import shelter.service.impl.AuditServiceImpl;
import shelter.service.impl.MockExplanationService;
import shelter.service.impl.RequestNotificationServiceImpl;
import shelter.service.impl.ShelterServiceImpl;
import shelter.service.impl.TransferServiceImpl;
import shelter.service.impl.VaccinationServiceImpl;
import shelter.service.impl.VaccineTypeCatalogServiceImpl;
import shelter.strategy.ActivityLevelStrategy;
import shelter.strategy.AgePreferenceStrategy;
import shelter.strategy.BreedPreferenceStrategy;
import shelter.strategy.IMatchingStrategy;
import shelter.strategy.LifestyleCompatibilityStrategy;
import shelter.strategy.SpeciesPreferenceStrategy;
import shelter.strategy.VaccinationPreferenceStrategy;

import java.util.List;
import java.util.Objects;

/**
 * Holds the fully wired application layer created during startup.
 * The CLI uses this graph to reach application services without knowing how
 * repositories, domain services, strategies, and audit services are constructed.
 */
public class ApplicationGraph {

    private final AnimalApplicationService animalAppService;
    private final AdopterApplicationService adopterAppService;
    private final ShelterApplicationService shelterAppService;
    private final AdoptionApplicationService adoptionAppService;
    private final TransferApplicationService transferAppService;
    private final MatchingApplicationService matchingAppService;
    private final VaccinationApplicationService vaccinationAppService;
    private final AuditApplicationService auditAppService;
    private final ExplanationService explanationService;

    /**
     * Constructs the application graph from already-wired application services.
     * Each parameter is validated to be non-null so that callers receive an early failure
     * rather than a NullPointerException at first use.
     *
     * @param animalAppService the application service for animal use cases
     * @param adopterAppService the application service for adopter use cases
     * @param shelterAppService the application service for shelter use cases
     * @param adoptionAppService the application service for adoption request use cases
     * @param transferAppService the application service for inter-shelter transfer use cases
     * @param matchingAppService the application service for animal-adopter matching use cases
     * @param vaccinationAppService the application service for vaccination use cases
     * @param auditAppService the application service for audit log retrieval
     * @param explanationService the service that generates natural-language match summaries
     */
    public ApplicationGraph(AnimalApplicationService animalAppService,
                            AdopterApplicationService adopterAppService,
                            ShelterApplicationService shelterAppService,
                            AdoptionApplicationService adoptionAppService,
                            TransferApplicationService transferAppService,
                            MatchingApplicationService matchingAppService,
                            VaccinationApplicationService vaccinationAppService,
                            AuditApplicationService auditAppService,
                            ExplanationService explanationService) {
        this.animalAppService = Objects.requireNonNull(
                animalAppService, "Animal application service must not be null.");
        this.adopterAppService = Objects.requireNonNull(
                adopterAppService, "Adopter application service must not be null.");
        this.shelterAppService = Objects.requireNonNull(
                shelterAppService, "Shelter application service must not be null.");
        this.adoptionAppService = Objects.requireNonNull(
                adoptionAppService, "Adoption application service must not be null.");
        this.transferAppService = Objects.requireNonNull(
                transferAppService, "Transfer application service must not be null.");
        this.matchingAppService = Objects.requireNonNull(
                matchingAppService, "Matching application service must not be null.");
        this.vaccinationAppService = Objects.requireNonNull(
                vaccinationAppService, "Vaccination application service must not be null.");
        this.auditAppService = Objects.requireNonNull(
                auditAppService, "Audit application service must not be null.");
        this.explanationService = Objects.requireNonNull(
                explanationService, "Explanation service must not be null.");
    }

    /**
     * Builds the service and application-service graph from repositories.
     *
     * @param repositories the repository bundle created during startup
     * @return the fully wired application graph
     */
    public static ApplicationGraph from(RepositoryBundle repositories) {
        Objects.requireNonNull(repositories, "Repository bundle must not be null.");

        Staff admin = new Staff("Admin", "Manager");

        ShelterRepository shelterRepository = repositories.shelterRepository();
        AnimalRepository animalRepository = repositories.animalRepository();
        AdopterRepository adopterRepository = repositories.adopterRepository();
        AdoptionRequestRepository adoptionRequestRepository = repositories.adoptionRequestRepository();
        TransferRequestRepository transferRequestRepository = repositories.transferRequestRepository();
        VaccineTypeRepository vaccineTypeRepository = repositories.vaccineTypeRepository();
        VaccinationRecordRepository vaccinationRecordRepository = repositories.vaccinationRecordRepository();
        AuditRepository auditRepository = repositories.auditRepository();

        AuditServiceImpl<Shelter> shelterAudit = new AuditServiceImpl<>(admin, auditRepository);
        AuditServiceImpl<Animal> animalAudit = new AuditServiceImpl<>(admin, auditRepository);
        AuditServiceImpl<Adopter> adopterAudit = new AuditServiceImpl<>(admin, auditRepository);
        AuditServiceImpl<AdoptionRequest> adoptionAudit = new AuditServiceImpl<>(admin, auditRepository);
        AuditServiceImpl<TransferRequest> transferAudit = new AuditServiceImpl<>(admin, auditRepository);
        AuditServiceImpl<VaccinationRecord> vaccinationAudit = new AuditServiceImpl<>(admin, auditRepository);
        AuditServiceImpl<Object> vaccineTypeAudit = new AuditServiceImpl<>(admin, auditRepository);

        ShelterServiceImpl shelterService = new ShelterServiceImpl(shelterRepository);
        AnimalServiceImpl animalService = new AnimalServiceImpl(animalRepository);
        AdopterServiceImpl adopterService = new AdopterServiceImpl(adopterRepository);
        AdoptionServiceImpl adoptionService =
                new AdoptionServiceImpl(
                        adoptionRequestRepository, animalRepository, adopterRepository, adoptionAudit);
        TransferServiceImpl transferService =
                new TransferServiceImpl(
                        transferRequestRepository, animalRepository, shelterRepository, transferAudit);
        VaccineTypeCatalogServiceImpl vaccineTypeCatalogService =
                new VaccineTypeCatalogServiceImpl(vaccineTypeRepository);
        VaccinationServiceImpl vaccinationService =
                new VaccinationServiceImpl(
                        vaccinationRecordRepository, vaccineTypeRepository, vaccinationAudit);
        RequestNotificationServiceImpl notificationService =
                new RequestNotificationServiceImpl(admin, auditRepository);

        List<IMatchingStrategy> strategies = List.of(
                new SpeciesPreferenceStrategy(),
                new BreedPreferenceStrategy(),
                new ActivityLevelStrategy(),
                new AgePreferenceStrategy(),
                new LifestyleCompatibilityStrategy(),
                new VaccinationPreferenceStrategy(vaccinationService)
        );
        AdopterBasedMatchingService adopterMatchingService =
                new AdopterBasedMatchingServiceImpl(strategies);
        AnimalBasedMatchingService animalMatchingService =
                new AnimalBasedMatchingServiceImpl(strategies);

        ExplanationService explanationService = new MockExplanationService();

        AnimalApplicationService animalAppService =
                new AnimalApplicationServiceImpl(animalService, shelterService, adoptionService, animalAudit);
        AdopterApplicationService adopterAppService =
                new AdopterApplicationServiceImpl(adopterService, adoptionService, adopterAudit);
        ShelterApplicationService shelterAppService =
                new ShelterApplicationServiceImpl(shelterService, shelterAudit);
        AdoptionApplicationService adoptionAppService =
                new AdoptionApplicationServiceImpl(
                        adoptionService, animalService, adopterService, notificationService, adoptionAudit);
        TransferApplicationService transferAppService =
                new TransferApplicationServiceImpl(
                        transferService, animalService, shelterService, notificationService, transferAudit);
        MatchingApplicationService matchingAppService =
                new MatchingApplicationServiceImpl(
                        adopterMatchingService,
                        animalMatchingService,
                        animalService,
                        adopterService,
                        shelterService,
                        explanationService);
        VaccinationApplicationService vaccinationAppService =
                new VaccinationApplicationServiceImpl(
                        vaccinationService, vaccineTypeCatalogService, animalService, vaccineTypeAudit);
        AuditApplicationService auditAppService =
                new AuditApplicationServiceImpl(new shelter.service.AuditService<String>() {
                    @Override
                    public void log(String action, String target) { /* no-op: only used for reading */ }

                    @Override
                    public java.util.List<shelter.service.model.AuditEntry<String>> getLog() {
                        return auditRepository.findAll();
                    }
                });

        return new ApplicationGraph(
                animalAppService,
                adopterAppService,
                shelterAppService,
                adoptionAppService,
                transferAppService,
                matchingAppService,
                vaccinationAppService,
                auditAppService,
                explanationService);
    }

    /**
     * Provides access to the animal application service that orchestrates animal-related use cases.
     * Returns the instance stored in this graph during construction.
     *
     * @return the animal application service
     */
    public AnimalApplicationService animalApp() { return animalAppService; }

    /**
     * Provides access to the adopter application service that orchestrates adopter-related use cases.
     * Returns the instance stored in this graph during construction.
     *
     * @return the adopter application service
     */
    public AdopterApplicationService adopterApp() { return adopterAppService; }

    /**
     * Provides access to the shelter application service that orchestrates shelter-related use cases.
     * Returns the instance stored in this graph during construction.
     *
     * @return the shelter application service
     */
    public ShelterApplicationService shelterApp() { return shelterAppService; }

    /**
     * Provides access to the adoption application service that orchestrates adoption request use cases.
     * Returns the instance stored in this graph during construction.
     *
     * @return the adoption request application service
     */
    public AdoptionApplicationService adoptionApp() { return adoptionAppService; }

    /**
     * Provides access to the transfer application service that orchestrates inter-shelter transfer use cases.
     * Returns the instance stored in this graph during construction.
     *
     * @return the transfer request application service
     */
    public TransferApplicationService transferApp() { return transferAppService; }

    /**
     * Provides access to the matching application service that orchestrates animal-adopter matching use cases.
     * Returns the instance stored in this graph during construction.
     *
     * @return the matching application service
     */
    public MatchingApplicationService matchingApp() { return matchingAppService; }

    /**
     * Provides access to the vaccination application service that orchestrates vaccination use cases.
     * Returns the instance stored in this graph during construction.
     *
     * @return the vaccination application service
     */
    public VaccinationApplicationService vaccinationApp() { return vaccinationAppService; }

    /**
     * Provides access to the audit application service used to retrieve the system audit log.
     * Returns the instance stored in this graph during construction.
     *
     * @return the audit application service
     */
    public AuditApplicationService auditApp() { return auditAppService; }

    /**
     * Provides access to the explanation service used to generate natural-language match summaries.
     * Returns the instance stored in this graph during construction.
     *
     * @return the explanation service
     */
    public ExplanationService explanationService() { return explanationService; }
}
