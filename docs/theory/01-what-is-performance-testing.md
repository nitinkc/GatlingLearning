# What is Performance Testing?

## Overview

**Performance testing** is the practice of systematically evaluating how your system behaves under realistic and stress conditions. Instead of testing whether your system *works*, you're testing whether it *works well enough* under expected and extreme loads.

### The Core Question

> **"How does our system behave as load increases?"**

---

## Why Performance Testing Matters

Imagine releasing a new e-commerce feature:

```
✓ All unit tests pass
✓ All integration tests pass
✓ Manual testing passes
✓ Code review approved
  ... but on Black Friday sales launch...
  
❌ Site crashes under 10,000 concurrent users
❌ Response times jump from 200ms to 30 seconds
❌ Database connections exhausted
❌ Cache thrashing, cascading failures
```

Performance testing would have **caught this before production**.

### Business Impact of Performance Issues

| Scenario | Impact |
|----------|--------|
| **Response time >3 seconds** | 40% user abandonment (Google studies) |
| **Site downtime** | ~$5,600 lost per minute for mid-size e-commerce |
| **Slow API** | 10% decrease in mobile app engagement |
| **Database bottleneck** | 100% CPU, service unavailable, SLA breach |

---

## What Performance Testing Reveals

### 1. **Capacity: How Much Load Can You Handle?**

```
Load (users) ↑    Latency (response time)
    1,000         150ms ✓ Acceptable
   10,000         200ms ✓ Still good
  100,000         500ms ⚠ Approaching limit
  500,000       5,000ms ❌ Unacceptable
1,000,000       System crashes
```

**Outcome**: You know the maximum sustainable load before degradation.

### 2. **Performance: How Fast Do Responses Come?**

```
Without load:     GET /api/users → 50ms  (server idle)
With 100K users:  GET /api/users → 500ms (CPU-bound)
With 500K users:  GET /api/users → 3000ms (I/O-bound, DB queue)
```

**Outcome**: You understand the performance curve and where bottlenecks emerge.

### 3. **Stability: Does the System Degrade Gracefully?**

```
✓ Good: Latency increases linearly, recovers when load drops
✗ Bad: Latency spikes unpredictably, doesn't recover
❌ Catastrophic: Crash, data corruption, cascading failures
```

**Outcome**: You know if your system can degrade gracefully under overload.

### 4. **Bottlenecks: Where is Time Actually Spent?**

```
Request Timeline (500ms total):
├─ API Gateway: 10ms
├─ Authentication: 5ms
├─ Database query: 400ms ⬅️ BOTTLENECK!
├─ Serialization: 50ms
├─ Network: 35ms
└─ Total: 500ms
```

**Outcome**: You know exactly what to optimize (database in this case).

---

## Types of Performance Testing

### 1. **Load Testing**
Tests system behavior under **expected, realistic load**.

```
Scenario: E-commerce site expecting 500 concurrent users during normal hours

Load pattern:
├─ Start: 0 users
├─ Ramp up: 500 users over 5 minutes
├─ Hold: 500 users for 30 minutes
└─ Ramp down: Back to 0 users

Objective: Does the system meet SLAs (p95 <500ms, <0.1% errors)?
```

| Metric | Target | Status |
|--------|--------|--------|
| p95 latency | <500ms | 400ms ✓ |
| p99 latency | <1000ms | 800ms ✓ |
| Error rate | <0.1% | 0.05% ✓ |
| **Result** | **Pass SLA** | ✅ **PASS** |

### 2. **Stress Testing**
Tests system behavior at **breaking point** and beyond.

```
Scenario: "What is the absolute maximum load we can sustain?"

Load pattern:
├─ Start: 0 users
├─ Ramp up: 5,000 users per minute (very aggressive)
├─ Continue until: System crashes or latency becomes unbearable
└─ Observe: Where does it break?

Observations:
├─ 2,000 users: p95=400ms (OK)
├─ 5,000 users: p95=1000ms (slow but stable)
├─ 10,000 users: p95=5000ms (degrading)
├─ 12,000 users: p99=30,000ms (queue building up)
└─ 15,000 users: 50% errors, cascading failures

Conclusion: Safe limit is ~10,000 concurrent users
```

### 3. **Soak Testing**
Tests system **stability over extended time** under normal load.

```
Scenario: "Does the system stay healthy over 24 hours?"

Load pattern:
├─ Start: 0 users
├─ Ramp: 500 users over 10 minutes
├─ Hold: 500 users for 24 hours (86,400 seconds)
├─ Monitor: Memory, CPU, connection pools, GC pauses

Potential issues detected:
├─ Memory leak: Heap grows from 1GB → 8GB over 24 hours
├─ Connection pool exhaustion: Connections not returned
├─ Slowdown: p95 latency drifts from 200ms → 1000ms
└─ Cache thrashing: Cache hit rate degrades

Action: Find and fix leaks before production deployment
```

### 4. **Spike Testing**
Tests system **recovery from sudden traffic surge**.

```
Scenario: "New feature goes viral on Twitter (10,000 sudden users)"

Load pattern:
├─ Start: 100 users (normal)
├─ Spike: Jump to 10,000 users instantly
├─ Hold: 10,000 users for 5 minutes
├─ Normal: Drop back to 100 users
└─ Observe: Recovery behavior

Expected behavior:
├─ During spike: Acceptable latency increase (maybe 2-3x)
├─ Circuit breaker triggers: Graceful degradation
├─ Auto-scaling activates: New instances spin up
├─ After spike: Smoothly returns to normal latency

Bad behavior:
├─ System crashes (cascading failures)
├─ Doesn't recover even after load drops
├─ Database locked, data corruption
```

---

## Comparison: All Four Types

| Type | Load Pattern | Duration | Purpose | Real-World Example |
|:-----|:-------------|:---------|:--------|:-------------------|
| **Load** | Realistic, constant | 5-30 min | Baseline performance | Normal business hours |
| **Stress** | Continuously increase | Until break | Find limits | How much can we grow? |
| **Soak** | Normal load, extended | 8-24 hours | Long-term stability | Overnight batch processing |
| **Spike** | Sudden jump, then drop | 5-10 min | Recovery behavior | Viral content, flash sales |

---

## Performance Testing Lifecycle

```
1. PLAN
   ├─ Define objectives (SLAs)
   ├─ Identify critical paths
   └─ Set success criteria (p95 <500ms, <0.1% errors)

2. DESIGN SCENARIOS
   ├─ User journeys (realistic behavior)
   ├─ Data composition (realistic data mix)
   └─ Load patterns (constant, ramp, spike, etc.)

3. IMPLEMENT SIMULATIONS
   ├─ Code scenarios in Gatling
   ├─ Create feeders (user data, product data)
   └─ Set up assertions and checks

4. EXECUTE TESTS
   ├─ Run in staging environment (not production!)
   ├─ Monitor system metrics (CPU, memory, disk)
   ├─ Capture APM traces (Datadog)
   └─ Record all metrics

5. ANALYZE RESULTS
   ├─ Compare against SLAs
   ├─ Find bottlenecks (CPU, database, network)
   ├─ Correlate with application traces
   └─ Identify root causes

6. OPTIMIZE & RE-TEST
   ├─ Fix bottleneck (scale, cache, optimize)
   ├─ Re-run same test
   ├─ Verify improvement
   └─ Repeat until SLA met

7. DEPLOY CONFIDENTLY
   ├─ Production deployment with confidence
   ├─ Set up monitoring alerts
   └─ Track real-world performance
```

---

## Key Questions Answered by Performance Testing

### Before Performance Testing
❓ "Will our system handle 10,000 users?"  
❓ "What's the maximum load we can sustain?"  
❓ "Where will our system break?"  
❓ "How do we reduce latency?"  
❓ "Can we scale to 100x current traffic?"  

### After Performance Testing
✅ "Yes, it handles 10,000 users with p95 latency of 400ms"  
✅ "Maximum sustainable load is 15,000 concurrent users"  
✅ "Database queries are the bottleneck (400ms out of 500ms)"  
✅ "Add query caching and connection pooling; optimize N+1 queries"  
✅ "With the optimizations, yes; scaling is straightforward"  

---

## Common Misconceptions

### ❌ "Performance testing is only for high-traffic sites"
**Reality**: Any system deployed to production should be performance tested.
- A banking app with 1,000 users needs different SLAs than an e-commerce site with 100,000 users
- Even "small" systems degrade under load; you need to know your limits

### ❌ "We'll optimize after deployment if needed"
**Reality**: Post-production optimization is exponentially more expensive.
- Production issues impact real users (reputation damage, revenue loss)
- Debugging under real load is chaotic
- Scaling out without understanding bottlenecks wastes infrastructure

### ❌ "One performance test is enough"
**Reality**: You need multiple test types.
- Load test establishes baseline
- Stress test reveals limits
- Soak test catches long-term issues
- Spike test validates resilience
- Re-test after every major change

### ❌ "Performance testing is slow and expensive"
**Reality**: Modern frameworks like Gatling make it fast and cheap.
- Gatling simulates 10,000+ concurrent users on a single machine
- No need for physical test labs with 10,000 computers
- Tests complete in 10-30 minutes
- Total cost: low (just infra for a staging environment)

---

## When to Perform Load Tests

### ✅ Always Test Before:
- **Major release** to production
- **High-traffic event** (Black Friday, sale launch)
- **Feature scaling** (new service, redesign)
- **Infrastructure change** (database migration, version upgrade)
- **Deployment to production** (final gate)

### ✅ Regularly Test:
- **Before each sprint release** (part of Definition of Done)
- **After optimization** (verify improvement)
- **Quarterly baseline** (track system behavior over time)
- **Before and after scaling** (validate scaling strategy)

### ⚠️ Be Careful When Testing:
- **Against production systems** → Always use staging
- **During business hours** → Could impact monitoring, logs
- **Without stakeholder knowledge** → Could trigger alerts

---

## What You'll Learn

In this documentation, you'll learn how to:

1. **Design** realistic load testing scenarios
2. **Implement** tests in Gatling (HTTP and Kafka)
3. **Run** tests with different load patterns (constant, ramp, step, spike)
4. **Analyze** results and find bottlenecks
5. **Monitor** tests with Datadog APM
6. **Optimize** your system based on findings
7. **Integrate** load testing into CI/CD pipelines

---

## Next Steps

→ **Read next**: [Key Metrics & Measurements](02-key-metrics.md) - Understand what to measure and why

