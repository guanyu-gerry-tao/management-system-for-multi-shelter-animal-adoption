package shelter.cli;

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
import shelter.domain.AdoptionRequest;
import shelter.domain.Animal;
import shelter.domain.Adopter;
import shelter.domain.Shelter;
import shelter.domain.Staff;
import shelter.domain.TransferRequest;
import shelter.domain.VaccinationRecord;
import shelter.repository.csv.CsvAdopterRepository;
import shelter.repository.csv.CsvAdoptionRequestRepository;
import shelter.repository.csv.CsvAnimalRepository;
import shelter.repository.csv.CsvAuditRepository;
import shelter.repository.csv.CsvShelterRepository;
import shelter.repository.csv.CsvTransferRequestRepository;
import shelter.repository.csv.CsvVaccinationRecordRepository;
import shelter.repository.csv.CsvVaccineTypeRepository;
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

/**
 * Singleton dependency-injection container that wires together all repositories, services,
 * and application-layer objects for a single CLI command execution.
 * Repositories are initialized from the given data directory; a hardcoded admin staff member
 * is used as the current session operator.
 */
public class AppContext {

    /** The data directory path used for all CSV repositories. */
    public static final String DATA_DIR =
            System.getProperty("user.home") + "/shelter/data";

    private static AppContext instance;

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
     * Returns the singleton AppContext, initializing it on first call.
     * Subsequent calls return the same instance without re-loading data.
     *
     * @return the shared AppContext instance
     */
    public static AppContext get() {
        if (instance == null) {
            instance = new AppContext(DATA_DIR);
        }
        return instance;
    }

    /**
     * Constructs an AppContext by wiring all repositories, services, and application services.
     * Uses the given data directory for all CSV-backed repositories and a hardcoded admin staff.
     *
     * @param dataDir the path to the directory where CSV data files are stored
     */
    private AppContext(String dataDir) {
        // Hardcoded admin staff for the demo session
        Staff admin = new Staff("Admin", "Manager");

        // --- Repositories ---
        CsvShelterRepository shelterRepo         = new CsvShelterRepository(dataDir);
        CsvAnimalRepository animalRepo           = new CsvAnimalRepository(dataDir);
        CsvAdopterRepository adopterRepo         = new CsvAdopterRepository(dataDir);
        CsvAdoptionRequestRepository adoptionRepo =
                new CsvAdoptionRequestRepository(dataDir, animalRepo, adopterRepo);
        CsvTransferRequestRepository transferRepo =
                new CsvTransferRequestRepository(dataDir, animalRepo, shelterRepo);
        CsvVaccineTypeRepository vaccTypeRepo    = new CsvVaccineTypeRepository(dataDir);
        CsvVaccinationRecordRepository vacRepo   =
                new CsvVaccinationRecordRepository(dataDir, animalRepo);
        CsvAuditRepository auditRepo             = new CsvAuditRepository(dataDir);

        // --- Typed audit services (one per domain type) ---
        AuditServiceImpl<Shelter>           shelterAudit    = new AuditServiceImpl<>(admin, auditRepo);
        AuditServiceImpl<Animal>            animalAudit     = new AuditServiceImpl<>(admin, auditRepo);
        AuditServiceImpl<Adopter>           adopterAudit    = new AuditServiceImpl<>(admin, auditRepo);
        AuditServiceImpl<AdoptionRequest>   adoptionAudit   = new AuditServiceImpl<>(admin, auditRepo);
        AuditServiceImpl<TransferRequest>   transferAudit   = new AuditServiceImpl<>(admin, auditRepo);
        AuditServiceImpl<VaccinationRecord> vacAudit        = new AuditServiceImpl<>(admin, auditRepo);
        AuditServiceImpl<Object>            vaccTypeAudit   = new AuditServiceImpl<>(admin, auditRepo);

        // --- Core services ---
        ShelterServiceImpl shelterService    = new ShelterServiceImpl(shelterRepo);
        AnimalServiceImpl animalService      = new AnimalServiceImpl(animalRepo);
        AdopterServiceImpl adopterService    = new AdopterServiceImpl(adopterRepo);
        AdoptionServiceImpl adoptionService  =
                new AdoptionServiceImpl(adoptionRepo, animalRepo, adopterRepo, adoptionAudit);
        TransferServiceImpl transferService  =
                new TransferServiceImpl(transferRepo, animalRepo, shelterRepo, transferAudit);
        VaccineTypeCatalogServiceImpl vaccTypeCatalogService =
                new VaccineTypeCatalogServiceImpl(vaccTypeRepo);
        VaccinationServiceImpl vaccinationService =
                new VaccinationServiceImpl(vacRepo, vaccTypeRepo, vacAudit);
        RequestNotificationServiceImpl notificationService =
                new RequestNotificationServiceImpl(admin, auditRepo);

        // --- Matching strategies (all active for the demo) ---
        List<IMatchingStrategy> strategies = List.of(
                new SpeciesPreferenceStrategy(),
                new BreedPreferenceStrategy(),
                new ActivityLevelStrategy(),
                new AgePreferenceStrategy(),
                new LifestyleCompatibilityStrategy(),
                new VaccinationPreferenceStrategy()
        );
        AdopterBasedMatchingService adopterMatchingService =
                new AdopterBasedMatchingServiceImpl(strategies);
        AnimalBasedMatchingService animalMatchingService =
                new AnimalBasedMatchingServiceImpl(strategies);

        // --- Application services ---
        shelterAppService    = new ShelterApplicationServiceImpl(shelterService, shelterAudit);
        animalAppService     = new AnimalApplicationServiceImpl(animalService, shelterService, animalAudit);
        adopterAppService    = new AdopterApplicationServiceImpl(adopterService, adopterAudit);
        adoptionAppService   = new AdoptionApplicationServiceImpl(
                adoptionService, animalService, adopterService, notificationService, adoptionAudit);
        transferAppService   = new TransferApplicationServiceImpl(
                transferService, animalService, shelterService, notificationService, transferAudit);
        matchingAppService   = new MatchingApplicationServiceImpl(
                adopterMatchingService, animalMatchingService,
                animalService, adopterService, shelterService,
                new MockExplanationService());
        explanationService = new MockExplanationService();
        vaccinationAppService = new VaccinationApplicationServiceImpl(
                vaccinationService, vaccTypeCatalogService, animalService, vaccTypeAudit);

        // Audit log delegates to the persistent repository to show the full history across sessions
        auditAppService = new AuditApplicationServiceImpl(new shelter.service.AuditService<String>() {
            @Override
            public void log(String action, String target) { /* no-op: only used for reading */ }

            @Override
            public java.util.List<shelter.service.model.AuditEntry<String>> getLog() {
                return auditRepo.findAll();
            }
        });
    }

    /**
     * Returns the application service for animal management operations.
     *
     * @return the {@link AnimalApplicationService} instance
     */
    public AnimalApplicationService animalApp()      { return animalAppService; }

    /**
     * Returns the application service for adopter management operations.
     *
     * @return the {@link AdopterApplicationService} instance
     */
    public AdopterApplicationService adopterApp()    { return adopterAppService; }

    /**
     * Returns the application service for shelter management operations.
     *
     * @return the {@link ShelterApplicationService} instance
     */
    public ShelterApplicationService shelterApp()    { return shelterAppService; }

    /**
     * Returns the application service for the adoption request lifecycle.
     *
     * @return the {@link AdoptionApplicationService} instance
     */
    public AdoptionApplicationService adoptionApp()  { return adoptionAppService; }

    /**
     * Returns the application service for the transfer request lifecycle.
     *
     * @return the {@link TransferApplicationService} instance
     */
    public TransferApplicationService transferApp()  { return transferAppService; }

    /**
     * Returns the application service for animal-adopter matching.
     *
     * @return the {@link MatchingApplicationService} instance
     */
    public MatchingApplicationService matchingApp()  { return matchingAppService; }

    /**
     * Returns the application service for vaccination and vaccine type management.
     *
     * @return the {@link VaccinationApplicationService} instance
     */
    public VaccinationApplicationService vaccinationApp() { return vaccinationAppService; }

    /**
     * Returns the application service for retrieving the session audit log.
     *
     * @return the {@link AuditApplicationService} instance
     */
    public AuditApplicationService auditApp()        { return auditAppService; }

    /**
     * Returns the explanation service used to generate natural-language match summaries.
     * The CLI calls this directly to display structured explanation output when {@code --explain} is set.
     *
     * @return the {@link ExplanationService} instance
     */
    public ExplanationService explanationService()   { return explanationService; }
}
