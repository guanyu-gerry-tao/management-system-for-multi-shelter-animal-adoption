package shelter.startup;

import shelter.application.AdopterApplicationService;
import shelter.application.AdoptionApplicationService;
import shelter.application.AnimalApplicationService;
import shelter.application.AuditApplicationService;
import shelter.application.MatchingApplicationService;
import shelter.application.ShelterApplicationService;
import shelter.application.SystemStartup;
import shelter.application.TransferApplicationService;
import shelter.application.VaccinationApplicationService;
import shelter.service.ExplanationService;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Coordinates startup for the shelter CLI application.
 * This class owns the startup sequence: bootstrap the work directory, create repositories,
 * restore loaded relationships, and build the application graph.
 */
public class SystemStartupImpl implements SystemStartup {

    
    private static final Path DEFAULT_SHELTER_HOME =
            Path.of(System.getProperty("user.home"), "shelter");

    private static SystemStartupImpl instance;

    private final Path shelterHome;
    private final WorkdirBootstrapper workdirBootstrapper;
    private final CsvRepositoryFactory repositoryFactory;
    private final ShelterAnimalLinker shelterAnimalLinker;

    private ApplicationGraph applicationGraph;

    /**
     * Constructs startup using the default {@code ~/shelter} work directory.
     * This constructor is used by the CLI when running in the normal demo environment.
     */
    public SystemStartupImpl() {
        this(DEFAULT_SHELTER_HOME);
    }

    /**
     * Constructs startup using a caller-provided shelter work directory.
     * This constructor supports tests that should not write into the real user home directory.
     *
     * @param shelterHome the base shelter work directory
     */
    public SystemStartupImpl(Path shelterHome) {
        this(shelterHome, new WorkdirBootstrapper(), new CsvRepositoryFactory(), new ShelterAnimalLinker());
    }

    /**
     * Constructs startup with explicit helper objects.
     * This keeps startup orchestration testable without introducing a dependency injection framework.
     *
     * @param shelterHome the base shelter work directory
     * @param workdirBootstrapper prepares the work directory before repository loading
     * @param repositoryFactory creates and loads CSV repositories
     * @param shelterAnimalLinker restores shelter-to-animal links after CSV loading
     */
    public SystemStartupImpl(Path shelterHome,
                             WorkdirBootstrapper workdirBootstrapper,
                             CsvRepositoryFactory repositoryFactory,
                             ShelterAnimalLinker shelterAnimalLinker) {
        this.shelterHome = Objects.requireNonNull(shelterHome, "Shelter home must not be null.");
        this.workdirBootstrapper = Objects.requireNonNull(
                workdirBootstrapper, "Workdir bootstrapper must not be null.");
        this.repositoryFactory = Objects.requireNonNull(
                repositoryFactory, "Repository factory must not be null.");
        this.shelterAnimalLinker = Objects.requireNonNull(
                shelterAnimalLinker, "Shelter-animal linker must not be null.");
    }

    /**
     * Returns the shared startup instance used by the CLI.
     * The instance is initialized before it is returned so callers can immediately use its getters.
     *
     * @return the shared startup implementation instance
     */
    public static SystemStartupImpl instance() {
        if (instance == null) {
            instance = new SystemStartupImpl();
        }
        instance.initialize();
        return instance;
    }

    /**
     * Initializes the system once for the current process.
     * Repeated calls are safe and return immediately after the application graph has been built.
     */
    @Override
    public void initialize() {
        if (applicationGraph != null) {
            return;
        }

        instance = this;

        workdirBootstrapper.bootstrap(shelterHome);

        RepositoryBundle repositories = repositoryFactory.create(shelterHome.resolve("data"));
        shelterAnimalLinker.restoreLinks(repositories);
        applicationGraph = ApplicationGraph.from(repositories);
    }

    /**
     * Provides access to the animal application service, initializing the system if not yet started.
     * Delegates retrieval to the fully wired {@link ApplicationGraph}.
     *
     * @return the animal application service
     */
    public AnimalApplicationService animalApp() {
        return graph().animalApp();
    }

    /**
     * Provides access to the adopter application service, initializing the system if not yet started.
     * Delegates retrieval to the fully wired {@link ApplicationGraph}.
     *
     * @return the adopter application service
     */
    public AdopterApplicationService adopterApp() {
        return graph().adopterApp();
    }

    /**
     * Provides access to the shelter application service, initializing the system if not yet started.
     * Delegates retrieval to the fully wired {@link ApplicationGraph}.
     *
     * @return the shelter application service
     */
    public ShelterApplicationService shelterApp() {
        return graph().shelterApp();
    }

    /**
     * Provides access to the adoption application service, initializing the system if not yet started.
     * Delegates retrieval to the fully wired {@link ApplicationGraph}.
     *
     * @return the adoption request application service
     */
    public AdoptionApplicationService adoptionApp() {
        return graph().adoptionApp();
    }

    /**
     * Provides access to the transfer application service, initializing the system if not yet started.
     * Delegates retrieval to the fully wired {@link ApplicationGraph}.
     *
     * @return the transfer request application service
     */
    public TransferApplicationService transferApp() {
        return graph().transferApp();
    }

    /**
     * Provides access to the matching application service, initializing the system if not yet started.
     * Delegates retrieval to the fully wired {@link ApplicationGraph}.
     *
     * @return the matching application service
     */
    public MatchingApplicationService matchingApp() {
        return graph().matchingApp();
    }

    /**
     * Provides access to the vaccination application service, initializing the system if not yet started.
     * Delegates retrieval to the fully wired {@link ApplicationGraph}.
     *
     * @return the vaccination application service
     */
    public VaccinationApplicationService vaccinationApp() {
        return graph().vaccinationApp();
    }

    /**
     * Provides access to the audit application service, initializing the system if not yet started.
     * Delegates retrieval to the fully wired {@link ApplicationGraph}.
     *
     * @return the audit application service
     */
    public AuditApplicationService auditApp() {
        return graph().auditApp();
    }

    /**
     * Provides access to the explanation service, initializing the system if not yet started.
     * Delegates retrieval to the fully wired {@link ApplicationGraph}.
     *
     * @return the explanation service
     */
    public ExplanationService explanationService() {
        return graph().explanationService();
    }

    private ApplicationGraph graph() {
        initialize();
        return applicationGraph;
    }
}
