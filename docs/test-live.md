# Live Test Plan (Claude Agent)

Full end-to-end verification before demo. Claude Code acts as AI agent — interpreting natural language and executing `shelter` CLI commands.

**Setup** (run from project root `management-system-for-multi-shelter-animal-adoption/`):
```bash
./gradlew installDist
export PATH="$PWD/build/install/shelter/bin:$PATH"
rm -rf ~/shelter/data
shelter --version
```

---

## Phase 1 — Shelter Management (UC-01)

**1.1 — Register two shelters**
> Register two shelters: the first called "Boston Paws" in Boston with capacity 15, the second called "Cambridge Care" in Cambridge with capacity 10.

✅ Two confirmation lines, each showing the shelter name and a generated ID.

---

**1.2 — List all shelters**
> List all shelters.

✅ A table with two rows showing ID, Name, Location, and Capacity columns.

---

**1.3 — Update shelter capacity**
> Change Boston Paws's capacity to 20.

✅ One confirmation line: `Updated shelter: Boston Paws (id=...)`

---

**1.4 — Delete and recreate an empty shelter**
> Delete Boston Paws, then re-register it with the same name in Boston, capacity 20.

✅ First a removal confirmation, then a registration confirmation. Empty shelters can be deleted freely; we recreate it for the next phases.

---

## Phase 2 — Animal Management (UC-02)

**2.1 — Admit a dog**
> Admit a 3-year-old Labrador named Max into Boston Paws, activity HIGH.

✅ One confirmation line: `Admitted DOG: Max (id=..., shelter=...)`

---

**2.2 — Admit a cat**
> Admit a 2-year-old Siamese cat named Luna into Boston Paws, activity LOW, indoor, neutered.

✅ One confirmation line: `Admitted CAT: Luna (id=..., shelter=...)`

---

**2.3 — Admit a rabbit**
> Admit a 1-year-old Dutch rabbit named Fluffy into Cambridge Care, activity MEDIUM.

✅ One confirmation line: `Admitted RABBIT: Fluffy (id=..., shelter=...)`

---

**2.4 — Admit an other-species animal**
> Admit a tropical fish named Nemo into Boston Paws, breed Clownfish, activity LOW, species name "fish".

✅ One confirmation line: `Admitted OTHER: Nemo (id=..., shelter=...)` — species shows as OTHER.

---

**2.5 — List animals by shelter**
> List all animals in Boston Paws.

✅ Max, Luna, and Nemo appear. Fluffy does not appear. The Shelter column shows "Boston Paws" for each row.

---

**2.6 — Update animal name**
> Rename Max to Buddy.

✅ One confirmation line: `Updated animal: Buddy (id=...)`

---

**2.7 — List all animals system-wide**
> List all animals without filtering by shelter.

✅ Buddy, Luna, Nemo, and Fluffy all appear. Each row shows its correct shelter name in the Shelter column.

---

## Phase 3 — Adopter Management (UC-03)

**3.1 — Register adopter with full preferences**
> Register an adopter named Alice — she lives in a house with yard, is home most of the day, prefers dogs, specifically Labradors, high activity level, requires vaccinated animals, age range 1 to 5.

✅ One confirmation line: `Registered adopter: Alice (id=...)`

---

**3.2 — Register adopter with minimal preferences**
> Register an adopter named Bob — he lives in an apartment, is away part of the day, prefers cats, activity LOW, no vaccination requirement, no age preference.

✅ One confirmation line: `Registered adopter: Bob (id=...)`

---

**3.3 — List all adopters**
> List all adopters.

✅ Both Alice and Bob appear. All preference columns are shown; unset fields display "any".

---

**3.4 — Update adopter preferences**
> Change Bob's schedule to HOME_MOST_OF_DAY and his preferred activity level to MEDIUM.

✅ One confirmation line: `Updated adopter: Bob (id=...)`

---

## Phase 4 — Matching (UC-04)

**4.1 — Match animals for an adopter**
> Match animals in Boston Paws for Alice.

✅ A ranked table with Rank, Animal ID, Name, and Score columns. Buddy should rank first — species, breed, and age all match Alice's preferences.

---

**4.2 — Match adopters for an animal**
> Find the best adopters for Buddy.

✅ A ranked table with Rank, Adopter ID, Name, and Score columns. Alice should rank first.

---

## Phase 5 — Adoption Workflow (UC-05)

**5.1 — Submit adoption request**
> Alice wants to adopt Buddy — submit the request.

✅ One confirmation line: `Submitted adoption request: id=... (adopter=..., animal=...)`

---

**5.2 — Approve adoption**
> Approve that adoption request.

✅ One confirmation line: `Approved adoption request: ...`

---

**5.3 — Verify adopted status**
> List animals in Boston Paws and check Buddy's status.

✅ Buddy's row shows `adopted`; Luna and Nemo show `available`.

---

**5.4 — Reject a second request on an adopted animal**
> Try to submit another adoption request for Buddy on behalf of Bob.

✅ Error — the animal is already adopted.

---

**5.5 — Reject an adoption request**
> Bob wants to adopt Luna — submit the request, then reject it.

✅ Submission succeeds. Rejection outputs: `Rejected adoption request: ...`

---

**5.6 — Cancel an adoption request**
> Submit another adoption request for Bob to adopt Luna, then cancel it.

✅ Submission succeeds. Cancellation outputs: `Cancelled adoption request: ...`

---

## Phase 6 — Transfer Workflow (UC-06)

**6.1 — Request a transfer**
> Transfer Luna from Boston Paws to Cambridge Care.

✅ One confirmation line: `Transfer request created: id=... (animal=..., Boston Paws → Cambridge Care)`

---

**6.2 — Approve the transfer**
> Approve that transfer request.

✅ One confirmation line: `Approved transfer request: ...`

---

**6.3 — Verify transfer result**
> List animals in Cambridge Care.

✅ Both Luna and Fluffy appear.

---

**6.4 — Reject a transfer request**
> Transfer Nemo from Boston Paws to Cambridge Care, then reject it.

✅ Request creation succeeds. Rejection outputs: `Rejected transfer request: ...`

---

## Phase 7 — Vaccination Management (UC-07)

**7.1 — Add vaccine types**
> Add two vaccine types: Rabies for dogs, valid 365 days; Feline FVRCP for cats, valid 365 days.

✅ Two confirmation lines: `Added vaccine type: Rabies (id=...)` and `Added vaccine type: Feline FVRCP (id=...)`

---

**7.2 — List vaccine types**
> List all vaccine types.

✅ Both Rabies and Feline FVRCP appear.

---

**7.3 — Record a vaccination**
> Give Buddy the Rabies vaccine today.

✅ One confirmation line: `Recorded vaccination: animal=..., type=Rabies, date=...`

---

**7.4 — Check overdue vaccinations**
> Check whether Buddy has any overdue vaccinations.

✅ Output confirms all vaccinations are current for Buddy.

---

**7.5 — Species mismatch error**
> Try to give Luna the Rabies vaccine (which is for dogs only).

✅ Error — species mismatch. Luna is a cat; Rabies is only applicable to dogs.

---

**7.6 — Update a vaccine type**
> Rename Feline FVRCP to Cat FVRCP.

✅ One confirmation line: `Updated vaccine type: Cat FVRCP (id=...)`

---

## Phase 8 — Audit Log (UC-08)

**8.1 — View audit log**
> Show the audit log.

✅ A table with Timestamp, Staff, Action, and Target columns covering every operation performed in this session.

---

## Phase 9 — Error Cases

**9.1 — Remove an animal without pending requests**
> Delete Fluffy — Fluffy has no pending adoption requests.

✅ One confirmation line: `Removed animal: ...`

---

**9.2 — Remove a shelter that still holds animals**
> Try to delete Cambridge Care — it still has Luna inside.

✅ Error — shelter still holds animals.

---

**9.3 — Look up a non-existent ID**
> Look up shelter with ID "abc-123".

✅ Error — not found.

---

## Pass Criteria

- [ ] All ✅ steps produce the expected output
- [ ] All error cases print a clear error message without crashing
- [ ] `shelter audit log` shows the complete operation history
- [ ] After restarting (re-running any list command), all data is still present
