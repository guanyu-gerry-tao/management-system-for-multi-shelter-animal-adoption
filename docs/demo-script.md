# Demo Script (Happy Path, Short Prompts)

Four turns: a warm-up `hi`, a registration batch, a matching batch, and an audit log. Short prompts let the agent do the translation work in front of the audience. The live dashboard (`shelter print --watch` writing `~/shelter/dashboard.md`) updates after every batch.

**Setup** (from project root; full instructions in `docs/test-live.md`):
```bash
bash scripts/init-demo.sh         # wipes ~/shelter, builds, regenerates CLAUDE.md
shelter print --watch &           # Pane A — watcher
code ~/shelter/dashboard.md       # Pane B — Cmd+K V for markdown preview
cd ~/shelter && claude            # Pane C — agent
```

---

**0 — Warm-up**
```
hi
```
✅ Agent introduces itself as the shelter management assistant. No commands run.

---

**1 — Register shelters, animals, adopters** *(deck: slide 7/12)*
```
Please do the following jobs for me:

Register:

Shelters
- Boston Paws, Boston, capacity 20
- Cambridge Care, Cambridge, capacity 10

Animals (all into Boston Paws except Max)
- Buddy, Labrador, 3y, HIGH, LARGE, vaccinated
- Luna, Siamese cat, 2y, LOW, indoor, neutered
- Fluffy, Dutch rabbit, 1y, MEDIUM
- Max, Golden Retriever, 4y, MEDIUM, LARGE, neutered → Cambridge Care

Adopters
- Alice: house w/ yard, home most of day, wants Labrador HIGH, vaccinated, age 1–5
- Bob: apartment, away part of day, wants indoor cat LOW
```
✅ 2 shelters + 4 animals + 2 adopters + 1 print. Dashboard shows everything populated.

---

**2 — Match** *(deck: slide 8/12)*
```
Find Alice's best match in Boston Paws. Then find Bob's best match in Boston Paws.
```
✅ Two ranked tables. Buddy tops Alice's (species + breed + age + activity + vaccinated all match). Luna tops Bob's (cat + low activity + apartment/indoor lifestyle).

---

**3 — Audit log** *(deck: slide 10/12)*
```
Show me the audit log.
```
✅ Chronological table of every action from this session — registrations, admits, matches.

---

## Tips

- **Short prompts, bundled intent.** The agent reads the bullets and fires the right commands in order.
- **End with `print` or `list`** so the dashboard visibly updates.
- **Reference by name, not UUID.** The agent carries IDs across calls.
