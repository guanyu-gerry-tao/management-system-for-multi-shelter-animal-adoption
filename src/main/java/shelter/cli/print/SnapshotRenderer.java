package shelter.cli.print;

import shelter.application.model.AnimalView;
import shelter.application.model.VaccinationRecordView;
import shelter.cli.AdoptCmd;
import shelter.cli.AdopterCmd;
import shelter.cli.AnimalCmd;
import shelter.cli.AppContext;
import shelter.cli.AuditCmd;
import shelter.cli.ShelterCmd;
import shelter.cli.TransferCmd;
import shelter.cli.VaccineCmd;
import shelter.domain.Adopter;
import shelter.domain.AdoptionRequest;
import shelter.domain.Shelter;
import shelter.domain.TransferRequest;
import shelter.domain.VaccineType;
import shelter.service.model.AuditEntry;

import java.io.PrintWriter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Renders the full 8-section system snapshot as plain text.
 * Used by both {@code shelter print} (writes to stdout) and by
 * {@link MarkdownRenderer} (which wraps each section in a fenced code block).
 * Section content is delegated to the package-private {@code render*} helpers
 * on each CLI command class so formatting lives in exactly one place per entity.
 */
public final class SnapshotRenderer {

    /** Maximum number of audit entries shown in the snapshot. */
    static final int AUDIT_LIMIT = 20;

    private final Supplier<List<Shelter>> shelters;
    private final Supplier<List<AnimalView>> animalViews;
    private final Supplier<List<Adopter>> adopters;
    private final Supplier<List<AdoptionRequest>> adoptionRequests;
    private final Supplier<List<TransferRequest>> transferRequests;
    private final Supplier<List<VaccineType>> vaccineTypes;
    private final Supplier<List<VaccinationRecordView>> vaccinationRecords;
    private final Supplier<List<AuditEntry<?>>> auditLog;

    /**
     * Constructs a SnapshotRenderer from eight data-source suppliers.
     * This primary constructor makes unit testing trivial — callers inject
     * in-memory lambdas for each section without touching any singleton.
     *
     * @param shelters           supplier of the shelter list; must not be null
     * @param animalViews        supplier of the animal-view list; must not be null
     * @param adopters           supplier of the adopter list; must not be null
     * @param adoptionRequests   supplier of the adoption request list; must not be null
     * @param transferRequests   supplier of the transfer request list; must not be null
     * @param vaccineTypes       supplier of the vaccine type list; must not be null
     * @param vaccinationRecords supplier of the vaccination record view list; must not be null
     * @param auditLog           supplier of the audit entry list; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public SnapshotRenderer(Supplier<List<Shelter>> shelters,
                            Supplier<List<AnimalView>> animalViews,
                            Supplier<List<Adopter>> adopters,
                            Supplier<List<AdoptionRequest>> adoptionRequests,
                            Supplier<List<TransferRequest>> transferRequests,
                            Supplier<List<VaccineType>> vaccineTypes,
                            Supplier<List<VaccinationRecordView>> vaccinationRecords,
                            Supplier<List<AuditEntry<?>>> auditLog) {
        if (shelters == null || animalViews == null || adopters == null
                || adoptionRequests == null || transferRequests == null
                || vaccineTypes == null || vaccinationRecords == null
                || auditLog == null) {
            throw new IllegalArgumentException("All section suppliers must be non-null.");
        }
        this.shelters = shelters;
        this.animalViews = animalViews;
        this.adopters = adopters;
        this.adoptionRequests = adoptionRequests;
        this.transferRequests = transferRequests;
        this.vaccineTypes = vaccineTypes;
        this.vaccinationRecords = vaccinationRecords;
        this.auditLog = auditLog;
    }

    /**
     * Constructs a SnapshotRenderer backed by the CLI's live application context.
     * Each section fetches fresh data every time {@link #render} is called.
     *
     * @param ctx the context used to obtain application services; must not be null
     */
    public SnapshotRenderer(AppContext ctx) {
        this(
                () -> ctx.shelterApp().listShelters(),
                () -> ctx.animalApp().listAnimalsWithShelterName(null),
                () -> ctx.adopterApp().listAdopters(),
                () -> ctx.adoptionApp().listAllRequests(),
                () -> ctx.transferApp().listAllTransfers(),
                () -> ctx.vaccinationApp().listVaccineTypes(),
                () -> ctx.vaccinationApp().listAllVaccinationRecords(),
                () -> ctx.auditApp().getLog()
        );
        if (ctx == null) {
            throw new IllegalArgumentException("AppContext must not be null.");
        }
    }

    /**
     * Writes the full snapshot to the given writer in a fixed 8-section order:
     * Shelters, Animals, Adopters, Adoption Requests, Transfer Requests,
     * Vaccine Types, Vaccinations, Audit Log. Each section is introduced by a
     * {@code === TITLE ===} line and separated from the next by a blank line.
     *
     * @param out the writer to print to; must not be null
     */
    public void render(PrintWriter out) {
        section(out, "SHELTERS",          w -> ShelterCmd.renderList(w, shelters.get()));
        section(out, "ANIMALS",           w -> AnimalCmd.renderList(w, animalViews.get()));
        section(out, "ADOPTERS",          w -> AdopterCmd.renderList(w, adopters.get()));
        section(out, "ADOPTION REQUESTS", w -> AdoptCmd.renderList(w, adoptionRequests.get()));
        section(out, "TRANSFER REQUESTS", w -> TransferCmd.renderList(w, transferRequests.get()));
        section(out, "VACCINE TYPES",     w -> VaccineCmd.renderTypeList(w, vaccineTypes.get()));
        section(out, "VACCINATIONS",      w -> VaccineCmd.renderRecordList(w, vaccinationRecords.get()));
        section(out, "AUDIT LOG",         w -> AuditCmd.renderLog(w, auditLog.get(), AUDIT_LIMIT));
        out.flush();
    }

    /**
     * Writes one section header, the section body, and a trailing blank line.
     *
     * @param out   the writer to print to
     * @param title the section title (uppercase, no delimiters)
     * @param body  the consumer that writes the section body
     */
    private static void section(PrintWriter out, String title, Consumer<PrintWriter> body) {
        out.println("=== " + title + " ===");
        body.accept(out);
        out.println();
    }
}
