# Open Load Patterns

## What is an "Open Model"?

In Gatling, there are two injection models:

| Model | Behavior |
|:------|:---------|
| **Closed Model** | Fixed number of users cycle through scenario (e.g., "10 users, each repeat forever") |
| **Open Model** | Unlimited concurrent users; new users arrive at a rate you specify |

Open models simulate **unlimited traffic arriving at a constant/varying rate**, which matches real-world scenarios better:

```
Closed (limited users):
├─ 10 users always active
├─ User 1 finishes request, immediately starts next
├─ Total concurrency is bounded
└─ Example: 10 customer service reps handling tickets

Open (unlimited users):
├─ New users arrive at 100/sec (open-ended)
├─ Each user makes requests and leaves
├─ Total concurrency depends on request duration
└─ Example: E-commerce website during shopping
```

---

## OPEN_CONSTANT_LOAD

Inject new virtual users at a **constant rate** throughout the test.

### Definition

```
constantUsersPerSec(rate).during(duration)

Example: constantUsersPerSec(100).during(300)
├─ Inject 100 new users every second
├─ For 300 seconds (5 minutes)
└─ Total users injected: 100 × 300 = 30,000 user sessions
```

### Execution Timeline

```
Time (sec) | Users/sec injected | Active users | Actions
-----------|-------------------|--------------|----------
0-10       | 100               | Growing      | Users arriving, requests starting
10-20      | 100               | ~Peak        | Steady state, some users finishing
20-300     | 100               | ~Constant    | Stable, user turnover
300+       | 0                 | Declining    | No new users, existing finish
```

### Real-World Scenarios

**Scenario 1: E-commerce baseline**
```
Situation: Normal weekday traffic to shop
Load pattern: 100 new customers/second for 30 minutes
What this means:
├─ Customers arrive steadily
├─ ~100 browsing, adding to cart, checking out at once
├─ System should handle 100 concurrent requests consistently
└─ Latency should remain stable (not increase over time)
```

**Scenario 2: API rate limiting test**
```
Situation: Test API performance with guaranteed TPS
Load pattern: 1000 RPS (1000 new requests/sec for 10 minutes)
What this means:
├─ Each second, 1000 requests must be processed
├─ API must sustain 1000 RPS without degrading
└─ Test verifies: Can we hit rate limit? Is it enforced?
```

### Metrics to Observe

```
During test, track:

┌─ Latency (should be STABLE)
│   p95 latency at 0min: 150ms
│   p95 latency at 2min: 155ms  ✓ Stable
│   p95 latency at 4min: 160ms  ✓ Still stable
│   If p95 jumps to 500ms, something is saturating
│
├─ Throughput (should PLATEAU at RPS)
│   Should process ~100 RPS consistently
│   If it drops to 50 RPS, system is struggling
│
├─ Error rate (should be ZERO or constant)
│   <0.1% errors throughout
│   Sudden spike indicates overload
│
├─ CPU/Memory (should be STABLE or LINEAR)
│   CPU: steady 40-60%  ✓ Good
│   Memory: flat line   ✓ Good (no leak)
│   If memory grows: potential leak detected
│
└─ Success rate (should be CONSISTENT)
   99.9% throughout
   If drops to 95%, something failed
```

### Success Criteria

```
Test passes if:
✅ Latency stays stable (p95 doesn't increase >20%)
✅ RPS achieved as planned (100 RPS = 30,000 total requests)
✅ Error rate stays <0.1%
✅ No cascading failures
✅ System recovers after test ends
```

---

## OPEN_RAMP_LOAD

Gradually **increase the rate** of user arrivals from start to end load.

### Definition

```
rampUsersPerSec(startRate).to(endRate).during(duration)

Example: rampUsersPerSec(10).to(100).during(300)
├─ Start: 10 new users/sec
├─ End: 100 new users/sec
├─ Ramp over: 300 seconds (5 minutes)
├─ Pattern: Linear increase (10 → 20 → 30 ... → 100)
└─ Total users: ~16,500 (average 55/sec × 300 sec)
```

### Execution Timeline

```
Time   | Inject Rate | Cumulative | Active Users | Latency | CPU
-------|-------------|------------|--------------|---------|-----
0s     | 10/sec      | 10         | 10           | 50ms    | 5%
30s    | 23/sec      | 680        | 30-40        | 60ms    | 10%
60s    | 36/sec      | 1,800      | 50-60        | 75ms    | 15%
120s   | 56/sec      | 5,200      | 80-100       | 120ms   | 25%
180s   | 76/sec      | 9,600      | 120-140      | 250ms   | 40%  ← Latency increasing
240s   | 90/sec      | 13,200     | 150-170      | 600ms   | 60%  ← Approaching limit
300s   | 100/sec     | 16,500     | 170-200      | 1500ms  | 75%  ← Near saturation
```

### Real-World Scenarios

**Scenario 1: Event launch**
```
Situation: New feature launches to growing user base
Load pattern: 10 users/sec → 100 users/sec over 30 minutes
What this means:
├─ Feature goes live, slowly picks up traffic
├─ As word spreads, more users discover it
├─ Test simulates: gradual ramp as users hear about new feature
└─ Objective: Ensure system can handle growth
```

**Scenario 2: Daily traffic pattern**
```
Situation: Rush hour peak (6pm-9pm)
Load pattern: 100 users/sec → 1000 users/sec over 1 hour
What this means:
├─ 6pm: People get home, open app (100 users/sec)
├─ 7pm: More people logging in (500 users/sec)
├─ 8pm: Peak usage (1000 users/sec)
└─ Test verifies: Can system handle gradual peak growth?
```

### Key Observation: Finding Breaking Point

Ramp tests reveal where system degrades:

```
Ramp from 10 to 100 RPS over 10 minutes

Results:
RPS → Latency
10  → 50ms  ✓ Excellent
20  → 60ms  ✓ Good
30  → 80ms  ✓ Good
40  → 120ms ✓ Acceptable
50  → 200ms ⚠ Getting slow
60  → 500ms ⚠ Concerning
70  → 1500ms ❌ Unacceptable
80  → 5000ms ❌ Breaking

Conclusion:
├─ System handles up to 50 RPS comfortably
├─ 60-70 RPS range is where it starts degrading
├─ >70 RPS is unacceptable
└─ Safe operating range: 40-50 RPS (with headroom)
```

### Metrics to Observe

```
Track these over the ramp:

1. Latency curve
   ├─ Should be flat initially
   ├─ Should increase smoothly
   └─ Should NOT spike unpredictably

2. Error rate
   ├─ Should stay <0.1% for most of ramp
   ├─ May increase near the end (nearing limit)
   └─ Should not jump suddenly

3. Resource utilization
   ├─ CPU should increase linearly with load
   ├─ Memory should be stable
   └─ Disk I/O should increase gradually

4. Identify the "elbow"
   └─ The point where latency starts increasing
      sharply (exponential vs linear)
```

### Success Criteria

```
Test passes if:
✅ Latency increases smoothly (no sudden jumps)
✅ System reaches target load
✅ Error rate stays <0.1% until near breaking point
✅ Obvious breaking point is identifiable
✅ System is stable (not cascading failures)
```

---

## OPEN_STEP_LOAD

Increase load in **discrete steps** (like stairs), holding each step for a duration.

### Definition

```
stepsTo pattern (Gatling v3.13+):

Example:
  .injectOpen(
      constantUsersPerSec(10).during(300),   // Step 1: 10/sec for 5 min
      constantUsersPerSec(20).during(300),   // Step 2: 20/sec for 5 min
      constantUsersPerSec(50).during(300),   // Step 3: 50/sec for 5 min
      constantUsersPerSec(100).during(300)   // Step 4: 100/sec for 5 min
  )

Total duration: 20 minutes
Steps: 4 distinct levels
```

### Execution Timeline

```
Step 1 (10/sec, 5 min):    Baseline, system cold
   │                       ├─ Caches not populated
   │                       ├─ Connection pools warming up
   │                       └─ p95 latency: 50-100ms
   │
   ├─ 5 min wait (system settles)
   │
Step 2 (20/sec, 5 min):    2x baseline load
   │                       ├─ Caches warmed up
   │                       ├─ More connections used
   │                       └─ p95 latency: 80-120ms (increase expected)
   │
   ├─ 5 min wait (observe stability)
   │
Step 3 (50/sec, 5 min):    2.5x load
   │                       ├─ Cache hit rate peak
   │                       ├─ Connection pool busy
   │                       └─ p95 latency: 150-250ms
   │
   ├─ 5 min wait
   │
Step 4 (100/sec, 5 min):   10x baseline load
   │                       ├─ Approaching saturation
   │                       ├─ CPU climbing
   │                       └─ p95 latency: 500-1500ms

Timeline view:
Latency
    │     ╱─
  500│    ╱  ├─────
    │   ╱    │
  250│  ╱     ├────
    │ ╱      │
  100├────┐   ├───
    │     │   │
   50├─────┐  │
    │           time →
    0 5 10 15 20 (min)
```

### Real-World Scenarios

**Scenario 1: Database migration testing**
```
Situation: Planning to migrate from MySQL to PostgreSQL
Load pattern: Step from 10 → 20 → 50 → 100 RPS at 5-min intervals
What this means:
├─ Step 1: Let new DB warm up (caches, indices)
├─ Step 2: Observe: Did latency increase? Is replication lag OK?
├─ Step 3: Push harder, see if indexing works
└─ Step 4: Near-breaking-point test
Result: "We're safe at 80 RPS; migration is OK"
```

**Scenario 2: Cache effectiveness analysis**
```
Situation: Want to verify cache is actually helping
Load pattern: Step test from 10 → 20 → 50 → 100 RPS
What to watch:
├─ Step 1: Cache cold, latency high (no cached data)
├─ Step 2: Cache warming, latency should DROP
├─ Step 3: Cache warm, latency flat (good hit rate)
└─ Step 4: Cache still working, latency increases due to load
Result: Can see exactly when cache impacts performance
```

**Scenario 3: Connection pool behavior**
```
Situation: Want to understand connection pool utilization
Load pattern: Step test, monitoring pool size at each step
Observations:
├─ Step 1 (10/sec): 5/50 connections used (pool has room)
├─ Step 2 (20/sec): 12/50 connections used
├─ Step 3 (50/sec): 35/50 connections (getting tight)
├─ Step 4 (100/sec): 50/50 connections + 20 waiting in queue
Result: "We need to increase pool size or optimize query times"
```

### Metrics to Observe

```
At EACH step, record:

Step level | Active users | p95 latency | p99 latency | CPU | Connections
-----------|-------------|-------------|-------------|-----|-------------
   10/sec  |    50       |    80ms     |   120ms     | 15% |  10/50
   20/sec  |   100       |   100ms     |   150ms     | 25% |  20/50
   50/sec  |   250       |   200ms     |   400ms     | 50% |  40/50
  100/sec  |   500       |   800ms     |  2000ms     | 80% |  50/50 + Q

Patterns to look for:

1. Linear scaling (good)
   └─ Latency increases proportionally with load

2. Super-linear scaling (bad)
   └─ Latency increases more than load increases

3. Sudden step up (concerning)
   └─ Latency jumps at specific load level (pool exhaustion, etc.)

4. Saturation (end of test)
   └─ Last step shows unacceptable latency
```

### Success Criteria

```
Test passes if:
✅ Each step is stable (latency flat during the step)
✅ Between steps, system recovers quickly
✅ Latency increase is gradual (not sudden spikes)
✅ Resources utilized efficiently (no waste)
✅ Identify safe operating point clearly
✅ Identify breaking point clearly
```

---

## OPEN_SPIKE_LOAD

**Sudden jump** to high load, then drop back down.

### Definition

```
Example:
  .injectOpen(
      constantUsersPerSec(100).during(300),      // Normal: 100/sec for 5 min
      rampUsersPerSec(100).to(5000).during(30),  // Spike: Jump to 5000/sec in 30sec
      constantUsersPerSec(5000).during(60),      // Hold spike: 5000/sec for 1 min
      rampUsersPerSec(5000).to(100).during(30),  // Drop: Back to 100/sec in 30sec
      constantUsersPerSec(100).during(300)       // Recovery: Monitor for 5 min
  )
```

### Execution Timeline

```
Phase 1: Normal
├─ 100 RPS for 5 minutes
├─ p95 latency: 150ms
├─ System stable, CPU 30%
└─ Cache warmed up

   │ "News goes viral on Twitter"
   ↓

Phase 2: Spike (incoming 5000/sec)
├─ Latency jumps: 150ms → 3000ms
├─ Queue forms: requests backlog
├─ CPU: 30% → 95%
├─ Circuit breaker might trip
└─ Load balancer might drop connections

Phase 3: During spike (holding at 5000/sec)
├─ System either:
│  A) Auto-scales (good): New instances start
│  B) Degrades (bad): Latency remains at 3000ms
│  C) Crashes (worst): Service becomes unavailable
└─ Error rate climbs if not handling well

Phase 4: Recovery phase (back to 100/sec)
├─ Queue drains (high latency for a while)
├─ Auto-scaled instances scale back down
├─ p95 latency: Should return to 150ms
└─ CPU: Should return to 30%

Phase 5: Post-recovery monitoring
├─ Is system stable?
├─ Any data corruption?
├─ Any lingering issues?
└─ Memory stable (no leak from scaling)?
```

### Real-World Scenarios

**Scenario 1: Viral moment**
```
Situation: Your product trends on social media
Normal load: 100 users/sec
Spike: 5000 users/sec (50x increase!)
Recovery: Back to 100 users/sec

What might happen:
├─ Spike hits: Requests queue, latency explodes
├─ Auto-scaler detects high CPU/latency
├─ New instances spin up (takes 30-60 seconds in AWS)
├─ System gradually handles more load
├─ Customers experience 2-5 second waits initially
├─ After 2-3 minutes: New instances online
├─ Load distributes: Latency improves to 500ms
├─ Spike passes: Traffic drops back to normal
├─ System scales down: Instances terminate after 5 min idle
└─ Back to baseline: Everything normal

Test objective: Verify system doesn't CRASH, degradation is acceptable
```

**Scenario 2: Flash sale**
```
Situation: Black Friday 20% off promotion goes live
Normal load: 50 users/sec
Spike: 2000 users/sec (40x increase)
Recovery: Back to 50 users/sec over 30 minutes

What we're testing:
├─ Inventory updates don't get corrupted
├─ Payment processing doesn't fail
├─ Database doesn't deadlock
└─ System recovers cleanly
```

### Metrics to Observe

```
During spike test:

Before spike:
├─ p95 latency: 150ms
├─ Error rate: 0.05%
├─ CPU: 30%
└─ RPS: 100

During spike onset (first 30 seconds):
├─ p95 latency: 3000ms (acceptable, but high)
├─ p99 latency: 10000ms (some requests timing out)
├─ Error rate: 0.5-2% (some overloaded errors)
├─ CPU: 95-100%
├─ RPS still ~100 (queue building, not processing 5000 yet)
└─ Circuit breaker: May be open, rejecting requests

After recovery:
├─ p95 latency: Should return to 150ms
├─ Error rate: Should return to <0.1%
├─ CPU: Should return to 30%
└─ RPS: Should return to 100
```

### Success Criteria

```
Test passes if:
✅ System doesn't crash during spike
✅ Error rate stays <5% during spike
✅ Circuit breaker works (graceful degradation)
✅ System recovers to baseline within 5-10 minutes
✅ No data corruption during spike
✅ No lingering issues after recovery
```

---

## Comparison: All Four Patterns

| Pattern | Use Case | Duration | Objective |
|:--------|:---------|:---------|:----------|
| **Constant** | Baseline testing, API rate limits | 5-30 min | "Do we meet SLAs?" |
| **Ramp** | Find breaking point, capacity planning | 10-30 min | "Where does it break?" |
| **Step** | Threshold analysis, cache behavior | 20-30 min | "What's safe max load?" |
| **Spike** | Recovery, resilience, auto-scaling | 15-20 min | "Can we handle surprises?" |

---

## Common Pattern Sequences

### For Initial Testing (Full Test: ~2 hours)

```
1. Smoke test: 1 user, 1 iteration
   └─ Verify code compiles, no errors (5 min)

2. Constant load test: 100 RPS for 15 minutes
   └─ Establish baseline (20 min)

3. Wait 10 minutes
   └─ Let system cool down (10 min)

4. Ramp test: 10 → 500 RPS over 30 minutes
   └─ Find breaking point (40 min)

5. Wait 10 minutes
   └─ Cool down (10 min)

6. Step test: 50 → 100 → 200 → 400 RPS (5 min each)
   └─ Understand thresholds (30 min)

Total: ~2 hours of testing
```

### For Soak Testing (Overnight)

```
1. Constant: 100 RPS for 12 hours
   └─ Monitor for memory leaks, degradation
```

---

## Next Steps

→ **Read next**: [Performance Targets & SLAs](05-performance-targets-slas.md) - Industry benchmarks

