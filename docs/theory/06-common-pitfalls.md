# Common Pitfalls & Best Practices

## Pitfalls: What NOT to Do

### ❌ Pitfall 1: Not Using Realistic Think-Time

**Problem**: Firing requests as fast as possible without pauses between them.

```java
// BAD - No think time
scenario("Bad User Journey")
    .exec(http("Step 1").get("/api/products"))
    .exec(http("Step 2").get("/api/product/123"))
    .exec(http("Step 3").post("/api/cart"))
    .exec(http("Step 4").post("/api/checkout"))
    // All executed instantly, one after another!
```

**Why it's bad**:
- Real users don't fire requests instantly
- Real user reads product description (10-30 sec)
- Real user reviews options (5-15 sec)
- Real user decides whether to buy (30-60 sec)
- Your test doesn't reflect reality
- You're testing code path, not user behavior

```
Real flow:
GET /api/products → pause 10-30s → GET /api/product/123
→ pause 15s → POST /api/cart → pause 20s → POST /api/checkout

Simulated flow (bad):
GET /api/products → GET /api/product/123 → POST /api/cart → POST /api/checkout
(All in <100ms, unrealistic spike)
```

**Fix**:

```java
// GOOD - Realistic think-time
scenario("Realistic User Journey")
    .exec(http("Browse Products").get("/api/products"))
    .pause(10, 30)  // 10-30 seconds think time
    .exec(http("View Product Detail").get("/api/product/#{productId}"))
    .pause(5, 15)
    .exec(http("Add to Cart").post("/api/cart"))
    .pause(20, 40)
    .exec(http("Checkout").post("/api/checkout"))
```

**Lesson**: Your test should simulate real user behavior, not optimal network conditions.

---

### ❌ Pitfall 2: Load Testing from Single Machine (Becoming Bottleneck)

**Problem**: Running 10,000 simulated users from your laptop.

```
┌─ Your laptop (1 machine)
│  ├─ 10,000 simulated users
│  ├─ CPU: 100% (maxed out)
│  ├─ Network: Saturated
│  └─ Garbage collection pauses
│
└─ API server being tested
   ├─ CPU: 20%
   └─ "The API is fast!" (not true, your load generator is the bottleneck)
```

**Why it's bad**:
- Single machine has CPU/memory/network limits
- Gatling JVM becomes bottleneck, not your API
- Results are meaningless (measuring load generator, not API)
- Can't simulate realistic request patterns

**Fix**:

```
Option 1: Gatling Enterprise (cloud)
└─ Distribute load across multiple cloud instances

Option 2: Self-hosted distribution
├─ Run Gatling from multiple machines
├─ Coordinate via shared report
└─ Aggregate results

Option 3: Start small, scale gradually
├─ 100 users on 1 machine (safe)
├─ 1000 users: Still 1 machine (getting risky)
├─ 10,000 users: Use 10 machines (1000 users each)
└─ Rule: 1,000 users max per machine
```

**Lesson**: As load increases, distribute it across machines.

---

### ❌ Pitfall 3: Testing Production Without Permission

**Problem**: Running load test against production database/API.

**Why it's bad**:
- Real users experience your test (slow site for actual customers)
- Logs polluted with fake data
- Alerts triggered (fire department called for practice drill)
- Load test metrics mixed with real user metrics
- Compliance violation (using production for testing)

**Fix**: Always test in **staging** environment.

```
Environment hierarchy:

Local Dev
├─ Fast iteration
├─ Unrealistic scale
└─ Solo testing

Staging/QA
├─ Production-like setup (data, scale, infrastructure)
├─ Safe to overload
├─ Load testing here ✓ CORRECT
└─ Results reliable

Production
├─ Real users
├─ Real data
├─ Real revenue impact
└─ Load testing here ✗ NEVER
```

**Lesson**: Load test only in staging.

---

### ❌ Pitfall 4: Ignoring the Warm-Up Phase

**Problem**: Starting test immediately without system warm-up.

```
Test results (no warm-up):
├─ First minute: p95 = 5000ms (JVM warming up, caches cold)
├─ Second minute: p95 = 2000ms (some caches warming)
├─ Third minute: p95 = 500ms (steady state)
├─ ...10 minutes...
├─ p95 = 500ms (stable)
│
└─ Average: 1000ms (high, but misleading!)
   (Skewed by cold-start phase)

Correct approach (with warm-up):
├─ Warm-up (5 min) + skip in results
├─ Actual test (15 min) + measure
└─ p95 = 500ms (real, stable performance)
```

**Why it's important**:
- JVM just-in-time compiler needs warm-up
- Database query caches need population
- Connection pools need initialization
- First few minutes unrealistically slow

**Fix**:

```java
setUp(
    scenario.injectOpen(
        constantUsersPerSec(100).during(300),  // Warm-up: 5 min
        constantUsersPerSec(100).during(900)   // Actual test: 15 min
    )
)
```

Then analyze only the second phase (after warm-up).

**Lesson**: Add 5-10 minute warm-up before measuring actual metrics.

---

### ❌ Pitfall 5: Only Tracking Mean Latency

**Problem**: Using mean latency to judge performance.

```
Test results:
├─ Mean: 200ms ✓ Looks good!
├─ p99: 5000ms ✗ But 1% of users wait 5 seconds!

Mean is misleading because:
└─ 100 requests: 99 at 100ms, 1 at 10,000ms
   └─ Mean = (99×100 + 1×10,000) ÷ 100 = 199ms
      (Mean hides the outlier!)
```

**Fix**: Always track percentiles.

```java
.assertions(
    global().responseTime().p50().lt(100),      // Median
    global().responseTime().p95().lt(500),      // 95th percentile
    global().responseTime().p99().lt(1000),     // 99th percentile
    global().responseTime().p99d9().lt(3000)    // 99.9th percentile
)
```

**Lesson**: Percentiles tell the real story, mean is misleading.

---

### ❌ Pitfall 6: Not Testing Error Scenarios

**Problem**: Only testing the happy path (everything succeeds).

**Why it's bad**:
- Real world has failures (database down, 5xx errors)
- Cascading failures hidden
- Circuit breaker behavior untested
- Error handling code untested

**Fix**: Include error injection in tests.

```java
scenario("Realistic Journey with Errors")
    .exec(http("Get products").get("/api/products"))
    .pause(5)
    // 10% of users see slow/error response
    .exec(http("Get details - sometimes slow")
        .get("/api/product/#{productId}")
        .simu lateServerErrors(0.1))  // 10% return 500
    .pause(5)
    .exec(http("Add to cart").post("/api/cart"))
    // ... rest of journey
```

**Lesson**: Test realistic failure scenarios.

---

### ❌ Pitfall 7: One Test and Done

**Problem**: Running single load test and assuming you're done.

```
Bad approach:
Run constant load test once → metrics look OK → Deploy ✗

Missing:
├─ Stress test: Where's the breaking point?
├─ Soak test: Stable overnight or memory leak?
├─ Spike test: Can we survive viral moment?
└─ Re-test after optimization: Did fix work?
```

**Fix**: Run multiple test types.

```
Complete test suite:

1. Load Test (baseline)
   └─ Does it meet SLAs?

2. Ramp Test (capacity)
   └─ Where does it break?

3. Soak Test (stability)
   └─ Stable overnight?

4. Spike Test (resilience)
   └─ Survives viral moment?

Timeline: ~2-3 hours of testing (scheduled)
```

**Lesson**: One test reveals one thing. Use multiple tests for comprehensive understanding.

---

### ❌ Pitfall 8: Not Monitoring System During Test

**Problem**: Only looking at Gatling report, ignoring server metrics.

```
Scenario: Test shows latency increasing, but why?

❌ Bad: Only Gatling report
├─ Conclusion: "API is slow"
└─ Action: Scale API (wastes money)

✓ Good: Gatling + Datadog
├─ Gatling says: p95 = 1000ms (increasing)
├─ Datadog shows: Database CPU = 95% (the real bottleneck)
└─ Action: Optimize database (cheaper fix)
```

**Fix**: Monitor both client (Gatling) and server (Datadog).

```
During test, watch:
├─ Gatling console (RPS, latency, errors)
├─ Datadog APM (traces, slow operations)
├─ Datadog Metrics (CPU, memory, disk)
├─ Application logs (errors, exceptions)
└─ Database monitoring (slow queries, locks)

Tools:
├─ Gatling: Response times (client-side)
├─ Datadog APM: Where time is spent (server-side)
├─ Datadog Metrics: Resource utilization
└─ Logs: Error details
```

**Lesson**: Monitor both sides to find real bottlenecks.

---

## Best Practices

### ✅ Best Practice 1: Define SLAs First

Before writing test code:

```
1. Engage stakeholders
   └─ Product: What do users tolerate?
   └─ Ops: What's infrastructure capable of?
   └─ Eng: What's reasonable to achieve?

2. Document SLAs
   └─ p95 <300ms
   └─ p99 <800ms
   └─ Success rate >99.9%
   └─ Uptime >99.95%

3. Build tests to validate SLAs
   └─ Test configured with assertions
   └─ Fails if SLA not met
   └─ Clear pass/fail result
```

**Lesson**: SLAs should come first, tests second.

---

### ✅ Best Practice 2: Test in Realistic Environments

```
Testing checklist:

Infrastructure:
☐ Staging matches production (same config, scale)
☐ Database is production-sized (at least)
☐ Network latency simulated (not zero)
☐ External services accessible (or mocked realistically)

Data:
☐ Real data patterns (not synthetic nonsense)
☐ Production-sized dataset (not just 100 rows)
☐ Realistic skew (80% users browse, 20% purchase)

Load:
☐ Peak traffic levels (not undersized)
☐ Realistic user behavior (think-time, variety)
☐ Distribution across features (not single endpoint)
```

**Lesson**: Realistic testing = reliable results.

---

### ✅ Best Practice 3: Establish Baseline, Then Optimize

```
Workflow:

1. Baseline test
   └─ Run current code/infrastructure
   └─ Record: p95=800ms, CPU=70%
   └─ This is your baseline (might be bad, but you know it)

2. Optimization attempt
   └─ Change code: Add caching, optimize query, etc.
   └─ Re-test with SAME load

3. Compare
   └─ p95: 800ms → 300ms ✓ 62% improvement
   └─ CPU: 70% → 30% ✓ Much better
   └─ Declare success!

Why baseline matters:
├─ You have a target to beat
├─ Optimization benefits are measurable
├─ Without baseline, "faster" is subjective
└─ Good for reporting improvement to business
```

**Lesson**: Baseline first, optimize second, measure improvement.

---

### ✅ Best Practice 4: Version Control Your Tests

```
Git repo structure:

gatling-tests/
├── README.md (how to run tests)
├── src/test/java/simulations/
│   ├── baseline_scenario.java
│   ├── peak_load_scenario.java
│   ├── spike_scenario.java
│   └── soak_scenario.java
├── src/test/resources/
│   ├── data/
│   └── bodies/
├── reports/
│   ├── 2024-01-15-baseline.html
│   ├── 2024-01-15-peak.html
│   └── ... (historical results)
└── pom.xml

Benefits:
├─ Track changes to scenarios over time
├─ Reproduce exact test conditions
├─ Share tests with team
├─ CI/CD integration easy
```

**Lesson**: Treat tests like code—version control them.

---

### ✅ Best Practice 5: Document Results

```
Test Report Template:

Date: 2024-01-15
Simulation: Peak Load Test
Environment: Staging
Duration: 30 minutes

Objectives:
- Verify system handles Black Friday traffic
- Find breaking point at 10,000 concurrent users

Scenario:
- 100 new users per second for 30 minutes
- Realistic think-time (5-30 second pauses)
- CSV feeder with 10,000 product IDs
- Equal distribution: 70% browse, 20% purchase, 10% support

Results:
- Total requests: 180,000
- Successful: 179,640 (99.8%) ✓
- Failed: 360 (0.2%)
- p95 latency: 450ms ✓ (target: <500ms)
- p99 latency: 950ms ✓ (target: <1000ms)
- Max RPS achieved: 1,850 (during ramp-down)
- Peak CPU (server): 78%
- Peak memory (server): 4.2GB (stable)

Bottlenecks:
- Database query time increasing with load
  ├─ Root cause: Missing index on product.category column
  ├─ Evidence: Datadog shows 300ms in SELECT query
  └─ Fix: Add index (planned for next sprint)

Recommendation:
✓ PASS SLA for current load levels
✓ Ready for 2x traffic growth
✓ Recommend index optimization for 3x growth

Next Steps:
1. Add database index
2. Re-test to confirm improvement
3. Plan capacity for Q1
```

**Lesson**: Document findings for future reference and accountability.

---

### ✅ Best Practice 6: Automate Tests in CI/CD

```
GitHub Actions example:

name: Load Tests
on:
  schedule:
    - cron: '0 2 * * *'  # Nightly at 2am

jobs:
  load-test:
    runs-on: ubuntu-latest
    services:
      - docker (for staging environment)
    steps:
      - uses: actions/checkout@v2
      - name: Run Gatling baseline test
        run: mvn gatling:test -Dgatling.simulationClass=BaselineTest
      - name: Check assertions
        run: | # Fail if SLAs not met
          if ! grep -q "Assertions passed" target/gatling/*/index.html; then
            exit 1
          fi
      - name: Upload report
        uses: actions/upload-artifact@v2
        with:
          name: gatling-report
          path: target/gatling/*/
      - name: Notify Slack
        run: |
          curl -X POST -d '{"text":"Load test completed"}' $SLACK_WEBHOOK
```

**Benefits**:
- Tests run automatically
- SLA violations caught immediately
- Historical trends tracked
- Prevent regression

**Lesson**: Automate tests; don't rely on manual execution.

---

### ✅ Best Practice 7: Communicate Results

```
Stakeholder Communication:

For Product/Business:
├─ "System handles 10,000 concurrent users ✓"
├─ "p95 latency is 300ms (target: 300ms) ✓"
└─ "Safe to launch feature on schedule"

For Ops/DevOps:
├─ "Peak CPU: 75%, headroom: 25%"
├─ "Database connections: 45/50 (one more scaling needed)"
└─ "Infrastructure recommendation: Add 1 more instance"

For Engineering:
├─ "Database query is bottleneck (400ms out of 500ms latency)"
├─ "Adding index on users.email should improve by 60%"
└─ "Priority: Optimize slow query, then re-test"

For Executives:
├─ "We can handle 3x current traffic"
├─ "No infrastructure scaling needed yet"
├─ "Ready for holiday season traffic surge"
```

**Lesson**: Tailor message to audience; everyone speaks different language.

---

## Summary: Do's and Don'ts

### ✅ DO:
- Define clear SLAs before testing
- Test in realistic staging environment
- Use realistic think-time in scenarios
- Monitor both client (Gatling) and server (Datadog)
- Test multiple scenarios (load, ramp, soak, spike)
- Establish baseline before optimizing
- Document findings and communicate results
- Re-test after any changes
- Automate tests in CI/CD pipeline
- Use percentiles (p95, p99), not mean

### ❌ DON'T:
- Fire all requests instantly (no think-time)
- Load test from single overloaded machine
- Test against production
- Start test without warm-up
- Track only mean latency
- Test only happy path (ignore errors)
- Run one test and assume you're done
- Monitor only Gatling, ignore server metrics
- Deploy without load test validation
- Test at same load level each time (no learning)

---

## Next Steps

→ **Read next**: [Gatling Concepts: Architecture](../02-gatling-concepts/01-gatling-architecture.md) - Understand Gatling framework

