package shelter.cli;

import picocli.CommandLine.Command;
import shelter.service.model.AuditEntry;

import java.util.List;

/**
 * Top-level CLI command group for audit log operations.
 * Provides a subcommand to view the full persistent audit trail of staff actions.
 * All operations are delegated to {@link AppContext#auditApp()}.
 */
@Command(
        name = "audit",
        description = "View the system audit log",
        subcommands = {AuditCmd.LogCmd.class},
        mixinStandardHelpOptions = true
)
public class AuditCmd implements Runnable {

    /**
     * Prints usage help when the subcommand group is invoked without a subcommand.
     * This method is called by Picocli when no subcommand is specified.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter audit log");
    }

    // -------------------------------------------------------------------------
    // log
    // -------------------------------------------------------------------------

    /**
     * Displays the full persistent audit log of all staff actions.
     * Reads from the CSV-backed audit repository so the history survives across command executions.
     */
    @Command(name = "log", description = "Display the full audit log",
             mixinStandardHelpOptions = true)
    static class LogCmd implements Runnable {

        /**
         * Executes the log retrieval and prints each entry with timestamp, staff, and action.
         * Prints a message if the audit log is empty.
         */
        @Override
        @SuppressWarnings("rawtypes")
        public void run() {
            List<AuditEntry<?>> entries = AppContext.get().auditApp().getLog();
            if (entries.isEmpty()) {
                System.out.println("Audit log is empty.");
                return;
            }
            System.out.printf("%-20s  %-12s  %-30s  %s%n",
                    "Timestamp", "Staff", "Action", "Target");
            System.out.println("-".repeat(90));
            for (AuditEntry<?> e : entries) {
                String staffName = e.getStaff() != null ? e.getStaff().getName() : "unknown";
                String target    = e.getTarget() != null ? e.getTarget().toString() : "";
                // Truncate long target strings for readability
                if (target.length() > 35) {
                    target = target.substring(0, 32) + "...";
                }
                System.out.printf("%-20s  %-12s  %-30s  %s%n",
                        e.getTimestamp(), staffName, e.getAction(), target);
            }
        }
    }
}
