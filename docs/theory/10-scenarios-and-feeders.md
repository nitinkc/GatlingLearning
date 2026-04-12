# Scenarios & Feeders

## Scenarios: User Journeys

A scenario is a sequence of actions (requests, pauses, logic) that a virtual user executes.

```java
ScenarioBuilder userJourney = scenario("E-commerce User")
    .feed(userFeeder)                    // Get data
    .exec(http("Browse").get("/products"))  // Request 1
    .pause(5, 10)                       // Think-time
    .exec(http("View Details").get("/product/#{productId}"))  // Request 2
    .pause(3, 5)
    .exec(http("Purchase").post("/checkout")
        .body(StringBody("{...}")))     // Request 3
    .pause(2)
```

---

## Scenario Execution Flow

When you run a Gatling simulation, here is the exact sequence:

```
1. Simulation Starts
   ↓
2. Protocol Configuration Loaded (baseUrl, headers, etc.)
   ↓
3. Feeders Initialized (CSV data, random generators, etc.)
   ↓
4. Virtual Users Spawned (according to injection profile)
   ├─ User 1 starts scenario
   ├─ User 2 starts scenario
   └─ ... (up to configured load)
   ↓
5. Each User Executes Scenario Steps Sequentially
   ├─ Send request 1
   ├─ Wait/pause
   ├─ Send request 2
   ├─ Extract data from response
   └─ Continue until scenario ends
   ↓
6. User Completes One Iteration
   ↓
7. User Restarts Scenario (loops by default)
   ↓
8. All Users Stop When Test Duration Expires
```

### Complete Scenario Structure

```java
ScenarioBuilder scenario = scenario("Scenario Name")
    // 1. SETUP: Initialize data/variables
    .exec(session -> session.set("userId", "user-123"))

    // 2. ACTION: Execute requests
    .exec(http("Request 1").get("/api/users/#{userId}"))
    .pause(1, 3)  // Think time between requests

    // 3. EXTRACT: Parse response and store for next request
    .exec(http("Request 2")
        .get("/api/products")
        .check(jsonPath("$.items[0].id").saveAs("productId")))

    // 4. LOOP: Repeat block
    .repeat(5) {
        exec(http("Request 3")
            .post("/api/cart")
            .body(StringBody("{\"productId\":\"#{productId}\"}")))
    }
```

---

## Feeders: Data Injection

**Feeders** provide external data to your scenarios (user IDs, email addresses, CSV records, etc.). Without feeders, you'd be testing the same data repeatedly — which isn't realistic.

### Feeder Types

#### 1. CSV Feeders (File-based)

```java
FeederBuilder<String> userFeeder = csv("data/users.csv").circular();

scenario("User Journey")
    .feed(userFeeder)  // Gets one row per iteration
    .exec(http("Get User").get("/api/users/#{userId}"))
```

CSV file (`data/users.csv`):
```
userId,username,email
123,alice,alice@example.com
456,bob,bob@example.com
789,charlie,charlie@example.com
```

#### 2. JSON Feeders

```java
FeederBuilder jsonFeeder = jsonFile("data/products.json").random();

scenario("Product Test")
    .feed(jsonFeeder)  // Gets random object from JSON array
    .exec(http("View Product").get("/product/#{productId}"))
```

#### 3. Auto Feeders (Generated On-the-fly)

Used in Kafka scenarios with `feederType: "auto"` in config:

```java
// Generate data automatically per message
scenario("Auto Generated Test")
    .feed(Iterator.continually(new HashMap<String, Object>() {{
        put("userId", "user-" + System.currentTimeMillis());
        put("amount", 100.0 + Math.random() * 900.0);
        put("timestamp", Instant.now().toString());
    }}))
    .exec(kafkaSend(...))
```

### Feeder Strategies

```java
csv("data/users.csv").circular()  // Loop back to start when exhausted
csv("data/users.csv").random()    // Randomize order
csv("data/users.csv").shuffle()   // Shuffle each cycle
csv("data/users.csv").queue()     // Stop when exhausted (limited test data)
```

### Feeder Variable Interpolation

When you use `#{variableName}` in requests, Gatling:
1. Looks up `variableName` in the current session
2. Replaces `#{variableName}` with the actual value
3. Sends the request with real data

```java
// From feeder: userId = "user-123"
scenario("Example")
    .feed(feeder)
    .exec(http("Get User").get("/api/users/#{userId}"))   // → /api/users/user-123
    .exec(session -> session.set("userName", "John"))
    .exec(http("Update User")
        .post("/api/users/#{userId}")
        .body(StringBody("{\"name\": \"#{userName}\"}"))) // → "John"
```

---

## Configuration to Code Mapping

Your JSON config maps directly to Gatling like this:

```json
{
    "scnName": "kafkaCreateScenario",
    "templateRequestFile": "kfk_create.json",
    "path": "${EXEC_ENV}-rtdx-salestxn-syndication",
    "loadTPS": 100,
    "feederType": "auto"
}
```

Maps to:

```java
ScenarioBuilder kafkaCreateScenario = scenario("kafkaCreateScenario")
    .feed(autoDataFeeder())               // ← feederType: "auto"
    .exec(
        kafka("Send Message")
            .send("${EXEC_ENV}-rtdx-salestxn-syndication")  // ← path
            .payload(loadJsonTemplate("kfk_create.json"))   // ← templateRequestFile
    )
    .pause(Duration.ofMillis(1000 / 100)); // ← loadTPS: 100

setUp(
    kafkaCreateScenario.injectOpen(constantUsersPerSec(100).during(300))
)
```

---

## Understanding TPS

**TPS (Transactions Per Second)** = messages/requests sent per second from your load test.

Gatling uses an **open workload model** — it injects requests at a fixed rate regardless of service backpressure. It keeps sending at the target rate and measures failures/latency.

For Kafka, one "transaction" = one `send()` call, so:
- **200 TPS** = 200 messages/sec
- **500 TPS** = 500 messages/sec
- **1000 TPS** = 1000 messages/sec (double production load)

### Scenario Execution Timeline at 100 TPS

```
Time    | User 1           | User 2           | User 3           | Notes
--------|------------------|------------------|------------------|------------------
0s      | Feed data 001    | Feed data 002    | Feed data 003    | 100 users created
        | Send msg 001     | Send msg 002     | Send msg 003     |
        | Wait 10ms        | Wait 10ms        | Wait 10ms        | 100 TPS = 10ms/msg
10ms    | Feed data 004    | Feed data 005    | Feed data 006    | Feeder cycles
        | Send msg 004     | Send msg 005     | Send msg 006     |
...     |                  |                  |                  |
300s    | Test Duration    | Test Duration    | Test Duration    | All users exit
(5min)  | Exceeded         | Exceeded         | Exceeded         | Total: 30,000 msgs
```

`100 TPS` → `1000ms / 100 = 10ms pause between messages` per user

---

## Session & Variable Substitution

Each virtual user has a session with variables:

```java
.feed(csv("data/users.csv").circular())           // from feeder
.exec(http("Create User")
    .post("/users")
    .check(jsonPath("$.userId").saveAs("newUserId")))  // from response
.exec(session -> session.set("customVar", "value"))   // from code

// All available in subsequent requests
.exec(http("Use Variable")
    .get("/users/#{userId}")                       // from feeder
    .body(StringBody("{\"id\": \"#{newUserId}\"}"))) // from response
```

### Key Points About Sessions

1. **Each Virtual User is Independent** — users don't share sessions
2. **Feeders provide data per iteration** — each loop gets the next row
3. **Variable scope is per-session** — user 1's data doesn't affect user 2
4. **Pause/Think time is critical** — simulates real users, prevents unrealistic hammering

---

## Advanced Scenarios

### Conditional Logic

```java
scenario("Conditional Test")
    .feed(userFeeder)
    .exec(http("Check Status").get("/account/#{userId}")
        .check(jsonPath("$.status").saveAs("status")))
    .doIf(session -> "premium".equals(session.getString("status"))) {
        exec(http("Premium Feature").post("/premium-action"))
    }
```

### Loops

```java
scenario("Repeat Actions")
    .feed(userFeeder)
    .repeat(5) {
        exec(http("Request").get("/data"))
        .pause(1)
    }
```

### Groups (for reporting)

```java
scenario("Grouped Actions")
    .exec(group("Authentication") {
        exec(http("Login").post("/login"))
    })
    .exec(group("Shopping") {
        exec(http("Browse").get("/products"))
        .pause(2)
        .exec(http("Add to Cart").post("/cart"))
    })
```

---

## Custom Feeders

For complex scenarios, build a custom feeder with full business logic:

```java
private Feeder<Object> customBusinessLogicFeeder() {
    return new Iterator<Map<String, Object>>() {
        @Override
        public boolean hasNext() { return true; }

        @Override
        public Map<String, Object> next() {
            Map<String, Object> data = new HashMap<>();
            data.put("copeId", "COPE-" + (int)(Math.random() * 100));
            data.put("amount", 1000 + (Math.random() * 9000));  // $1000–$10000
            data.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(new Date()));
            data.put("region", selectRandomRegion());
            return data;
        }
    };
}
```

---

## Navigation

**← Previous**: [HTTP vs Kafka Patterns](09-http-vs-kafka.md)  
**→ Next**: [Checks & Assertions](11-checks-and-assertions.md)  
**↑ Up**: [Documentation Index](../index.md)
