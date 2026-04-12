# Load Testing Methodology

## The Systematic Approach

Performance testing isn't random. It's a disciplined process:

```
1. PLAN: Define what you're testing and why
   ↓
2. DESIGN: Create realistic scenarios
   ↓
3. IMPLEMENT: Code simulations in Gatling
   ↓
4. EXECUTE: Run tests systematically
   ↓
5. ANALYZE: Understand the results
   ↓
6. OPTIMIZE: Fix bottlenecks
   ↓
7. RE-TEST: Verify improvements
   ↓
8. DEPLOY: With confidence
```

---

## Phase 1: PLAN (Define Objectives)

### Step 1a: Document Success Criteria (SLAs)

Before you write any code, define what "success" looks like:

```
Question: What must be true for the test to PASS?

Examples:

1. E-commerce checkout
   ├─ p95 latency: <500ms
   ├─ p99 latency: <1000ms
   ├─ Success rate: >99.5% (max 0.5% errors)
   └─ Achieve: 2,000 concurrent users

2. Mobile API
   ├─ p95 latency: <200ms
   ├─ p99 latency: <500ms
   ├─ Success rate: >99.9%
   └─ Achieve: 5,000 RPS

3. Real-time analytics service
   ├─ p95 latency: <50ms
   ├─ p99 latency: <100ms
   ├─ Success rate: >99.99%
   └─ Achieve: 10,000 RPS
```

**Pro tip**: Involve stakeholders in defining SLAs!
- Product team: What do users tolerate?
- Ops team: What can infrastructure support?
- Engineering: What's reasonable to achieve?

### Step 1b: Identify Critical User Paths

List the most important scenarios to test:

```
E-commerce example:

Rank 1: Browse & Purchase (most revenue impact)
├─ Search products
├─ View product details
├─ Add to cart
├─ Checkout
└─ Payment

Rank 2: Account Management
├─ Login
├─ View order history
├─ Update profile
└─ Change password

Rank 3: Customer Support
├─ View FAQ
├─ Submit ticket
└─ Chat

→ Focus load testing on Rank 1 paths first!
```

### Step 1c: Estimate Expected Load

```
Question: How many concurrent users will we have?

Methods:

1. Historical data
   └─ "We had 10,000 concurrent users on Black Friday"

2. Capacity planning
   └─ "We want to grow to 50,000 users/day = 500 concurrent"

3. Industry benchmarks
   └─ "Industry average is 10% simultaneous"

4. Peak calculation
   └─ "100,000 daily active users × 5% = 5,000 peak concurrent"

→ TEST TO 2-3x EXPECTED LOAD (safety margin)
```

---

## Phase 2: DESIGN (Create Scenarios)

### Step 2a: Define Realistic User Behavior

**Bad**: 1,000 users all doing the exact same thing at the exact same time
- Unrealistic
- Tests a specific bottleneck, not general behavior
- Doesn't reveal cascade failures

**Good**: Users follow realistic patterns with variety

```
Realistic user behavior:
├─ Not all users do the same thing
│  └─ 70% browse
│  └─ 20% purchase
│  └─ 10% use support
│
├─ Users have think-time (don't hammer continuously)
│  └─ Read product description: 10-30 seconds
│  └─ Check reviews: 5-15 seconds
│  └─ Decide to purchase or not: 30-60 seconds
│
├─ Request patterns vary
│  └─ Sometimes cache hit (fast)
│  └─ Sometimes cache miss (slow)
│  └─ Occasional errors (network, external service)
│
└─ Data varies
   └─ Different users, products, regions
   └─ Different request payloads
```

**Implementation in Gatling**: Use feeders + pauses

```java
scenario("Realistic User")
    .feed(userFeeder)  // Different user each iteration
    .exec(http("Search").get("/search?q=#{query}"))
    .pause(10, 30)     // 10-30 sec think-time
    .exec(http("View Details").get("/product/#{productId}"))
    .pause(5, 15)
    // ... more realistic actions ...
```

### Step 2b: Design Multiple Scenarios

```
Scenario 1: Load Test (Baseline)
├─ Constant load: 1,000 users for 15 minutes
├─ Objective: "Does it meet SLAs under normal load?"
├─ Success: p95 <500ms, success rate >99%
└─ Duration: ~20 minutes

Scenario 2: Ramp Test (Find Limits)
├─ Start: 100 users, ramp to 5,000 users over 30 minutes
├─ Objective: "At what load do SLAs break?"
├─ Watch for: Where does p95 spike? Where do errors start?
└─ Duration: ~40 minutes

Scenario 3: Step Test (Threshold Analysis)
├─ Step 1: 1,000 users for 5 min
├─ Step 2: 2,000 users for 5 min
├─ Step 3: 3,000 users for 5 min
├─ Step 4: 4,000 users for 5 min
├─ Objective: "At what level does each metric degrade?"
├─ Watch for: Cache warmup, JVM behavior at each level
└─ Duration: ~25 minutes

Scenario 4: Spike Test (Recovery)
├─ Normal: 500 users for 5 min
├─ Spike: Jump to 5,000 users for 5 min
├─ Recovery: Back to 500 users for 10 min
├─ Objective: "Can the system recover from sudden load?"
└─ Duration: ~25 minutes

Scenario 5: Soak Test (Long-term Stability)
├─ Constant: 1,000 users for 24 hours
├─ Objective: "Does the system stay stable overnight?"
├─ Watch for: Memory leaks, connection leaks, degradation
└─ Duration: 24 hours (run overnight)
```

### Step 2c: Data Strategy

```
Question: What data will you use in the test?

Options:

1. Random data (auto-generated)
   └─ No setup needed
   └─ Sometimes unrealistic (fake emails, nonsense values)
   └─ Good for: Basic load testing

2. CSV file with real data
   └─ Realistic values (real user IDs, emails, products)
   └─ Can simulate real data patterns
   └─ Good for: Realistic testing

3. Custom generator (Java code)
   └─ Full control over data generation
   └─ Can create complex, business-logic-aware data
   └─ Good for: Advanced testing

Example: Testing purchase flow

Option 1 (Bad): Random product ID
├─ productId: "xyz123random456"
└─ API returns: "Product not found" error
   └─ Test is invalid!

Option 2 (Good): CSV file
├─ CSV has 1,000 valid product IDs
├─ Test uses: "P123", "P456", "P789", etc.
└─ API returns: Valid product data

Option 3 (Best): Custom generator
├─ Java code knows: "Products are P0001 to P9999"
├─ Java code also knows: "Premium users can buy any product"
├─ Java code also knows: "Normal users can only buy available items"
└─ Test simulates: Real business logic constraints
```

---

## Phase 3: IMPLEMENT (Code Simulations)

This is where you write Gatling code. (See Labs 1-8 for detailed examples.)

Quick template:

```java
public class MyLoadTest extends Simulation {
  
  // 1. Protocol (where to send requests)
  HttpProtocolBuilder httpProtocol = http
      .baseUrl("https://api.example.com")
      .header("Content-Type", "application/json");

  // 2. Scenario (what users do)
  ScenarioBuilder scenario = scenario("User Journey")
      .feed(csvFeeder)  // Inject data
      .exec(http("GET products").get("/products"))
      .pause(5)  // Think time
      .exec(http("POST purchase").post("/purchase")
          .body(StringBody("{...}")));

  // 3. Setup (how many users, how fast)
  {
    setUp(
        scenario.injectOpen(constantUsersPerSec(100).during(600))
    )
    .protocols(httpProtocol)
    .assertions(
        global().responseTime().p95().lt(500),  // SLA
        global().successfulRequests().percent().gt(99.0)
    );
  }
}
```

---

## Phase 4: EXECUTE (Run Tests)

### Step 4a: Pre-Test Checklist

```
Before you hit "run":

Infrastructure:
  ☐ Staging environment is clean (no other traffic)
  ☐ Databases are reset to known state
  ☐ Caches are cleared (or warmed up, depending on scenario)
  ☐ Monitoring is enabled (Datadog, APM, etc.)
  ☐ Log level is set appropriately (ERROR level to avoid log spam)

Gatling:
  ☐ Simulation compiles with no errors
  ☐ Smoke test passes (1 user, single iteration)
  ☐ Test data is available (CSV files, feeders)
  ☐ Assertions are set correctly (SLAs)
  ☐ Duration is reasonable (don't run 24-hour soak on first try)

Team:
  ☐ Stakeholders are informed (don't surprise ops team)
  ☐ On-call engineer is available if needed
  ☐ Slack/email channel is open for communication
  ☐ Plan to cancel test if something goes wrong
```

### Step 4b: Execution Pattern

```
1. Smoke Test (warm-up)
   └─ Run with 1 user for 1-2 minutes
   └─ Verifies: Code compiles, API responds, no crashes
   └─ If fails: Fix and retry before real test

2. Wait 10 minutes
   └─ Let system settle
   └─ Let logs clear
   └─ Let monitoring reset

3. Run Load Test
   └─ Your actual test (constant, ramp, step, spike)
   └─ Record all metrics and logs

4. Wait 10 minutes
   └─ Let system cool down
   └─ Monitor for memory leaks or slow recovery

5. If all good: Run next scenario
   └─ E.g., after load test, run ramp test
   └─ Or, schedule for next day

Don't run back-to-back tests without cool-down!
```

### Step 4c: During Test: Monitor Everything

```
Gatling console (live):
├─ Active users (ramping up?)
├─ Throughput (RPS increasing or plateau?)
├─ Response times (p95, p99 trending?)
├─ Errors (any appearing?)
└─ Success rate (dropping?)

Server monitoring (Datadog/APM):
├─ CPU usage (climbing?)
├─ Memory (stable or growing?)
├─ Database connections (free or exhausted?)
├─ Disk I/O (at ceiling?)
├─ Network (saturated?)
└─ Any errors in logs?

Be ready to abort if:
  ❌ Server crashes
  ❌ Error rate suddenly spikes >10%
  ❌ System doesn't recover (GC pauses endless)
  ❌ Cascading failures detected
```

---

## Phase 5: ANALYZE (Review Results)

### Step 5a: Read Gatling Report

```
After test completes, open:
  target/gatling/[simulation-name-timestamp]/index.html

Key sections:

1. Global Stats (top-level summary)
   ├─ Total requests sent
   ├─ Success/failure counts
   ├─ Min, mean, p50, p75, p95, p99 latencies
   └─ Requests per second

2. Request Detail (by endpoint)
   ├─ GET /products: p95=150ms
   ├─ POST /purchase: p95=500ms ← Slower!
   └─ GET /order: p95=200ms

3. Scenario (load ramp-up timeline)
   ├─ Shows: How many users active at each second
   └─ Validates: Did load ramp as expected?

4. Response Time Distribution (histogram)
   ├─ Visual: Most requests are fast
   └─ Tail: Few requests are very slow

5. Errors (if any)
   ├─ Error type: 500 Internal Server Error
   ├─ Count: 50 times
   └─ Timeline: Appeared after 10 minutes
```

### Step 5b: Compare Against SLAs

```
Example: E-commerce load test

SLAs defined:
├─ p95 latency < 500ms
├─ p99 latency < 1000ms
├─ Success rate > 99%
└─ Support 2,000 concurrent users

Results:
├─ p95 latency: 480ms ✓ PASS
├─ p99 latency: 950ms ✓ PASS
├─ Success rate: 99.2% ✓ PASS
└─ Handled 2,000 concurrent ✓ PASS

Final result: ✅ TEST PASSED
```

### Step 5c: Find Bottlenecks

```
If test failed, diagnose why:

Symptom 1: Latency increases linearly with load
├─ Likely cause: Resource exhaustion (CPU, memory, disk)
├─ Evidence: Check Datadog CPU/memory graphs
└─ Fix: Optimize code, add caching, scale infrastructure

Symptom 2: Latency increases exponentially
├─ Likely cause: Queue buildup (request backlog)
├─ Evidence: Response times spike, then don't recover
└─ Fix: Increase capacity or reduce incoming load

Symptom 3: Error rate increases suddenly
├─ Likely cause: Something overflowing (connection pool, memory)
├─ Evidence: Check error types in Gatling report
└─ Fix: Increase pool size, fix memory leak, scale service

Symptom 4: Certain endpoints slow, others fast
├─ Likely cause: Specific bottleneck (slow DB query)
├─ Evidence: Datadog traces show slow query in one endpoint
└─ Fix: Optimize that specific query

Symptom 5: p95 OK but p99 bad
├─ Likely cause: GC pauses, occasional queuing
├─ Evidence: p99 spikes, but p95 stable
└─ Fix: JVM tuning, improve code efficiency
```

### Step 5d: Use Datadog to Drill Down

```
When Gatling says "latency is bad", Datadog tells you WHY:

Query in Datadog:
  trace.web.request.duration{service:my-api}
  by resource_name

Results:
├─ GET /api/products: p95=150ms ✓ Fast
├─ POST /api/purchase: p95=800ms ⚠ Slow
│  └─ Drill down into traces
│     └─ Find: Call to external payment service (600ms)
│        └─ Root cause: Payment API is slow
│           └─ Fix: Add timeout, use async, add circuit breaker
│
└─ GET /api/user/profile: p95=200ms ✓ Fast
```

---

## Phase 6: OPTIMIZE

Based on analysis, fix bottlenecks:

```
Examples:

1. Slow database query
   ├─ Evidence: Datadog shows 400ms in SELECT query
   ├─ Fixes:
   │  ├─ Add index on WHERE column
   │  ├─ Fetch only needed columns (not *)
   │  ├─ Add database caching
   │  └─ Use read replica for high-traffic endpoints
   └─ Re-test: Verify improvement

2. CPU-bound processing
   ├─ Evidence: CPU 100%, latency high
   ├─ Fixes:
   │  ├─ Profile with JFR (Java Flight Recorder)
   │  ├─ Find hot methods
   │  ├─ Optimize algorithm or use caching
   │  └─ Consider moving to async processing
   └─ Re-test: Verify improvement

3. Exhausted connection pool
   ├─ Evidence: Errors "Too many connections"
   ├─ Fixes:
   │  ├─ Increase pool size
   │  ├─ Fix connection leaks (ensure close() called)
   │  ├─ Use connection pooling library (HikariCP)
   │  └─ Reduce query time (so connections released faster)
   └─ Re-test: Verify improvement

4. Memory leak
   ├─ Evidence: Heap grows 1GB → 8GB over test
   ├─ Fixes:
   │  ├─ Use heap dump analyzer (JProfiler, YourKit)
   │  ├─ Find object holding references
   │  ├─ Fix leak (remove listener, close resource)
   │  └─ Verify leak is gone
   └─ Re-test: Verify improvement
```

---

## Phase 7: RE-TEST

After optimization:

```
1. Run same test scenario again
   └─ Compare metrics to baseline
   └─ Verify improvement (e.g., p95 dropped from 500ms → 300ms)

2. Run to new load level
   └─ If you optimized, try higher load
   └─ If p95 was 500ms at 1000 users, test 2000 users now

3. Run soak test
   └─ Ensure optimization didn't introduce memory leak
   └─ Run 2-8 hours at normal load

Success criteria:
✅ Metrics improved
✅ New SLAs are met
✅ No new issues introduced
```

---

## Phase 8: DEPLOY

With confidence:

```
Before production deployment:
├─ Load test passed ✓
├─ Soak test passed ✓
├─ Code reviewed ✓
├─ Rollback plan ready ✓
└─ Team prepared ✓

Deployment strategy:
├─ Canary: 10% traffic to new version
├─ Monitor: Watch metrics, error rate, latency
├─ Expand: 50% traffic if metrics good
├─ Expand: 100% traffic if still good
└─ Rollback plan: If issues detected, revert instantly
```

---

## Best Practices Summary

### ✅ DO:
- Define SLAs before testing
- Test realistic user behavior (think-time, variety)
- Test multiple scenarios (load, ramp, soak, spike)
- Monitor system resources during test
- Use Datadog/APM to find bottlenecks
- Re-test after optimization
- Test in staging, not production
- Document findings

### ❌ DON'T:
- Run test without clear objective
- Test with unrealistic data or behavior
- Assume one test tells the whole story
- Ignore spike in error rate during test
- Fire all load from one machine (becomes bottleneck)
- Deploy without testing (or with minimal testing)
- Test at same load level each time (no learning)

---

## Next Steps

→ **Read next**: [Open Load Patterns](04-open-load-patterns.md) - Specific load pattern techniques

