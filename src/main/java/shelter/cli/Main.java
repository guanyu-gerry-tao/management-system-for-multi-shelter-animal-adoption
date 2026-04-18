package shelter.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import shelter.startup.SystemStartupImpl;
import java.nio.file.Path;

/**
 * Entry point for the {@code shelter} CLI tool.
 * Registers all top-level subcommand groups and delegates execution to Picocli.
 * On startup, {@link SystemStartupImpl} initializes all repositories and services
 * from the directory specified by the {@code SHELTER_HOME} environment variable, or {@code ~/shelter} by default.
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
                PrintCmd.class,
                CommandLine.HelpCommand.class
        },
        mixinStandardHelpOptions = true,
        version = "1.0"
)
public class Main implements Runnable {

    /**
     * Creates a new Main instance.
     * Picocli instantiates command classes reflectively via the no-arg constructor.
     */
    public Main() {}

    /**
     * Prints a brief usage hint when the CLI is invoked with no subcommand.
     * This method is called by Picocli when the user types {@code shelter} with no arguments.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter <subcommand> --help");
        System.out.println("Subcommands: shelter, animal, adopter, adopt, transfer, match, vaccine, audit, print");
    }

    /**
     * Application entry point. Initializes Picocli and exits with the command's return code.
     * Errors thrown during command execution are handled within each command's {@code run()} method.
     *
     * @param args the CLI arguments passed from the operating system
     */
    public static void main(String[] args) {
        String shelterHomeEnv = System.getenv("SHELTER_HOME");
        Path shelterHome = shelterHomeEnv != null
                ? Path.of(shelterHomeEnv)
                : Path.of(System.getProperty("user.home"), "shelter");
        new SystemStartupImpl(shelterHome).initialize();
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
