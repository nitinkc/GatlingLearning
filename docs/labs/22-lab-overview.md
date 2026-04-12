# Lab Overview

## How to Work Through the Labs

This section contains **8 hands-on labs** that will teach you Gatling through doing. Each lab builds on the previous one.

### Prerequisites

Ensure you have:
```bash
java -version          # Java 21+
mvn -version          # Maven 3.9+
docker -v             # For Kafka labs (Labs 6-7)

# Clone the project
cd /Users/sgovinda/Learn/GatlingLearning
```

---

## Lab Structure

Each lab follows this pattern:

### 1. **Learning Objectives**
- What you'll learn in this lab
- Concepts covered
- Real-world use case

### 2. **Concept Explanation**
- Theory behind the feature
- When to use it
- Common patterns

### 3. **Code Walkthrough**
- The actual simulation code
- Comments explaining each part
- How it relates to Gatling concepts

### 4. **Run the Lab**
- Exact Maven command to execute
- What to expect during execution
- How long it takes

### 5. **Verify Success**
- What to look for in the HTML report
- Key metrics to check
- Expected values

### 6. **Expected Output**
- Screenshot or copy-paste of results
- Confirms "you did it right"

### 7. **Troubleshooting**
- Common issues and fixes
- What if it fails
- How to debug

---

## Learning Paths by Goal

### Path 1: Quick Skills (2-3 hours)
```
Goal: Understand core Gatling concepts and run your first tests

1. Lab 1: Basic HTTP (20 min)
   └─ Simple GET requests, constant load
   
2. Lab 2: HTTP Feeders (25 min)
   └─ CSV data injection, variable substitution
   
3. Lab 3: Checks & Validation (25 min)
   └─ Response validation, data correlation
   
4. Lab 4: Load Profiles (30 min)
   └─ Smoke, ramp, step, spike patterns

Total: 100 minutes (1.5-2 hours of learning)
Outcome: Can write basic HTTP simulations
```

### Path 2: Full Mastery (4-6 hours)
```
Goal: Complete expertise from HTTP to Kafka

1-4. HTTP Labs (2 hours)
   └─ As above

5. Lab 5: CRUD Operations (30 min)
   └─ PUT, PATCH, DELETE requests
   
6. Lab 6: Kafka Producer (30 min)
   └─ Event streaming, async patterns
   
7. Lab 7: Kafka with Feeders (30 min)
   └─ Data-driven Kafka tests
   
8. Lab 8: Advanced Patterns (45 min)
   └─ Complex scenarios, custom logic

Total: 4-6 hours
Outcome: Full-stack Gatling expert
```

### Path 3: Specific Focus
```
Goal: Learn just what you need

For HTTP APIs:
├─ Labs 1, 2, 3, 4, 5 (2.5 hours)
└─ Outcome: HTTP load testing expert

For Event-Driven Systems:
├─ Labs 1 (basics), 6, 7, 8 (2 hours)
└─ Outcome: Kafka/event streaming expert

For Complete System:
├─ Labs 1-8 (all of them)
└─ Outcome: Full Gatling expert
```

---

## Lab Progression Map

```
Lab 1: Basic HTTP
├─ Learn: GET requests, pauses, constant load
├─ Simulates: Simple user browsing
├─ Difficulty: ⭐ Beginner
├─ Duration: 20 min
└─ Next: Lab 2

Lab 2: HTTP with Feeders
├─ Learn: CSV feeders, data injection, variable substitution
├─ Simulates: Realistic users with varying data
├─ Difficulty: ⭐⭐ Beginner-Intermediate
├─ Duration: 25 min
└─ Next: Lab 3

Lab 3: Checks & Validation
├─ Learn: Response validation, extraction, correlation
├─ Simulates: Multi-step journeys with data flow
├─ Difficulty: ⭐⭐⭐ Intermediate
├─ Duration: 25 min
└─ Next: Lab 4

Lab 4: Load Profiles
├─ Learn: Smoke, ramp, step, spike patterns
├─ Simulates: Different test scenarios
├─ Difficulty: ⭐⭐ Intermediate
├─ Duration: 30 min
└─ Next: Lab 5

Lab 5: CRUD Operations
├─ Learn: PUT, PATCH, DELETE, file bodies
├─ Simulates: Full CRUD API lifecycle
├─ Difficulty: ⭐⭐ Intermediate
├─ Duration: 30 min
└─ Next: Lab 6 (or 8 if no Kafka)

Lab 6: Kafka Producer
├─ Learn: Async messaging, custom exec blocks
├─ Simulates: Event production
├─ Difficulty: ⭐⭐⭐ Intermediate-Advanced
├─ Duration: 30 min (⚠️ Requires Docker)
└─ Next: Lab 7

Lab 7: Kafka with Feeders
├─ Learn: Data-driven Kafka tests
├─ Simulates: Production-like event flows
├─ Difficulty: ⭐⭐⭐ Intermediate-Advanced
├─ Duration: 30 min (⚠️ Requires Docker)
└─ Next: Lab 8

Lab 8: Advanced Patterns
├─ Learn: Custom feeders, complex scenarios, throttling
├─ Simulates: Production-ready testing
├─ Difficulty: ⭐⭐⭐⭐ Advanced
├─ Duration: 45 min
└─ Done!
```

---

## Before Each Lab

### Checklist

```
☐ Read the lab introduction
☐ Understand the learning objectives
☐ Have the concept explanation handy
☐ Know what the expected outcome should be
☐ Check if Docker is needed (Labs 6-7)
☐ Clear any previous test runs: rm -rf target/gatling
```

### Get the Code

Each lab references a simulation file:

```bash
# Lab 1: Basic HTTP
cat src/test/java/io/learn/gatling/simulations/http/Sim01_BasicHttp.java

# Lab 2: HTTP Feeders
cat src/test/java/io/learn/gatling/simulations/http/Sim02_HttpWithFeeders.java

# ... etc for Labs 3-8
```

---

## Running a Lab

### Generic Steps

```bash
# 1. Navigate to project root
cd /Users/sgovinda/Learn/GatlingLearning

# 2. Run the simulation (replace CLASS with actual class)
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim01_BasicHttp

# 3. Wait for test to complete
# You'll see:
#   ├─ Console output showing progress
#   ├─ Metrics live
#   └─ Test completes with assertion results

# 4. View the report
open target/gatling/sim01_basichttp-*/index.html   # macOS
xdg-open target/gatling/sim01_basichttp-*/index.html  # Linux
```

### Reading the Report

After test completes, the HTML report shows:

```
Home page:
├─ Global Statistics
│  ├─ Total requests
│  ├─ Success/failure rates
│  ├─ Min, mean, p50, p75, p95, p99 latencies
│  └─ Requests per second
│
├─ Request Details
│  └─ Breakdown by endpoint
│     ├─ GET /api/products: 1000 requests, p95=150ms
│     ├─ POST /api/purchase: 500 requests, p95=350ms
│     └─ etc
│
├─ Response Time Distribution
│  └─ Histogram showing response time spread
│
├─ Requests/Responses
│  └─ Timeline of success/errors over test duration
│
└─ Scenario
   └─ Timeline of user ramp-up
```

---

## Success Criteria for Each Lab

### Lab 1
```
✅ Test completes without errors
✅ >100 total requests made
✅ Success rate >99%
✅ p95 latency <2000ms (public API might be slow)
✅ HTML report generated
```

### Lab 2
```
✅ CSV feeder loaded successfully
✅ Variables substituted correctly (URLs differ per user)
✅ >200 total requests
✅ Success rate >99%
✅ All data varied (not repeating same requests)
```

### Lab 3
```
✅ Checks validated responses
✅ Extracted data from responses
✅ Data used in subsequent requests
✅ All requests successful
✅ Complex request chains work
```

### Lab 4
```
✅ Smoke test: 1 user, successful
✅ Load test: Constant load, stable metrics
✅ Ramp test: Load increases smoothly
✅ Step test: Each step distinguishable
✅ All profiles complete without assertion failure
```

### Lab 5
```
✅ All CRUD methods work (GET, POST, PUT, PATCH, DELETE)
✅ Request bodies from file templates
✅ Response parsing works
✅ >100 requests total
✅ All assertions pass
```

### Lab 6
```
⚠️ Docker must be running
✅ Kafka broker starts (Testcontainers)
✅ Messages produced successfully
✅ >100 messages sent
✅ No errors in test output
```

### Lab 7
```
⚠️ Docker must be running
✅ CSV feeder loaded
✅ Messages sent with feeder data
✅ Multiple scenarios execute
✅ >200 messages total
✅ Test completes successfully
```

### Lab 8
```
✅ Advanced patterns (custom feeders, throttling, etc.)
✅ Complex scenario logic works
✅ All features demonstrate successfully
✅ Understanding of advanced Gatling concepts
```

---

## Common Issues & Fixes

### Issue: "Test won't run"
```
Error: "Simulation class not found"

Fix:
├─ Check class name spelling: io.learn.gatling.simulations.http.Sim01_BasicHttp
├─ Check file exists: src/test/java/.../Sim01_BasicHttp.java
├─ Clean and rebuild: mvn clean compile
└─ Try: mvn gatling:test -DgatlingSimulationsFolder=src/test/java
```

### Issue: "Feeder file not found"
```
Error: "CSV file not found: data/users.csv"

Fix:
├─ Check file exists: src/test/resources/data/users.csv
├─ Check path is relative to resources: "data/users.csv" not "/full/path"
├─ Clean: rm -rf target/
└─ Rebuild: mvn compile
```

### Issue: "Connection refused"
```
Error: "Connection refused" when connecting to API

Fix:
├─ Verify API is running and accessible
├─ Check URL in HttpProtocol: baseUrl("https://api.example.com")
├─ Test manually: curl https://api.example.com
└─ Check firewall/proxy settings
```

### Issue: "Docker not found" (Kafka labs)
```
Error: "Docker daemon not accessible"

Fix:
├─ Start Docker: open -a Docker  (macOS)
├─ Verify Docker running: docker ps
├─ Retry lab: mvn gatling:test -D...
└─ If still fails: Skip Kafka labs, focus on HTTP labs
```

### Issue: "Assertion failed"
```
Error: "Assertions failed"

Fix:
├─ Check expected values in setUp()
├─ Relax assertions if test was close: p95 < 5000 instead of < 500
├─ Review HTML report for actual metrics
├─ Understand if failure is real issue or just strict SLA
└─ Re-run lab to check for flakiness
```

---

## Tips for Success

### ✅ DO:
- Read the concept explanation before running code
- Run labs in order (each builds on previous)
- Study the simulation code before running
- Review the HTML report carefully
- Understand each metric (p95, p99, etc.)
- Take notes on what you learn
- Experiment: Try modifying the code
- Ask questions when confused

### ❌ DON'T:
- Skip the theory (foundations matter)
- Run labs out of order without understanding
- Just copy-paste code without understanding
- Ignore the HTML report
- Set unrealistic SLA targets
- Run multiple labs simultaneously (confuses output)
- Skip reading error messages
- Give up if first attempt fails

---

## After Each Lab

### Reflection Questions

Ask yourself:

```
1. What was the main concept?
   └─ Explain it to a colleague

2. When would I use this in real work?
   └─ Give a concrete example

3. What could go wrong?
   └─ How would I troubleshoot?

4. How could I extend this?
   └─ What if I needed to do X instead?

5. What's still unclear?
   └─ Go back and re-read that section
```

### Experimentation

Try modifying the lab code:

```java
// Lab 1: Try changing the load pattern
// Original: constantUsersPerSec(2).during(15)
// Try: constantUsersPerSec(5).during(30)
//      rampUsersPerSec(2).to(10).during(30)
// See how results change

// Lab 2: Try different feeders
// Original: csv("data/users.csv")
// Try: csv("data/users.csv").random()
//      csv("data/users.csv").shuffle()
// See how behavior changes

// Lab 3: Try different checks
// Original: status().is(200)
// Try: status().in(200, 201)
//      bodyString().contains("expected text")
// See how validation changes
```

---

## Lab Time Estimates

If working through all labs:

```
Lab 1: 25 min (reading + running + analysis)
Lab 2: 30 min (CSV, substitution, patterns)
Lab 3: 30 min (validation, correlation)
Lab 4: 40 min (multiple profiles)
Lab 5: 35 min (CRUD operations)
Lab 6: 40 min (Kafka setup + running)
Lab 7: 40 min (Kafka with data)
Lab 8: 60 min (advanced patterns, experimentation)

Total: ~5 hours for complete mastery
```

Or faster if you skip Kafka (Labs 6-7):
```
Labs 1-5 + 8: ~3 hours
```

---

## Next Steps

→ **Start Lab 1**: [Lab 1: Basic HTTP](23-lab-basic-http.md)

