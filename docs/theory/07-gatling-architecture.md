# Gatling Architecture

## What is Gatling?

Gatling is an open-source **load testing framework** that simulates thousands of concurrent virtual users making requests to your system.

```
Key characteristics:
├─ Written in Scala (but Java DSL for ease)
├─ High performance (can simulate 10,000+ users on single machine)
├─ Multiple protocols: HTTP, WebSocket, Kafka, SSE, etc.
├─ Expressive syntax: Scenarios written as code
├─ Rich reports: HTML dashboards with charts and metrics
├─ CI/CD friendly: Maven/Gradle plugins
└─ Open source: Free to use, active community
```

---

## Core Architecture

```
┌─ Your Load Test Script (Gatling Code)
│  ├─ HttpProtocol (baseUrl, headers, etc.)
│  ├─ ScenarioBuilder (what users do)
│  └─ setUp() (how many users, how fast)
│
├─ Gatling Engine
│  ├─ Parses scenarios
│  ├─ Spawns virtual users
│  ├─ Manages request lifecycle
│  ├─ Records metrics
│  └─ Collects results
│
├─ Virtual Users
│  ├─ User 1 executes scenario
│  ├─ User 2 executes scenario
│  ├─ User N executes scenario
│  └─ Each independent, concurrent
│
├─ Protocol Implementations
│  ├─ HttpClient (for HTTP/HTTPS)
│  ├─ WebSocket client
│  ├─ Kafka producer/consumer
│  └─ SSE client
│
└─ Results Aggregation
   ├─ Record each response time
   ├─ Track success/failures
   ├─ Generate HTML report
   └─ Export metrics (CSV, Datadog, etc.)
```

---

## The Three Building Blocks

Every Gatling simulation needs these three pieces:

### 1. **Protocol**

Defines **"where to send requests"** and common settings.

```java
HttpProtocolBuilder httpProtocol = http
    .baseUrl("https://api.example.com")              // All requests start here
    .header("Content-Type", "application/json")      // Common header
    .header("Accept", "application/json")
    .warmUp("https://api.example.com")              // Warm up connection pool
    .shareConnections();                            // Reuse connections
```

### 2. **Scenario**

Defines **"what virtual users do"** (their journey through your system).

```java
ScenarioBuilder userJourney = scenario("E-commerce Journey")
    .feed(userFeeder)                           // Inject user data
    .exec(http("Search products").get("/search?q=#{query}"))
    .pause(5)                                  // Think time
    .exec(http("View product").get("/product/#{productId}"))
    .pause(3)
    .exec(http("Add to cart").post("/cart")
        .body(StringBody("{\"productId\": \"#{productId}\"}")))
    .pause(2)
    .exec(http("Checkout").post("/checkout"));
```

### 3. **Setup**

Defines **"how many users, how fast"** (injection profile).

```java
{
    setUp(
        userJourney                     // Run the scenario
            .injectOpen(
                constantUsersPerSec(100).during(600)  // 100 new users/sec for 10 min
            )
    )
    .protocols(httpProtocol)           // Use the protocol defined above
    .assertions(
        global().responseTime().p95().lt(500),    // SLA
        global().successfulRequests().percent().gt(99.0)
    );
}
```

---

## Simulation Lifecycle

When you run `mvn gatling:test -Dgatling.simulationClass=MySimulation`:

```
1. LOAD PHASE
   ├─ Gatling loads your Simulation class
   ├─ Parses Protocol, Scenario, setUp()
   └─ Validates syntax

2. WARM-UP PHASE (implicit)
   ├─ Connection pools initialized
   ├─ TLS handshakes performed
   └─ JVM JIT compilation begins

3. INJECT PHASE
   ├─ Users are created according to injection profile
   │  └─ constantUsersPerSec(100).during(600):
   │     └─ At second 0: 1st user created, starts scenario
   │     └─ At second 1: 2nd user created, starts scenario
   │     └─ ... continues for 600 seconds
   └─ If user finishes scenario before injection ends:
      └─ User loops back to start of scenario (infinite by default)

4. EXECUTION PHASE
   ├─ Each user executes scenario steps
   ├─ Requests are sent
   ├─ Responses recorded
   ├─ Metrics collected in real-time
   └─ Live statistics printed to console

5. COOL-DOWN PHASE
   ├─ No new users injected (injection ended)
   ├─ Existing users finish their current iteration
   ├─ Final requests completed
   └─ Metrics collection stops

6. REPORTING PHASE
   ├─ All metrics aggregated
   ├─ Statistics calculated (p50, p95, p99, etc.)
   ├─ HTML report generated
   ├─ Assertions evaluated (pass/fail)
   └─ Results written to target/gatling/[simulation-timestamp]/
```

---

## Virtual User Model

Each virtual user operates independently:

```
User 1                          User 2                      User N
  │                              │                            │
  ├─ Feeder: userId=123          ├─ Feeder: userId=456       ├─ Feeder: userId=789
  │                              │                            │
  ├─ GET /products               ├─ GET /products            ├─ GET /products
  ├─ Wait: 5-10 sec              ├─ Wait: 5-10 sec           ├─ Wait: 5-10 sec
  │                              │                            │
  ├─ GET /product/#{productId}   ├─ GET /product/#{productId}├─ GET /product/#{productId}
  ├─ Wait: 3-5 sec               ├─ Wait: 3-5 sec            ├─ Wait: 3-5 sec
  │                              │                            │
  ├─ POST /cart                  ├─ POST /cart               ├─ POST /cart
  │  (with their own data)        │  (with their own data)     │  (with their own data)
  │                              │                            │
  ├─ Loop back to feeder         ├─ Loop back to feeder      ├─ Loop back to feeder
  │                              │                            │
  └─ When injection ends, user   └─ When injection ends      └─ When injection ends
     finishes naturally              finishes naturally        finishes naturally

Key point: All users are independent
├─ User 1's data doesn't affect User 2
├─ User 1's latency doesn't affect User 2
├─ They just happen to run concurrently
└─ Simulates real-world where users are independent
```

---

## Session & State

Each virtual user has a **session** (per-user state):

```
User 1's Session (in memory):
├─ userId: "123"           ← From feeder
├─ username: "alice"       ← From feeder
├─ productId: "P456"       ← Extracted from response
├─ cartId: "cart-789"      ← Extracted from response
└─ orderTotal: 99.99       ← Extracted from response

User 2's Session (different instance):
├─ userId: "456"
├─ username: "bob"
├─ productId: "P789"
├─ cartId: "cart-001"
└─ orderTotal: 149.99

Variables are available throughout user's scenario:
.exec(http("Use my data").get("/api/users/#{userId}"))
                                          ↑
                                    Substituted from session
```

---

## Request-Response Flow

When a virtual user executes a request:

```
1. REQUEST BUILDING
   ├─ HTTP method: GET, POST, PUT, etc.
   ├─ URL: Substitute variables (#{productId})
   ├─ Headers: Add common + custom headers
   ├─ Body: Inject data (if POST/PUT)
   └─ Result: Fully formed HTTP request

2. SENDING
   ├─ Send request to server
   ├─ Record send timestamp
   └─ Await response

3. RECEIVING
   ├─ Response arrives from server
   ├─ Record receive timestamp
   ├─ Latency = receive - send
   └─ Extract response body/headers

4. PROCESSING
   ├─ Apply CHECKS (validate response)
   │  └─ status().is(200) ← Does it match expectation?
   │  └─ jsonPath("$.productId").saveAs("productId")
   │     └─ Extract data and save to session for next request
   └─ If check fails: Request marked as FAILED
      Otherwise: Marked as SUCCESSFUL

5. NEXT ACTION
   ├─ If scenario has more steps: Execute next step
   ├─ If scenario is done: Loop back to start (or end if single iteration)
   └─ Or execute pause() before next step
```

---

## Metrics Collection

Gatling records metrics continuously:

```
For each request, record:
├─ Request name (from your code: "GET all posts")
├─ Start time (when request sent)
├─ End time (when response received)
├─ Latency (end - start)
├─ Status code (200, 404, 500, etc.)
├─ Success/failure (based on checks)
├─ Response body (optional, for parsing)
└─ Custom metrics (from your code)

Aggregated over time:
├─ Total requests: 30,000
├─ Successful: 29,850 (99.5%)
├─ Failed: 150 (0.5%)
├─ Min latency: 25ms
├─ Mean latency: 150ms
├─ Median (p50): 120ms
├─ p75: 200ms
├─ p95: 450ms
├─ p99: 1200ms
├─ p99.9: 3500ms
├─ Max latency: 8000ms
├─ RPS at peak: 1,234 requests/sec
└─ Average RPS: 834 requests/sec
```

---

## Protocols Supported

Gatling supports multiple protocols:

```
HTTP/HTTPS (most common)
├─ Synchronous request-response
├─ Web APIs, REST services
└─ Example: Most simulations in this learning path

WebSocket
├─ Bidirectional communication
├─ Real-time applications
└─ Example: Chat, notifications, live updates

SSE (Server-Sent Events)
├─ One-way server to client streaming
├─ Push notifications, live data
└─ Example: Stock tickers, news feeds

Kafka (custom integration)
├─ Asynchronous message publishing
├─ Event streaming
└─ Example: Labs 6-7 in this learning path

gRPC
├─ RPC over HTTP/2
├─ High-performance APIs
└─ Example: Microservice calls

JDBC
├─ Direct database connections
├─ Database stress testing
└─ Example: Load test database directly
```

---

## Performance Characteristics

Why is Gatling so efficient?

```
Traditional JMeter (thread-based):
├─ 1,000 users = 1,000 threads
├─ Each thread consumes memory
├─ Context switching overhead
├─ CPU struggles with > 1,000 users per machine
└─ Typical limit: ~1,000-5,000 users per machine

Gatling (non-blocking, async):
├─ 10,000+ users = Single connection pool
├─ Each user is lightweight (session + state)
├─ No thread per user; async I/O
├─ Minimal context switching
└─ Typical limit: ~10,000-100,000 users per machine
```

Why? Gatling uses non-blocking I/O:
- Request sent
- User doesn't block waiting for response
- User can execute pause() or other actions
- Response comes back whenever
- Efficient resource usage

---

## Typical Load Test Architecture

```
┌─ Your Code (Maven project)
│  ├─ src/test/java/simulations/
│  │  └─ MyLoadTest.java ← Contains Protocol, Scenario, setUp()
│  ├─ src/test/resources/
│  │  ├─ data/
│  │  │  └─ users.csv ← Feeder data
│  │  └─ gatling.conf ← Gatling settings
│  └─ pom.xml
│
├─ Command line
│  └─ mvn gatling:test -Dgatling.simulationClass=MyLoadTest
│
├─ Gatling Engine
│  ├─ Loads and parses simulation
│  ├─ Spawns virtual users
│  ├─ Sends requests
│  ├─ Records responses
│  └─ Collects metrics
│
├─ Target System Under Test
│  ├─ Web API
│  ├─ Database
│  ├─ Cache
│  └─ External services
│
├─ (Optional) Datadog Agent
│  ├─ Monitors system during test
│  ├─ Traces requests end-to-end
│  └─ Provides context for bottlenecks
│
└─ Results
   ├─ target/gatling/[timestamp]/index.html ← Report
   ├─ Console output ← Live stats
   └─ Metrics → Datadog (if configured)
```

---

## Key Takeaways

1. **Protocol** = Configuration (where to send, common settings)
2. **Scenario** = User behavior (what they do)
3. **Setup** = Load injection (how many, how fast)
4. **Virtual users** = Independent, concurrent, realistic
5. **Sessions** = Per-user state (variables, data)
6. **Non-blocking** = Efficient resource usage
7. **Metrics** = Automatically collected and reported

---

## Next Steps

→ **Read next**: [Simulation Lifecycle](08-simulation-lifecycle.md) - Detailed execution flow

