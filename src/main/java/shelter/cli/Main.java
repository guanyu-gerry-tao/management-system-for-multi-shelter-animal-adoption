package shelter.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Entry point for the {@code shelter} CLI tool.
 * Registers all top-level subcommand groups and delegates execution to Picocli.
 * On first invocation, {@link AppContext#get()} initializes all repositories and services
 * from {@code ~/shelter/data/}; subsequent calls reuse the same wired instance.
 */
@Command(
        name = "shelter",
        description = "Multi-shelter animal adoption management system",
        subcommands = {
                ShelterCmd.class,
                AnimalCmd.class,
                AdopterCmd.class,
                AdoptCmd.class,
                TransferCmd.class,
                MatchCmd.class,
                VaccineCmd.class,
                AuditCmd.class,
                CommandLine.HelpCommand.class
        },
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class Main implements Runnable {

    /**
     * Prints a brief usage hint when the CLI is invoked with no subcommand.
     * This method is called by Picocli when the user types {@code shelter} with no arguments.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter <subcommand> --help");
        System.out.println("Subcommands: shelter, animal, adopter, adopt, transfer, match, vaccine, audit");
    }

    /**
     * Application entry point. Initializes Picocli and exits with the command's return code.
     * Errors thrown during command execution are handled within each command's {@code run()} method.
     *
     * @param args the CLI arguments passed from the operating system
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
