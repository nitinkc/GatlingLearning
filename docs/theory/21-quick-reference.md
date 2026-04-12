# Quick Reference

## Core Syntax Cheat Sheet

### Protocol

```java
HttpProtocolBuilder http = http
    .baseUrl("https://api.example.com")
    .header("Content-Type", "application/json")
    .warmUp("https://api.example.com");
```

### Scenario with Requests

```java
ScenarioBuilder scenario = scenario("Name")
    .feed(csv("data/users.csv").circular())
    .exec(http("GET request").get("/path"))
    .pause(1)
    .exec(http("POST request").post("/path")
        .body(StringBody("{\"data\": \"value\"}")))
    .exec(http("PUT request").put("/path"))
    .exec(http("DELETE request").delete("/path"));
```

### Setup with Load Patterns

```java
{
    setUp(
        scenario
            .injectOpen(
                constantUsersPerSec(100).during(300),   // 100/sec for 5 min
                rampUsersPerSec(100).to(500).during(600),  // Ramp to 500/sec
                heavisideUsers(1000).over(300)  // Alternative ramp
            )
    )
    .protocols(http)
    .assertions(
        global().responseTime().p95().lt(500),
        global().successfulRequests().percent().gt(99.0)
    );
}
```

### Checks & Assertions

```java
.exec(http("Request")
    .get("/api")
    .check(status().is(200))
    .check(jsonPath("$.field").saveAs("varName"))
    .check(bodyString().contains("text")))

.assertions(
    global().responseTime().p95().lt(500),
    global().responseTime().p99().lt(1000),
    global().successfulRequests().percent().gt(99.0)
)
```

### Variables & Substitution

```java
// From feeder
.feed(csv("data/users.csv").circular())

// From response
.check(jsonPath("$.userId").saveAs("userId"))

// Manual
.exec(session -> session.set("var", "value"))

// Use in request
.get("/users/#{userId}")
.body(StringBody("{\"id\": \"#{userId}\"}")
```

---

## Load Patterns Quick Reference

| Pattern | Syntax | Use Case |
|---------|--------|----------|
| **Constant** | `constantUsersPerSec(100).during(300)` | Baseline testing |
| **Ramp** | `rampUsersPerSec(10).to(100).during(300)` | Find breaking point |
| **Step** | Multiple `.constantUsersPerSec()` phases | Threshold analysis |
| **Spike** | Jump load mid-test | Recovery testing |
| **Smoke** | `constantUsersPerSec(1).during(60)` | Verify simulation |

---

## Metrics to Monitor

```
p50 (median)  → 50% of requests faster than this
p95           → 95% of requests faster than this (key SLA)
p99           → 99% of requests faster than this (SLA)
p99.9         → 99.9% of requests faster than this (extreme)
RPS           → Requests per second (throughput)
Success rate  → % of successful requests
Error rate    → % of failed requests
```

---

## Common Commands

```bash
# Run specific simulation
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim01_BasicHttp

# Clean and run
mvn clean gatling:test -Dgatling.simulationClass=...

# View latest report
open target/gatling/*/index.html

# Run all simulations in package
mvn gatling:test

# Verbose output
mvn gatling:test -X
```

---

## More Resources

- [Gatling Official Docs](https://gatling.io/docs/gatling/reference/current/)
- [Gatling Injection Models](https://gatling.io/docs/gatling/reference/current/core/injection/)

---

## Next Steps

→ **Glossary**: [Glossary](22-glossary.md) - Terminology explained

