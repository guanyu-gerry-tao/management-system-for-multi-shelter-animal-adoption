package shelter.application;

/**
 * Handles system initialization at startup, loading all persisted data and setting up session state.
 * Implementations are responsible for reading data from the persistent store and establishing
 * the hardcoded admin staff member as the current session operator.
 */
public interface SystemStartup {

    /**
     * Initializes the system by loading all persisted data from the data directory.
     * If no data files exist, the system starts with empty state.
     * After this method returns, all services are ready to accept requests for the current session.
     */
    void initialize();
}
