package shelter.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import shelter.domain.TransferRequest;

import java.io.PrintWriter;
import java.util.List;

/**
 * Top-level CLI command group for the inter-shelter transfer request lifecycle.
 * Provides subcommands to request, approve, reject, and cancel transfer requests.
 * All operations are delegated to {@link AppContext#transferApp()}.
 */
@Command(
        name = "transfer",
        description = "Manage inter-shelter transfer requests",
        subcommands = {
                TransferCmd.ListCmd.class,
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
    // render helper (shared by `transfer list` and `shelter print`)
    // -------------------------------------------------------------------------

    /**
     * Renders transfer requests as a comma-headed, space-padded table to the given writer.
     * Empty input prints the header followed by {@code (none)}.
     * Used by both {@code shelter transfer list} and {@code shelter print}.
     *
     * @param out      the writer to print to; must not be null
     * @param requests the transfer requests to render; must not be null (may be empty)
     */
    public static void renderList(PrintWriter out, List<TransferRequest> requests) {
        out.printf("%-36s  %-14s  %-14s  %-14s  %-10s  %s%n",
                "ID,", "ANIMAL,", "FROM,", "TO,", "STATUS,", "REQUESTED AT");
        if (requests.isEmpty()) {
            out.println("(none)");
            out.flush();
            return;
        }
        for (TransferRequest r : requests) {
            out.printf("%-36s  %-14s  %-14s  %-14s  %-10s  %s%n",
                    r.getId(),
                    r.getAnimal().getName(),
                    r.getFrom().getName(),
                    r.getTo().getName(),
                    r.getStatus().name(),
                    r.getRequestedAt());
        }
        out.flush();
    }

    // -------------------------------------------------------------------------
    // list
    // -------------------------------------------------------------------------

    /**
     * Lists every transfer request currently in the system.
     * Used primarily for demo purposes and by the {@code shelter print} summary.
     */
    @Command(name = "list", description = "List all transfer requests",
             mixinStandardHelpOptions = true)
    static class ListCmd implements Runnable {

        /**
         * Executes the list operation by delegating to {@link TransferCmd#renderList}.
         * Writes to stdout via a flushing {@link PrintWriter}.
         */
        @Override
        public void run() {
            try {
                renderList(new PrintWriter(System.out, true),
                        AppContext.get().transferApp().listAllTransfers());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
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
