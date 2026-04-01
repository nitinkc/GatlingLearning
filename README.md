# Gatling Learning Project

A **self-contained, tutorial-style** project for systematically learning the [Gatling](https://gatling.io/) load testing framework — progressing from simple HTTP requests to parameterised Kafka producer simulations.

> **No cloud accounts, no secrets, no tokens required.**
> HTTP simulations target the free public API [jsonplaceholder.typicode.com](https://jsonplaceholder.typicode.com).
> Kafka simulations use an **embedded Kafka broker** started by [Testcontainers](https://testcontainers.com/) — just Docker.

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker | any recent | Only needed for Kafka sims (Sim06, Sim07) |

---

## Project Structure

```
gatling-learning/
├── pom.xml                         ← Single Maven module (no internal libs)
├── .github/workflows/
│   └── gatling-ci.yml              ← GitHub Actions CI pipeline
└── src/test/
    ├── java/io/learn/gatling/
    │   ├── common/
    │   │   └── EmbeddedKafka.java  ← Shared Testcontainers Kafka broker
    │   └── simulations/
    │       ├── http/
    │       │   ├── Sim01_BasicHttp.java         ← Lesson 1
    │       │   ├── Sim02_HttpWithFeeders.java   ← Lesson 2
    │       │   ├── Sim03_HttpChecks.java        ← Lesson 3
    │       │   ├── Sim04_LoadProfiles.java      ← Lesson 4
    │       │   └── Sim05_CRUD.java              ← Lesson 5
    │       └── kafka/
    │           ├── Sim06_BasicKafkaProducer.java ← Lesson 6
    │           └── Sim07_KafkaWithFeeders.java   ← Lesson 7
    └── resources/
        ├── data/
        │   ├── users.csv       ← CSV feeder (userId, username)
        │   ├── products.csv    ← CSV feeder (productId, productName, price)
        │   ├── stores.csv      ← CSV feeder (storeId, storeName)
        │   └── posts.json      ← JSON array feeder
        ├── bodies/
        │   ├── update_post.json    ← ElFileBody template (Gatling EL #{var})
        │   ├── product_event.json  ← Kafka message template ({{placeholder}})
        │   └── inventory_event.json
        ├── gatling.conf        ← Gatling configuration
        └── logback.xml         ← Logging configuration
```

---

## Learning Path

Work through the simulations **in order**. Each one builds on the previous.

### HTTP Track

#### Sim01 — Basic HTTP *(start here)*
**Concepts:** HttpProtocol · ScenarioBuilder · setUp() · GET requests · pauses · constantUsersPerSec

The three building blocks every Gatling simulation needs:
```
HttpProtocol  →  "where to send requests"
ScenarioBuilder →  "what virtual users do"
setUp()         →  "how many users, how fast"
```

```bash
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim01_BasicHttp
```

---

#### Sim02 — HTTP with Feeders
**Concepts:** CSV feeders · JSON feeders · `#{variable}` EL syntax · feeder strategies (circular/random/queue) · rampUsersPerSec

A **feeder** injects a row of data into each virtual user's session:
```java
FeederBuilder<String> userFeeder = csv("data/users.csv").circular();

scenario("...")
    .feed(userFeeder)                       // inject a row
    .exec(http("...").get("/users/#{userId}"))  // use the value
```

```bash
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim02_HttpWithFeeders
```

---

#### Sim03 — Checks & Assertions
**Concepts:** `status().is()` · `jsonPath().saveAs()` · `bodyString().exists()` · session correlation · `doIf()` · global assertions (SLAs)

**Checks** validate responses. **Assertions** define pass/fail SLAs for the whole run:
```java
.check(
    status().is(201),
    jsonPath("$.id").saveAs("createdPostId")  // ← save to session
)
// later in the same scenario:
.get("/posts/#{createdPostId}")               // ← use from session
```

Assertions run after the simulation and fail the Maven build if violated:
```java
.assertions(
    global().responseTime().percentile(95).lt(3000),
    global().successfulRequests().percent().gte(99.0)
)
```

```bash
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim03_HttpChecks
```

---

#### Sim04 — Load Profiles
**Concepts:** All injection models · throttle · maxDuration · smoke / ramp / step / spike profiles

The file contains **four profiles** — read the comments and uncomment the profile you want:

| Profile | Purpose | When to use |
|---------|---------|-------------|
| **SMOKE** | 1 user, single pass | First run to check the sim compiles |
| **RAMP** | Slow increase → hold | Find breaking point |
| **STEP LOAD** | Staircase up | Find max sustainable throughput (mirrors dps-load's `OPEN_STEP_LOAD`) |
| **SPIKE** | Sudden burst | Test auto-scaling / circuit breakers |

```bash
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim04_LoadProfiles
```

---

#### Sim05 — CRUD (PUT, PATCH, DELETE)
**Concepts:** `put()` · `patch()` · `delete()` · `ElFileBody` (body from file with EL substitution) · `RawFileBody`

```java
.body(ElFileBody("bodies/update_post.json"))  // file + #{variable} substitution
```

```bash
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim05_CRUD
```

---

### Kafka Track

> **Requirement:** Docker must be running. The first run pulls the Kafka Docker image (~500 MB). Subsequent runs reuse the cached image and start in ~10 seconds.

#### Sim06 — Basic Kafka Producer
**Concepts:** `exec(session -> ...)` · KafkaProducer setup · `send().get()` (sync) · `markAsSucceeded()` / `markAsFailed()` · EmbeddedKafka (Testcontainers)

Unlike HTTP, Gatling has **no built-in Kafka protocol**. We call the Kafka Java client directly inside `exec()`:

```java
.exec(session -> {
    producer.send(new ProducerRecord<>(topic, key, message)).get();
    return session.markAsSucceeded();
})
```

```bash
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.kafka.Sim06_BasicKafkaProducer
```

---

#### Sim07 — Kafka with Feeders
**Concepts:** CSV feeders + Kafka · async sends with callbacks · message key routing · multiple scenarios on one broker · `{{placeholder}}` templates loaded from disk

Mirrors the pattern used in production `dps-load` Kafka simulations:
- Load a JSON template from `src/test/resources/bodies/`
- Replace `{{placeholder}}` tokens with feeder values
- Send to a topic with the entity ID as the Kafka key (partition routing)

```bash
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.kafka.Sim07_KafkaWithFeeders
```

---

## Quick Reference: Run Commands

```bash
# Smoke test — always run this first to verify the simulation is valid
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim04_LoadProfiles

# All HTTP simulations (no Docker needed)
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim01_BasicHttp
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim02_HttpWithFeeders
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim03_HttpChecks
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim05_CRUD

# Kafka simulations (Docker required)
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.kafka.Sim06_BasicKafkaProducer
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.kafka.Sim07_KafkaWithFeeders
```

After each run, open the HTML report:
```bash
open target/gatling/*/index.html        # macOS
xdg-open target/gatling/*/index.html   # Linux
```

---

## CI Pipeline

The GitHub Actions workflow (`.github/workflows/gatling-ci.yml`) runs automatically on every push to `main`/`develop`:

- **Job 1 – HTTP:** Runs Sim01–Sim05. No Docker, no secrets.
- **Job 2 – Kafka:** Runs Sim06–Sim07. GitHub-hosted runners have Docker pre-installed.
- Reports are uploaded as **build artifacts** and kept for 7 days.

---

## Key Concepts Summary

| Concept | Where demonstrated | Notes |
|---------|-------------------|-------|
| `HttpProtocol` | Sim01–Sim05 | Base URL, headers, warm-up |
| `scenario()` | All sims | Virtual user journey |
| `exec(http(...))` | Sim01–Sim05 | HTTP request action |
| `exec(session -> ...)` | Sim06–Sim07 | Any Java code (used for Kafka) |
| CSV Feeder | Sim02, Sim05, Sim07 | `csv("file").circular()` |
| JSON Feeder | Sim02 | `jsonFile("file").random()` |
| `#{expression}` EL | Sim02–Sim05 | Session variable injection |
| `ElFileBody` | Sim05 | Body from file + EL substitution |
| `status().is()` | Sim03–Sim05 | HTTP status check |
| `jsonPath().saveAs()` | Sim03, Sim05 | Extract & correlate responses |
| `global().assertions()` | Sim03–Sim04 | SLA / pass-fail criteria |
| Load profiles | Sim04 | smoke / ramp / step / spike |
| EmbeddedKafka | Sim06–Sim07 | Testcontainers, no cloud needed |
| `KafkaProducer` | Sim06–Sim07 | Apache Kafka Java client |
| Async Kafka send | Sim07 | `send(record, callback)` |
| Sync Kafka send | Sim06 | `send(record).get()` |

---

## Connection to dps-load

This project mirrors the architecture of the production `dps-load` project but strips away enterprise-specific layers so the Gatling concepts are visible:

| dps-load | This project |
|----------|-------------|
| `dps-core-kafka` (internal lib) | Apache Kafka client directly |
| `KafkaSender` bean (Spring) | Plain `KafkaProducer` |
| Azure Keyvault for Kafka creds | No credentials (Testcontainers) |
| External Kafka cluster | Embedded Kafka via Testcontainers |
| `ApplicationConfig` / env vars | No external config |
| `OPEN_STEP_LOAD` model | Sim04 step load profile |
| `CommonSimulation` base class | Inline in each simulation (easier to follow) |
| Helm charts for deployment | GitHub Actions only |

---

## Further Reading

- [Gatling Java API reference](https://gatling.io/docs/gatling/reference/current/core/simulation/)
- [Gatling HTTP protocol](https://gatling.io/docs/gatling/reference/current/http/protocol/)
- [Gatling injection models](https://gatling.io/docs/gatling/reference/current/core/injection/)
- [Testcontainers Kafka](https://java.testcontainers.org/modules/kafka/)
- [Apache Kafka Producer API](https://kafka.apache.org/documentation/#producerapi)
