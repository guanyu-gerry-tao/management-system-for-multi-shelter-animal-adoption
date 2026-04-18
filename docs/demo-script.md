# Demo Script (Happy Path, Compound Prompts)

Four prompts cover the whole system. Each one bundles several related CLI actions so the agent demonstrates natural-language → multi-command translation. The live dashboard (`shelter print --watch` writing `~/shelter/dashboard.md`, rendered by VS Code markdown preview) updates after every batch so the audience sees shelters, animals, adopters, requests, and vaccinations all appearing together.

**Setup** (from project root; full instructions in `docs/test-live.md`):
```bash
bash scripts/init-demo.sh         # wipes ~/shelter, builds, regenerates CLAUDE.md
shelter print --watch &           # Pane A — watcher
code ~/shelter/dashboard.md       # Pane B — Cmd+K V for markdown preview
cd ~/shelter && claude            # Pane C — agent picks up ~/shelter/CLAUDE.md
# Pane D — presentation deck (flip to the matching prompt slide during each step)
cd <project>/docs/presentation && python3 -m http.server 8765 &
open http://localhost:8765/deck.html   # navigate to slide 7/12 to start
```

Warm-up (not counted): just say `hi` to the agent. It should introduce itself as the shelter management assistant without running any command.

---

**1 — Populate the system: shelters, animals, adopters** *(deck: slide 7/12)*
```
Register two shelters:
- "Boston Paws" in Boston, capacity 20
- "Cambridge Care" in Cambridge, capacity 10

Admit four animals:
- Buddy — Labrador, 3 years old, activity HIGH, size LARGE, into Boston Paws
- Luna — Siamese cat, 2 years old, activity LOW, indoor, neutered, into Boston Paws
- Fluffy — Dutch rabbit, 1 year old, activity MEDIUM, into Boston Paws
- Max — Golden Retriever, 4 years old, activity MEDIUM, size LARGE, neutered, into Cambridge Care

Register two adopters:

Alice
- Lives in a house with yard, home most of the day
- Prefers Labradors, high activity level
- Requires vaccinated animals
- Age range 1 to 5

Bob
- Lives in an apartment, away part of the day
- Prefers indoor cats, low activity
- No vaccination requirement, no age limit

Finally print the full system snapshot.
```
✅ Nine confirmation lines (2 shelter + 4 animal + 2 adopter + 1 print call), then the snapshot with Shelters / Animals / Adopters populated and everything else `(none)`.

---

**2 — Match, adopt, vaccinate** *(deck: slide 8/12)*
```
Find the best animal in Boston Paws for Alice, submit an adoption request for whichever scored highest, then approve it.

Next, set up the vaccine catalog:
- Rabies for dogs, valid 365 days
- Feline FVRCP for cats, valid 365 days

Record two vaccinations: Buddy received Rabies today, and Luna received Feline FVRCP today.

Print the snapshot at the end.
```
✅ A ranked match table (Buddy wins), a submit+approve pair, two vaccine-type additions, two vaccinations, then a snapshot showing Buddy as `adopted` and both Vaccine Types / Vaccinations sections populated.

---

**3 — Transfer workflow with a rejection, plus updates** *(deck: slide 9/12)*
```
Request a transfer for Luna from Boston Paws to Cambridge Care, then reject that request.

Submit a fresh transfer request for Luna from Boston Paws to Cambridge Care, and approve it this time.

Also make these updates while you're at it:
- Rename Max to Rocky
- Change Bob's preferred activity level to MEDIUM

Print the snapshot at the end.
```
✅ Two transfer-request lifecycles (one rejected, one approved), two update confirmations, then a snapshot where Luna is in Cambridge Care, Transfer Requests shows a REJECTED row and an APPROVED row, and Max's row now reads Rocky.

---

**4 — Errors, then audit log wrap-up** *(deck: slide 10/12)*
```
Try three operations that should fail — show me the error message for each:
1. Submit an adoption request for Bob to adopt Buddy (Buddy is already adopted).
2. Give Luna the Rabies vaccine (Rabies is for dogs only).
3. Delete Cambridge Care (it still holds animals).

After that, show me the audit log so we can review every action from this session.
```
✅ Three distinct error messages, system state unchanged, then the full audit log — a chronological table covering every registration / admit / match / adopt / vaccinate / transfer / update event performed so far.

---

## Tips for compound prompts

- **Reference entities by description, not UUID.** *"whichever scored highest"*, *"that transfer request"*, *"the animal we just admitted"* — the agent re-uses IDs from its own tool output.
- **End each step with `print` or `list`.** Confirms the action landed and makes the dashboard update visible to the audience.
- **Bundle by story, not by entity type.** Step 2 above bundles match → adopt → vaccinate because that's a single narrative; splitting them would lose the flow.
- **Approval / rejection must be explicit.** `CLAUDE.md` tells the agent to stop after `submit` or `request`. Adding "then approve" in the same prompt is the trigger to continue.
