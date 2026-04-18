package shelter.startup;

import shelter.exception.DataPersistenceException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Prepares the shelter demo work directory before repositories load data.
 * This class owns file-system setup such as creating the base directory,
 * the data directory, and first-launch context files.
 */
public class WorkdirBootstrapper {

    private static final String CLAUDE_FILE_NAME = "CLAUDE.md";
    private static final String DATA_DIR_NAME = "data";
    private static final String CLAUDE_TEMPLATE = """
            # CLAUDE.md - Shelter CLI Demo Context

            ## Agent Behavior Rules (enforced during demo)

            **Language: English only.** All responses must be in English regardless of the
            language the user speaks. Do not switch to any other language.

            **Ignore personal memory.** Do not apply, cite, or reference any memory stored
            about the user (e.g. in ~/.claude/). Respond only based on the current conversation
            and this CLAUDE.md file.

            **Deletion requires confirmation.** Before executing any `remove` command
            (shelter remove, animal remove, adopter remove, vaccine type remove), ask the user
            to confirm. Do not proceed until the user explicitly says yes.

            **Approval-step commands: submit only, then wait.** When the user asks to submit
            an adoption or transfer request, only run the `submit` / `request` command. Do NOT
            automatically follow up with `approve`, `reject`, or `cancel`. Stop after creation
            and wait for an explicit instruction before running the next step.

            **Always render results as tables with all fields.** Whenever you print, list, or
            display any data returned by a `shelter` command, format the output as a table
            (markdown table is fine) and include every field returned — do not omit, summarize,
            or hide any column, even if the value is `N/A`, `any`, or empty.

            ## Project Context

            This directory is the runtime work directory for a Java multi-shelter animal adoption
            management system. The system is operated through the `shelter` CLI and manages
            shelters, animals, adopters, adoption requests, transfer requests, matching,
            vaccination records, vaccine types, and audit logs.

            ## Binary Location

            Assume the `shelter` command is available on PATH. If it is not, add
            `<project>/build/install/shelter/bin` to PATH after building the project.

            ## Command Reference

            General:
            - `shelter --help`
            - `shelter <subcommand> --help`

            Shelter:
            - `shelter shelter list`
            - `shelter shelter register --name <name> --location <location> --capacity <capacity>`
            - `shelter shelter update --id <shelter-id> [--name <name>] [--location <location>] [--capacity <capacity>]`
            - `shelter shelter remove --id <shelter-id>`

            Animal:
            - `shelter animal list [--shelter <shelter-id>]`
              (columns: ID / Species / Name / Breed / Age / Activity / Neutered / Indoor / Size / Fur / Shelter / Status)
              (species-specific columns show N/A when not applicable; unset fields show their current value)
            - `shelter animal admit --species dog --name <name> --breed <breed> (--birthday <yyyy-mm-dd> | --age <years>) --activity <LOW|MEDIUM|HIGH> --shelter <shelter-id> [--size <SMALL|MEDIUM|LARGE>] [--neutered]`
            - `shelter animal admit --species cat --name <name> --breed <breed> (--birthday <yyyy-mm-dd> | --age <years>) --activity <LOW|MEDIUM|HIGH> --shelter <shelter-id> [--indoor] [--neutered]`
            - `shelter animal admit --species rabbit --name <name> --breed <breed> (--birthday <yyyy-mm-dd> | --age <years>) --activity <LOW|MEDIUM|HIGH> --shelter <shelter-id> [--fur <SHORT|LONG>]`
            - `shelter animal admit --species other --name <name> --breed <description> (--birthday <yyyy-mm-dd> | --age <years>) --activity <LOW|MEDIUM|HIGH> --shelter <shelter-id> [--species-name <name>]`
            - `shelter animal update --id <animal-id> [--name <name>] [--activity <LOW|MEDIUM|HIGH>] [--neutered <true|false>]`
              (--neutered applies to dogs and cats only; ignored for rabbit/other)
            - `shelter animal remove --id <animal-id>`

            Adopter:
            - `shelter adopter list`
              (columns: ID / Name / Living Space / Schedule / Species / Breed / Activity / Vaccinated / Min Age / Max Age)
              (unset preference fields show "any")
            - `shelter adopter register --name <name> --space <APARTMENT|HOUSE_NO_YARD|HOUSE_WITH_YARD> --schedule <HOME_MOST_OF_DAY|AWAY_PART_OF_DAY|AWAY_MOST_OF_DAY> [--species <DOG|CAT|RABBIT|OTHER>] [--breed <breed>] [--activity <LOW|MEDIUM|HIGH>] [--requires-vaccinated <true|false>] [--min-age <years>] [--max-age <years>]`
            - `shelter adopter update --id <adopter-id> [--name <name>] [--space <APARTMENT|HOUSE_NO_YARD|HOUSE_WITH_YARD>] [--schedule <HOME_MOST_OF_DAY|AWAY_PART_OF_DAY|AWAY_MOST_OF_DAY>] [--species <DOG|CAT|RABBIT|OTHER>] [--breed <breed>] [--activity <LOW|MEDIUM|HIGH>] [--requires-vaccinated <true|false>] [--min-age <years>] [--max-age <years>]`
            - `shelter adopter remove --id <adopter-id>`

            Adopt:
            - `shelter adopt list`
              (columns: ID / Adopter / Animal / Status / Submitted At)
            - `shelter adopt submit --adopter <adopter-id> --animal <animal-id>`
            - `shelter adopt approve --request <request-id>`
            - `shelter adopt reject --request <request-id>`
            - `shelter adopt cancel --request <request-id>`

            Transfer:
            - `shelter transfer list`
              (columns: ID / Animal / From / To / Status / Requested At)
            - `shelter transfer request --animal <animal-id> --from <source-shelter-id> --to <destination-shelter-id>`
            - `shelter transfer approve --request <request-id>`
            - `shelter transfer reject --request <request-id>`
            - `shelter transfer cancel --request <request-id>`

            Match:
            - `shelter match animal --adopter <adopter-id> --shelter <shelter-id>`
            - `shelter match adopter --animal <animal-id>`

            Vaccine:
            - `shelter vaccine list`
              (columns: ID / Animal / Species / Vaccine / Date)
            - `shelter vaccine record --animal <animal-id> --type <vaccine-type-name> --date <yyyy-mm-dd>`
            - `shelter vaccine overdue --animal <animal-id>`
            - `shelter vaccine type list`
            - `shelter vaccine type add --name <name> --species <DOG|CAT|RABBIT|OTHER> --days <validity-days>`
            - `shelter vaccine type update --id <vaccine-type-id> [--name <name>] [--species <DOG|CAT|RABBIT|OTHER>] [--days <validity-days>]`
            - `shelter vaccine type remove --id <vaccine-type-id>`

            Audit:
            - `shelter audit log`

            Print / Live Dashboard:
            - `shelter print`
              Prints the full 8-section system snapshot to stdout
              (sections: Shelters, Animals, Adopters, Adoption Requests, Transfer Requests,
              Vaccine Types, Vaccinations, Audit Log).
            - `shelter print --watch [--out <path>]`
              Keeps running and rewrites a markdown file whenever CSV contents change.
              Default output path is `~/shelter/dashboard.md`. Ideal for a demo: open the
              file with VS Code's markdown preview in a side pane, run this command in a
              terminal, then run any `shelter` command in another terminal and watch the
              preview update within a second.

            ## Natural Language To CLI Guidance

            The user may speak in natural language. Translate their intent into one or more
            `shelter` CLI calls. Always list shelters, adopters, animals, vaccine types, or
            requests first if you need an ID.

            After running any shelter match command, use the ranked output as the source of truth
            and explain the top match to the user in natural language.

            ## Data Directory

            Data persists as CSV files under `~/shelter/data/` and survives across sessions.
            """;

    /**
     * Creates the required startup files and folders under the given shelter home directory.
     * Existing user-edited files are preserved so startup is safe to run repeatedly.
     *
     * @param shelterHome the base shelter work directory, such as {@code ~/shelter}
     */
    public void bootstrap(Path shelterHome) {
        Objects.requireNonNull(shelterHome, "Shelter home must not be null.");

        try {
            Files.createDirectories(shelterHome);
            Files.createDirectories(shelterHome.resolve(DATA_DIR_NAME));
            createClaudeFileIfMissing(shelterHome.resolve(CLAUDE_FILE_NAME));
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to bootstrap shelter work directory.", e);
        }
    }

    private void createClaudeFileIfMissing(Path claudeFile) throws IOException {
        if (Files.exists(claudeFile)) {
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                claudeFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE)) {
            writer.write(CLAUDE_TEMPLATE);
        }
    }
}
