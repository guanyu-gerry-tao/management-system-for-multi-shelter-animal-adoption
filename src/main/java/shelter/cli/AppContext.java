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

    /** @return the animal application service */
    public AnimalApplicationService animalApp() { return startup.animalApp(); }

    /** @return the adopter application service */
    public AdopterApplicationService adopterApp() { return startup.adopterApp(); }

    /** @return the shelter application service */
    public ShelterApplicationService shelterApp() { return startup.shelterApp(); }

    /** @return the adoption request application service */
    public AdoptionApplicationService adoptionApp() { return startup.adoptionApp(); }

    /** @return the transfer request application service */
    public TransferApplicationService transferApp() { return startup.transferApp(); }

    /** @return the matching application service */
    public MatchingApplicationService matchingApp() { return startup.matchingApp(); }

    /** @return the vaccination application service */
    public VaccinationApplicationService vaccinationApp() { return startup.vaccinationApp(); }

    /** @return the audit application service */
    public AuditApplicationService auditApp() { return startup.auditApp(); }

    /** @return the explanation service */
    public ExplanationService explanationService() { return startup.explanationService(); }
}
