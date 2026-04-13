package shelter.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import shelter.domain.TransferRequest;

/**
 * Top-level CLI command group for the inter-shelter transfer request lifecycle.
 * Provides subcommands to request, approve, reject, and cancel transfer requests.
 * All operations are delegated to {@link AppContext#transferApp()}.
 */
@Command(
        name = "transfer",
        description = "Manage inter-shelter transfer requests",
        subcommands = {
                TransferCmd.RequestCmd.class,
                TransferCmd.ApproveCmd.class,
                TransferCmd.RejectCmd.class,
                TransferCmd.CancelCmd.class
        },
        mixinStandardHelpOptions = true
)
public class TransferCmd implements Runnable {

    /**
     * Prints usage help when the subcommand group is invoked without a subcommand.
     * This method is called by Picocli when no subcommand is specified.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter transfer <subcommand> --help");
    }

    // -------------------------------------------------------------------------
    // request
    // -------------------------------------------------------------------------

    /**
     * Initiates a transfer request to move an available animal from one shelter to another.
     * The animal must be available and the destination shelter must not be at capacity.
     */
    @Command(name = "request", description = "Initiate a transfer request",
             mixinStandardHelpOptions = true)
    static class RequestCmd implements Runnable {

        /** The ID of the animal to transfer; required. */
        @Option(names = "--animal", required = true, description = "Animal ID")
        private String animalId;

        /** The ID of the source shelter; required. */
        @Option(names = "--from", required = true, description = "Source shelter ID")
        private String fromShelterId;

        /** The ID of the destination shelter; required. */
        @Option(names = "--to", required = true, description = "Destination shelter ID")
        private String toShelterId;

        /**
         * Executes the transfer request and prints the new request's ID.
         * Prints an error message if the animal is not found, not available, or the destination
         * shelter is at capacity.
         */
        @Override
        public void run() {
            try {
                TransferRequest r = AppContext.get().transferApp()
                        .requestTransfer(animalId, fromShelterId, toShelterId);
                System.out.printf("Transfer request created: id=%s (animal=%s, %s → %s)%n",
                        r.getId(), animalId, fromShelterId, toShelterId);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // approve
    // -------------------------------------------------------------------------

    /**
     * Approves a pending transfer request and moves the animal to the destination shelter.
     * Updates the animal's shelter assignment in the persistent store.
     */
    @Command(name = "approve", description = "Approve a transfer request",
             mixinStandardHelpOptions = true)
    static class ApproveCmd implements Runnable {

        /** The ID of the transfer request to approve; required. */
        @Option(names = "--request", required = true, description = "Transfer request ID")
        private String requestId;

        /**
         * Executes the approval and prints a confirmation message.
         * Prints an error message if the request is not found or is not in a pending state.
         */
        @Override
        public void run() {
            try {
                AppContext.get().transferApp().approveTransfer(requestId);
                System.out.println("Approved transfer request: " + requestId);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // reject
    // -------------------------------------------------------------------------

    /**
     * Rejects a pending transfer request, leaving the animal in the source shelter.
     * The destination shelter remains unaffected.
     */
    @Command(name = "reject", description = "Reject a transfer request",
             mixinStandardHelpOptions = true)
    static class RejectCmd implements Runnable {

        /** The ID of the transfer request to reject; required. */
        @Option(names = "--request", required = true, description = "Transfer request ID")
        private String requestId;

        /**
         * Executes the rejection and prints a confirmation message.
         * Prints an error message if the request is not found or is not in a pending state.
         */
        @Override
        public void run() {
            try {
                AppContext.get().transferApp().rejectTransfer(requestId);
                System.out.println("Rejected transfer request: " + requestId);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // cancel
    // -------------------------------------------------------------------------

    /**
     * Cancels a pending transfer request that has not yet been reviewed.
     * The animal remains in the source shelter after cancellation.
     */
    @Command(name = "cancel", description = "Cancel a transfer request",
             mixinStandardHelpOptions = true)
    static class CancelCmd implements Runnable {

        /** The ID of the transfer request to cancel; required. */
        @Option(names = "--request", required = true, description = "Transfer request ID")
        private String requestId;

        /**
         * Executes the cancellation and prints a confirmation message.
         * Prints an error message if the request is not found or is not in a pending state.
         */
        @Override
        public void run() {
            try {
                AppContext.get().transferApp().cancelTransfer(requestId);
                System.out.println("Cancelled transfer request: " + requestId);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
