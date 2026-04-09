package shelter.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import shelter.service.model.ExplanationResult;
import shelter.service.model.MatchResult;

import java.util.List;

/**
 * Top-level CLI command group for animal-adopter matching operations.
 * Provides subcommands to match animals for an adopter or adopters for an animal.
 * All operations are delegated to {@link AppContext#matchingApp()}.
 */
@Command(
        name = "match",
        description = "Run animal-adopter matching",
        subcommands = {
                MatchCmd.AnimalCmd.class,
                MatchCmd.AdopterCmd.class
        },
        mixinStandardHelpOptions = true
)
public class MatchCmd implements Runnable {

    /**
     * Prints usage help when the subcommand group is invoked without a subcommand.
     * This method is called by Picocli when no subcommand is specified.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter match <animal|adopter> --help");
    }

    // -------------------------------------------------------------------------
    // match animal  (find animals for an adopter)
    // -------------------------------------------------------------------------

    /**
     * Finds and ranks available animals in a shelter for a given adopter.
     * Only available (unadopted) matchable animals are scored and returned.
     * Results are printed in descending score order; an optional AI explanation follows.
     */
    @Command(name = "animal",
             description = "Find best-matching animals for an adopter",
             mixinStandardHelpOptions = true)
    static class AnimalCmd implements Runnable {

        /** The adopter ID to match for; required. */
        @Option(names = "--adopter", required = true, description = "Adopter ID")
        private String adopterId;

        /** The shelter ID to search in; required. */
        @Option(names = "--shelter", required = true, description = "Shelter ID")
        private String shelterId;

        /** Whether to include a structured AI-generated explanation after the ranked list. */
        @Option(names = "--explain", description = "Include AI match explanation")
        private boolean explain;

        /**
         * Executes the matching, prints ranked results, and optionally prints the AI explanation.
         * Prints a message if no available animals are found in the shelter.
         */
        @Override
        public void run() {
            try {
                List<MatchResult> results = AppContext.get().matchingApp()
                        .matchAnimalsForAdopter(adopterId, shelterId, false);
                if (results.isEmpty()) {
                    System.out.println("No available animals found in shelter " + shelterId + ".");
                    return;
                }
                // Print ranked match results table
                System.out.printf("%-4s  %-36s  %-15s  %s%n",
                        "Rank", "Animal ID", "Name", "Score");
                System.out.println("-".repeat(70));
                int rank = 1;
                for (MatchResult r : results) {
                    System.out.printf("%-4d  %-36s  %-15s  %d%n",
                            rank++, r.getAnimal().getId(), r.getAnimal().getName(), r.getScore());
                }
                // Optionally print structured explanation from the explanation service
                if (explain) {
                    ExplanationResult exp =
                            AppContext.get().explanationService().explain(results);
                    System.out.println();
                    System.out.println("=== Match Explanation ===");
                    System.out.println("Rationale   : " + exp.getMatchRationale());
                    System.out.println("Confidence  : " + exp.getConfidenceAssessment());
                    System.out.println("Advice      : " + exp.getPersonalizedAdvice());
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // match adopter  (find adopters for an animal)
    // -------------------------------------------------------------------------

    /**
     * Finds and ranks all registered adopters by compatibility with a given available animal.
     * The animal must not have been adopted. Results are printed in descending score order.
     */
    @Command(name = "adopter",
             description = "Find best-matching adopters for an animal",
             mixinStandardHelpOptions = true)
    static class AdopterCmd implements Runnable {

        /** The animal ID to match for; required. */
        @Option(names = "--animal", required = true, description = "Animal ID")
        private String animalId;

        /** Whether to include a structured AI-generated explanation after the ranked list. */
        @Option(names = "--explain", description = "Include AI match explanation")
        private boolean explain;

        /**
         * Executes the matching, prints ranked results, and optionally prints the AI explanation.
         * Prints an error if the animal is not found, is already adopted, or is not matchable.
         */
        @Override
        public void run() {
            try {
                List<MatchResult> results = AppContext.get().matchingApp()
                        .matchAdoptersForAnimal(animalId, false);
                if (results.isEmpty()) {
                    System.out.println("No adopters registered.");
                    return;
                }
                // Print ranked match results table
                System.out.printf("%-4s  %-36s  %-15s  %s%n",
                        "Rank", "Adopter ID", "Name", "Score");
                System.out.println("-".repeat(70));
                int rank = 1;
                for (MatchResult r : results) {
                    System.out.printf("%-4d  %-36s  %-15s  %d%n",
                            rank++, r.getAdopter().getId(), r.getAdopter().getName(),
                            r.getScore());
                }
                // Optionally print structured explanation from the explanation service
                if (explain) {
                    ExplanationResult exp =
                            AppContext.get().explanationService().explain(results);
                    System.out.println();
                    System.out.println("=== Match Explanation ===");
                    System.out.println("Rationale   : " + exp.getMatchRationale());
                    System.out.println("Confidence  : " + exp.getConfidenceAssessment());
                    System.out.println("Advice      : " + exp.getPersonalizedAdvice());
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
