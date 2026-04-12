# Lab 8: Advanced Patterns

## Learning Objectives

By the end of this lab, you'll understand:

- ✅ How to create **custom feeders** (beyond CSV)
- ✅ How to use **throttling** to limit throughput
- ✅ How to implement **conditional logic** in scenarios
- ✅ How to use **group()** for complex sub-scenarios
- ✅ How to handle **asynchronous patterns**
- ✅ How to build **production-ready** simulations

## Real-World Scenario

You've mastered the basics. Now for production-grade complexity:

1. **Custom feeders**: Generate realistic data on-the-fly (credit cards, IBANs, emails)
2. **Throttling**: Control maximum throughput (API limits, resource constraints)
3. **Conditional logic**: Branch based on response (A/B testing, feature flags)
4. **Groups**: Organize complex scenarios (user types, workflows)
5. **Async patterns**: Handle non-blocking operations

---

## Concept 1: Custom Feeders

### Problem with CSV Feeders
```java
csv("data/creditcards.csv")
// Requires pre-generated file
// Can't generate unlimited unique data
// Exposed sensitive test data
```

### Solution: Custom Feeder
```java
// Generate credit cards on-the-fly
Iterator<Map<String, Object>> creditCardFeeder = 
    Stream.generate(() -> {
        Map<String, Object> map = new HashMap<>();
        map.put("cardNumber", "4532" + generateRandom(12));
        map.put("expiry", "12/26");
        map.put("cvv", String.format("%03d", random.nextInt(1000)));
        return map;
    }).iterator();

// Use in scenario
.feed(new SequenceFeeder(creditCardFeeder))
.exec(http("Pay")
    .post("/payment")
    .body(StringBody("{\"cardNumber\": \"#{cardNumber}\", \"cvv\": \"#{cvv}\"}"))
)
```

---

## Concept 2: Throttling

### Problem: Unlimited Load
```java
.injectOpen(constantUsersPerSec(1000).during(60))
// 1000 new users per second - uncontrolled
```

### Solution: Throttle Requests
```java
.throttle(
    jumpToRps(50),           // Start at 50 req/sec
    holdFor(30 seconds),     // Hold for 30 sec
    jumpToRps(100),          // Jump to 100 req/sec
    holdFor(30 seconds)      // Hold for 30 sec
)
```

Result: Exactly 50, then 100 requests per second. No spikes.

---

## Concept 3: Conditional Logic

### If/Then Logic
```java
.exec(
    http("Check balance")
        .get("/account/balance")
        .check(jsonPath("$.balance").saveAs("balance"))
)
.doIf(session -> Double.parseDouble(session.getString("balance")) > 1000)
    .then(
        http("Top up account")
            .post("/account/topup")
            .body(StringBody("{\"amount\": 5000}"))
    )
```

---

## Concept 4: Groups

### Organize Sub-Scenarios
```java
ScenarioBuilder browsing = scenario("Browsing")
    .exec(http("Home").get("/"))
    .exec(http("Products").get("/products"));

ScenarioBuilder purchasing = scenario("Purchasing")
    .exec(http("Add to cart").post("/cart"))
    .exec(http("Checkout").post("/checkout"));

ScenarioBuilder combined = scenario("Full Journey")
    .group(browsing)
    .pause(2)
    .group(purchasing);

setUp(combined.injectOpen(constantUsersPerSec(10).during(60)))
```

---

## Code Pattern: Advanced Simulation

```java
public class Sim08_AdvancedPatterns extends Simulation {

    // 1. Custom Feeder
    Iterator<Map<String, Object>> dynamicFeeder = 
        Stream.generate(() -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", UUID.randomUUID().toString());
            map.put("email", "user" + random.nextInt(10000) + "@example.com");
            return map;
        }).iterator();

    // 2. Scenario with advanced patterns
    ScenarioBuilder advancedJourney = scenario("Advanced Patterns")
        .feed(new SequenceFeeder(dynamicFeeder))
        
        // Create account
        .exec(http("Register")
            .post("/register")
            .body(StringBody("{\"email\": \"#{email}\"}"))
            .check(jsonPath("$.userId").saveAs("userId"))
        )
        .pause(1, 3)
        
        // Conditional: Premium users do more
        .doIf(session -> random.nextDouble() > 0.7)
            .then(
                http("Upgrade to premium")
                    .post("/upgrade")
            )
        
        // Complex workflow with repeat
        .repeat(5)
            .on(
                exec(http("View product")
                    .get("/product/" + random.nextInt(100))
                    .check(status().is(200))
                )
                .pause(1, 2)
            );

    // 3. Setup with throttling
    {
        setUp(
            advancedJourney
                .injectOpen(constantUsersPerSec(50).during(60))
                .throttle(
                    rampRps(10).to(100).during(30 seconds),
                    holdFor(30 seconds)
                )
        )
        .assertions(
            global().responseTime().p95().lt(2000),
            global().successfulRequests().percent().gt(99.0)
        );
    }
}
```

---

## Advanced Loops

### Repeat Loops
```java
.repeat(10)
    .on(
        exec(http("Request").get("/api"))
    )
// User executes same request 10 times
```

### While Loops
```java
.asLongAs(session -> session.getInt("counter") < 100)
    .on(
        exec(session -> session.set("counter", session.getInt("counter") + 1))
    )
```

### Foreach Loops
```java
.foreach("#{ids}", "id")
    .on(
        exec(http("Get").get("/item/#{id}"))
    )
```

---

## Running the Lab

```bash
cd /Users/sgovinda/Learn/GatlingLearning

mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim08_AdvancedPatterns
```

### Expected Results

- ✅ Custom feeder generates unique data
- ✅ Throttling maintains target RPS
- ✅ Conditional branches execute
- ✅ Groups organize scenarios
- ✅ 3000+ requests total
- ✅ >99% success rate
- ✅ p95 latency <2000ms

---

## Key Takeaways

1. **Custom feeders** generate unlimited realistic data
2. **Throttling** controls throughput precisely
3. **Conditional logic** enables complex scenarios
4. **Groups** organize sub-scenarios
5. **Loops** (repeat, while, foreach) handle repetition
6. **Advanced patterns** are production-ready

---

## Navigation

**← Previous**: [Lab 7: Kafka with Feeders](07-lab-kafka-feeders.md)  
**→ Next**: [Monitoring & Analysis](01-datadog-integration.md)  
**↑ Up**: [Lab Overview](00-lab-overview.md)

