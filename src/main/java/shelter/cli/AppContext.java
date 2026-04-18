package shelter.cli;

import shelter.application.AdopterApplicationService;
import shelter.application.AdoptionApplicationService;
import shelter.application.AnimalApplicationService;
import shelter.application.AuditApplicationService;
import shelter.application.MatchingApplicationService;
import shelter.application.ShelterApplicationService;
import shelter.application.TransferApplicationService;
import shelter.application.VaccinationApplicationService;
import shelter.service.ExplanationService;
import shelter.startup.SystemStartupImpl;

/**
 * Compatibility adapter for CLI commands that still call {@code AppContext.get()}.
 * The real startup and dependency wiring now live in {@link SystemStartupImpl}.
 */
public class AppContext {

    private static AppContext instance;

    private final SystemStartupImpl startup;

    private AppContext(SystemStartupImpl startup) {
        this.startup = startup;
    }

    /**
     * Returns the shared CLI context.
     *
     * @return the shared AppContext instance
     */
    public static AppContext get() {
        if (instance == null) {
            instance = new AppContext(SystemStartupImpl.instance());
        }
        return instance;
    }

    /**
     * Provides access to the animal application service for orchestrating animal-related use cases.
     * Delegates to the underlying {@link SystemStartupImpl} instance.
     *
     * @return the animal application service
     */
    public AnimalApplicationService animalApp() { return startup.animalApp(); }

    /**
     * Provides access to the adopter application service for orchestrating adopter-related use cases.
     * Delegates to the underlying {@link SystemStartupImpl} instance.
     *
     * @return the adopter application service
     */
    public AdopterApplicationService adopterApp() { return startup.adopterApp(); }

    /**
     * Provides access to the shelter application service for orchestrating shelter-related use cases.
     * Delegates to the underlying {@link SystemStartupImpl} instance.
     *
     * @return the shelter application service
     */
    public ShelterApplicationService shelterApp() { return startup.shelterApp(); }

    /**
     * Provides access to the adoption application service for orchestrating adoption request use cases.
     * Delegates to the underlying {@link SystemStartupImpl} instance.
     *
     * @return the adoption request application service
     */
    public AdoptionApplicationService adoptionApp() { return startup.adoptionApp(); }

    /**
     * Provides access to the transfer application service for orchestrating inter-shelter transfer use cases.
     * Delegates to the underlying {@link SystemStartupImpl} instance.
     *
     * @return the transfer request application service
     */
    public TransferApplicationService transferApp() { return startup.transferApp(); }

    /**
     * Provides access to the matching application service for orchestrating animal-adopter matching use cases.
     * Delegates to the underlying {@link SystemStartupImpl} instance.
     *
     * @return the matching application service
     */
    public MatchingApplicationService matchingApp() { return startup.matchingApp(); }

    /**
     * Provides access to the vaccination application service for orchestrating vaccination use cases.
     * Delegates to the underlying {@link SystemStartupImpl} instance.
     *
     * @return the vaccination application service
     */
    public VaccinationApplicationService vaccinationApp() { return startup.vaccinationApp(); }

    /**
     * Provides access to the audit application service for retrieving the system audit log.
     * Delegates to the underlying {@link SystemStartupImpl} instance.
     *
     * @return the audit application service
     */
    public AuditApplicationService auditApp() { return startup.auditApp(); }

    /**
     * Provides access to the explanation service used to generate natural-language match summaries.
     * Delegates to the underlying {@link SystemStartupImpl} instance.
     *
     * @return the explanation service
     */
    public ExplanationService explanationService() { return startup.explanationService(); }
}
