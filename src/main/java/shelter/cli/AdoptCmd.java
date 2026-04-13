package shelter.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import shelter.domain.AdoptionRequest;

/**
 * Top-level CLI command group for the adoption request lifecycle.
 * Provides subcommands to submit, approve, reject, and cancel adoption requests.
 * All operations are delegated to {@link AppContext#adoptionApp()}.
 */
@Command(
        name = "adopt",
        description = "Manage adoption requests",
        subcommands = {
                AdoptCmd.SubmitCmd.class,
                AdoptCmd.ApproveCmd.class,
                AdoptCmd.RejectCmd.class,
                AdoptCmd.CancelCmd.class
        },
        mixinStandardHelpOptions = true
)
public class AdoptCmd implements Runnable {

    /**
     * Prints usage help when the subcommand group is invoked without a subcommand.
     * This method is called by Picocli when no subcommand is specified.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter adopt <subcommand> --help");
    }

    // -------------------------------------------------------------------------
    // submit
    // -------------------------------------------------------------------------

    /**
     * Submits a new adoption request on behalf of an adopter for a specific animal.
     * The animal must be available (not yet adopted).
     */
    @Command(name = "submit", description = "Submit an adoption request",
             mixinStandardHelpOptions = true)
    static class SubmitCmd implements Runnable {

        /** The ID of the adopter submitting the request; required. */
        @Option(names = "--adopter", required = true, description = "Adopter ID")
        private String adopterId;

        /** The ID of the animal being requested; required. */
        @Option(names = "--animal", required = true, description = "Animal ID")
        private String animalId;

        /**
         * Executes the submission and prints the new request's ID.
         * Prints an error message if the adopter or animal is not found, or the animal is not available.
         */
        @Override
        public void run() {
            try {
                AdoptionRequest r = AppContext.get().adoptionApp()
                        .submitRequest(adopterId, animalId);
                System.out.printf("Submitted adoption request: id=%s (adopter=%s, animal=%s)%n",
                        r.getId(), adopterId, animalId);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // approve
    // -------------------------------------------------------------------------

    /**
     * Approves a pending adoption request, marking the animal as adopted.
     * Updates both the animal's adopter reference and the adopter's adopted animal list.
     */
    @Command(name = "approve", description = "Approve an adoption request",
             mixinStandardHelpOptions = true)
    static class ApproveCmd implements Runnable {

        /** The ID of the adoption request to approve; required. */
        @Option(names = "--request", required = true, description = "Adoption request ID")
        private String requestId;

        /**
         * Executes the approval and prints a confirmation message.
         * Prints an error message if the request is not found or is not in a pending state.
         */
        @Override
        public void run() {
            try {
                AppContext.get().adoptionApp().approveRequest(requestId);
                System.out.println("Approved adoption request: " + requestId);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // reject
    // -------------------------------------------------------------------------

    /**
     * Rejects a pending adoption request, leaving the animal available for other requests.
     * The adopter is notified of the rejection.
     */
    @Command(name = "reject", description = "Reject an adoption request",
             mixinStandardHelpOptions = true)
    static class RejectCmd implements Runnable {

        /** The ID of the adoption request to reject; required. */
        @Option(names = "--request", required = true, description = "Adoption request ID")
        private String requestId;

        /**
         * Executes the rejection and prints a confirmation message.
         * Prints an error message if the request is not found or is not in a pending state.
         */
        @Override
        public void run() {
            try {
                AppContext.get().adoptionApp().rejectRequest(requestId);
                System.out.println("Rejected adoption request: " + requestId);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // cancel
    // -------------------------------------------------------------------------

    /**
     * Cancels a pending adoption request before it is reviewed.
     * The animal remains available for other requests after cancellation.
     */
    @Command(name = "cancel", description = "Cancel an adoption request",
             mixinStandardHelpOptions = true)
    static class CancelCmd implements Runnable {

        /** The ID of the adoption request to cancel; required. */
        @Option(names = "--request", required = true, description = "Adoption request ID")
        private String requestId;

        /**
         * Executes the cancellation and prints a confirmation message.
         * Prints an error message if the request is not found or is not in a pending state.
         */
        @Override
        public void run() {
            try {
                AppContext.get().adoptionApp().cancelRequest(requestId);
                System.out.println("Cancelled adoption request: " + requestId);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
