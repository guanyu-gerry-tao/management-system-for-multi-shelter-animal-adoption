# Demo Script (Happy Path)

Quick demo covering all major features in ~10 commands.
IDs returned at each step are used in later commands — note them as you go.

**Setup** (run before demo):
```bash
./gradlew installDist
export PATH="$PWD/build/install/shelter/bin:$PATH"
rm -rf ~/shelter/data
```

---

**1 — Register a shelter**
> Register a shelter called "Boston Paws" in Boston with a capacity of 20.

✅ One confirmation line with the shelter name and a generated ID.

---

**2 — Admit a dog**
> Admit a 3-year-old Labrador named Buddy into Boston Paws, activity level HIGH, size LARGE.

✅ One confirmation line showing Buddy's ID and the shelter ID.

---

**3 — Admit a cat**
> Admit a 2-year-old Siamese cat named Luna into Boston Paws, activity LOW, she is indoor and neutered.

✅ One confirmation line showing Luna's ID and the shelter ID.

---

**4 — Register an adopter**
> Register an adopter named Alice — she lives in a house with yard, is home most of the day, prefers dogs, specifically Labradors, high activity level, requires vaccinated animals, age range 1 to 5.

✅ One confirmation line with Alice's name and generated ID.

---

**5 — Smart matching**
> Match animals in Boston Paws for Alice.

✅ A ranked table — Buddy should score the highest because species, breed, age, and activity all match Alice's preferences.

---

**6 — Submit adoption request**
> Alice wants to adopt Buddy, submit the request.

✅ One confirmation line showing the request ID, Alice's ID, and Buddy's ID.

---

**7 — Approve adoption**
> Approve that adoption request.

✅ One confirmation line showing the request is approved. Buddy's status is now adopted.

---

**8 — Vaccination**
> Add a vaccine type called Rabies for dogs, valid for 365 days. Then record that Buddy received it today.

✅ Two confirmation lines — one for the new vaccine type, one for the vaccination record.

---

**9 — Second shelter and transfer**
> Register a second shelter called "Cambridge Care" in Cambridge with capacity 10. Then transfer Luna from Boston Paws to Cambridge Care.

✅ Two confirmation lines — one for the new shelter, one for the pending transfer request.

---

**10 — Audit log**
> Show the audit log.

✅ A table listing every action taken this session, each row showing timestamp, staff name, action type, and target.
