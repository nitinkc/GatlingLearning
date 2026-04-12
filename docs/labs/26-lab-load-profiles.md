# Lab 4: Load Profiles

## Learning Objectives

By the end of this lab, you'll understand:

- ✅ What **load profiles** are and why they matter
- ✅ How to implement **smoke tests** (minimal load)
- ✅ How to implement **ramp tests** (gradual increase)
- ✅ How to implement **step tests** (staircase increases)
- ✅ How to implement **spike tests** (sudden jumps)
- ✅ How to analyze results for each profile

## Real-World Scenario

Your product team asks: **"How does our system behave under different loads?"**

You need to answer:
- **Smoke test**: Can the system handle 1 user? (sanity check)
- **Ramp test**: What happens as load gradually increases to 100 users?
- **Step test**: At what point does performance degrade? (50 users → 100 → 200?)
- **Spike test**: Can we handle a sudden viral moment (100 → 10,000 users instantly)?

Each load profile reveals different bottlenecks.

---

## Concept: Load Profiles

### Smoke Test
```java
.injectOpen(
    constantUsersPerSec(1).during(60)  // 1 user for 1 minute
)
```

**Purpose**: "Is the system running?"

---

### Ramp Test
```java
.injectOpen(
    rampUsersPerSec(0).to(100).during(300)  // 0→100 users over 5 minutes
)
```

**Purpose**: "Where does it start struggling?"

---

### Step Test
```java
.injectOpen(
    constantUsersPerSec(50).during(60),
    rampUsersPerSec(50).to(100).during(60),
    constantUsersPerSec(100).during(60),
    rampUsersPerSec(100).to(150).during(60)
)
```

**Purpose**: "At which step does latency jump?"

---

### Spike Test
```java
.injectOpen(
    constantUsersPerSec(10).during(60),
    rampUsersPerSec(10).to(500).during(30),
    constantUsersPerSec(500).during(60),
    rampUsersPerSec(500).to(10).during(30)
)
```

**Purpose**: "Can we recover from sudden load?"

---

## Running the Lab

```bash
cd /Users/sgovinda/Learn/GatlingLearning

# Smoke Test
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim04_LoadProfileSmoke

# Ramp Test (5+ minutes)
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim04_LoadProfileRamp

# Step Test (4+ minutes)
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim04_LoadProfileStep

# Spike Test (3+ minutes)
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim04_LoadProfileSpike
```

---

## Expected Results

| Test | Requests | Success | p95 Latency |
|------|----------|---------|-------------|
| Smoke | 60+ | 100% | <1000ms |
| Ramp | 600+ | >99% | increases with load |
| Step | 400+ | >99% | shows steps |
| Spike | 1000+ | >95% | high during spike |

---

## Key Takeaways

1. **Smoke test** = sanity check
2. **Ramp test** = performance curve
3. **Step test** = threshold identification
4. **Spike test** = resilience testing
5. Each profile reveals different insights

---

## Navigation

**← Previous**: [Lab 3: Checks & Validation](25-lab-checks-validation.md)  
**→ Next**: [Lab 5: CRUD Operations](27-lab-crud-operations.md)  
**↑ Up**: [Lab Overview](22-lab-overview.md)

