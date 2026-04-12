# Lab 1: Basic HTTP

## Learning Objectives

After this lab, you'll understand:

- ✅ The three building blocks of Gatling (Protocol, Scenario, setUp)
- ✅ Making simple GET requests
- ✅ Adding pauses between requests (think-time)
- ✅ Constant-rate load injection (constantUsersPerSec)
- ✅ Reading Gatling HTML reports

## Real-World Scenario

You're testing an e-commerce API. You want to simulate 50 concurrent users browsing products. Each user:
1. Fetches list of all products
2. Waits 2-5 seconds reading the list
3. Fetches details of one product
4. Waits 2-5 seconds reading details
5. Loops back to start

**Question**: Does the API handle 50 concurrent browsers with acceptable latency?

---

## Code Walkthrough

Open the simulation:

```bash
cat src/test/java/io/learn/gatling/simulations/http/Sim01_BasicHttp.java
```

Key sections:

### 1. Protocol (Where to send requests)

```java
HttpProtocolBuilder httpProtocol = http
    .baseUrl("https://jsonplaceholder.typicode.com")  // Public test API
    .header("Content-Type", "application/json")
    .header("Accept", "application/json")
    .warmUp("https://jsonplaceholder.typicode.com");
```

**Explanation**:
- `baseUrl()`: All relative URLs are prefixed with this
- `header()`: Headers sent with every request
- `warmUp()`: Pre-establish connection pool before test

### 2. Scenario (What users do)

```java
ScenarioBuilder browsePosts = scenario("Browse Posts")
    .exec(http("GET all posts")
        .get("/posts"))
    .pause(1)
    .exec(http("GET single post")
        .get("/posts/1"))
    .pause(1)
    .exec(http("GET comments for post")
        .get("/comments")
        .queryParam("postId", "1"));
```

**Explanation**:
- `scenario()`: Names the user journey
- `exec(http(...))`: Makes an HTTP request
- `.get("/posts")`: HTTP GET to https://jsonplaceholder.typicode.com/posts
- `.pause(1)`: Wait 1 second (think-time)
- `.queryParam()`: Adds ?postId=1 to the URL

### 3. Setup (How many users, how long)

```java
{
    setUp(
        browsePosts
            .injectOpen(
                constantUsersPerSec(2).during(15)  // 2 new users/sec for 15 sec
            )
    )
    .protocols(httpProtocol);
}
```

**Explanation**:
- `constantUsersPerSec(2)`: Create 2 new virtual users every second
- `.during(15)`: For 15 seconds
- Total users: 2/sec × 15 sec = 30 users will run
- `.protocols(httpProtocol)`: Use the protocol configured above

---

## Run the Lab

```bash
# Navigate to project
cd /Users/sgovinda/Learn/GatlingLearning

# Run Sim01
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim01_BasicHttp

# Expected output (live in console):
# ├─ Simulation started
# ├─ Users ramping up
# ├─ Requests being sent
# ├─ Real-time statistics showing:
# │  ├─ Active users count
# │  ├─ Response time p95, p99
# │  ├─ RPS (requests/second)
# │  └─ Error count
# ├─ Test completes
# └─ HTML report generated

# View report
open target/gatling/sim01_basichttp-*/index.html
```

**Test Duration**: ~30 seconds total (ramp-up + cool-down)

---

## Verify Success

### Expected Results in Report

When you open the HTML report, look for:

```
Global Stats:
├─ Total requests: 90 (3 requests per user × 30 users)
├─ Successful: 90 (100%)
├─ Failed: 0
├─ Min latency: 50ms (fast)
├─ Mean latency: 300ms
├─ p50 latency: 250ms
├─ p95 latency: 600ms
├─ p99 latency: 900ms
├─ Max latency: 1500ms (acceptable for public API)
└─ RPS: ~30 requests/second (ramping up from 0 to ~6 RPS at peak)
```

### Checklist

```
✅ Test completed without errors
✅ >90 requests made (3 per user × 30 users)
✅ Success rate = 100%
✅ No assertion failures
✅ Latency is reasonable (<2000ms for public test API)
✅ HTML report generated and viewable
```

---

## Understanding the Metrics

### Requests Breakdown

```
3 requests per user:
├─ GET /posts (0.9sec response)
├─ GET /posts/1 (0.5sec response)
└─ GET /comments?postId=1 (0.7sec response)

Total requests = 3 × 30 users = 90 requests
```

### Latency Distribution

```
Latencies (from report):

p50 (median) = 250ms
├─ Half of requests were faster than 250ms
├─ Half were slower
└─ Good baseline

p95 = 600ms
├─ 95% of requests finished within 600ms
├─ 5% took longer
├─ For a public test API, acceptable
└─ In production, target <300ms

p99 = 900ms
├─ 99% of requests finished within 900ms
├─ 1% took longer (possibly network hiccups)
└─ For public test API, acceptable
```

### Active Users Timeline

From "Scenario" section in report:

```
Users ramping up:
├─ Second 1: 2 users active (0-2 making requests)
├─ Second 2: 4 users active (2-4 making requests)
├─ Second 3: 6 users active
├─ ... continues ...
├─ Second 15: 30 users active (peak)
├─ Second 16+: Fewer users (those from second 1 finish scenario, don't loop)
└─ After second 30: 0 users (all completed)
```

---

## Troubleshooting

### Problem: "Connection refused"

```
Error: java.net.ConnectException: Connection refused

Cause: Can't reach https://jsonplaceholder.typicode.com

Fix:
├─ Check internet connection
├─ Verify API is accessible: curl https://jsonplaceholder.typicode.com/posts
├─ If down, temporarily change to: .baseUrl("http://localhost:8080")
└─ Or wait for the public API to come back online
```

### Problem: "Only 10 requests made instead of 90"

```
Cause: Scenario didn't loop (or Gatling configuration issue)

Possible fixes:
├─ Verify pauseTime is correct (pause(1) means 1 second)
├─ Check if some requests are failing silently
├─ Look at HTML report "Errors" section
└─ Re-run with more verbose logging: mvn gatling:test -X
```

### Problem: "Report not generated"

```
Cause: Missing assertions or other issue

Fix:
├─ Check console output for errors
├��� Verify pom.xml has gatling-maven-plugin
├─ Clean and retry: rm -rf target/ && mvn clean gatling:test ...
└─ Check if test output exists: ls target/gatling/
```

---

## Experimentation: Try Modifying the Code

### Experiment 1: Different Load

Replace:
```java
constantUsersPerSec(2).during(15)
```

With:
```java
constantUsersPerSec(5).during(30)  // More users, longer duration
```

Then re-run. Question: Does latency increase with more users?

### Experiment 2: Different Think-Time

Replace:
```java
.pause(1)
```

With:
```java
.pause(3)  // 3 second think-time
```

Then re-run. Question: Do overall metrics change?

### Experiment 3: Different API Endpoint

Add a new request to the scenario:

```java
.pause(1)
.exec(http("GET users")
    .get("/users"))  // Fetch users instead of comments
```

Then re-run. Question: How do different endpoints compare in latency?

---

## Key Takeaways

1. **Protocol** sets up common configuration
2. **Scenario** defines what users do (requests + pauses)
3. **setUp** specifies load profile (how many users, how fast)
4. **Pauses** simulate real user think-time
5. **constantUsersPerSec** creates load at constant rate
6. **HTML report** shows comprehensive metrics
7. **p95, p99** are more useful than mean latency

---

## Next Steps

→ **Move to Lab 2**: [Lab 2: HTTP with Feeders](24-lab-http-feeders.md) - Inject varying data for realistic testing

