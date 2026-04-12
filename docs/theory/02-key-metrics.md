# Key Metrics & Measurements

## Introduction

Performance metrics tell you **how well your system is actually performing**. Without metrics, you're flying blind.

> **"You can't improve what you don't measure."** — Peter Drucker

This section covers the metrics you'll track during load tests and what they mean.

---

## Latency Metrics (Response Time)

**Latency** is the time from when a request is sent until a response is received.

```
┌─ Client sends request (GET /api/users)
│  
├─ Network transit: 5ms
├─ Server processes: 100ms
├─ Network return: 5ms
│
└─ Response received
   Total latency = 110ms
```

### The Problem with Mean Latency

Many teams track **mean (average) latency**, but it's misleading:

```
Request 1:  50ms
Request 2:  60ms
Request 3:  55ms
Request 4:  70ms
Request 5:  5000ms ← One slow request (cache miss, GC pause)
Request 6:  65ms
Request 7:  60ms
Request 8:  55ms
Request 9:  50ms
Request 10: 70ms

Mean latency = 5535ms ÷ 10 = 553.5ms ← Hiding outliers!
```

Only 1 request was slow, but the mean suggests widespread problems.

### Percentile Latencies (Correct Approach)

**Percentiles** tell you the distribution of response times:

#### P50 (Median)
- **Definition**: 50% of requests complete faster than this time
- **Example**: p50 = 100ms means half your requests are faster than 100ms
- **Use case**: Baseline performance; less useful alone

#### P95 (95th Percentile)
- **Definition**: 95% of requests complete within this time; 5% are slower
- **Example**: p95 = 300ms means:
  ```
  ✓ 9,500 out of 10,000 requests complete in ≤300ms
  ✗ 500 out of 10,000 requests take >300ms
  ```
- **Industry guideline**:
  - Web apps: target <500ms
  - APIs: target <200ms
  - Real-time systems: target <50ms
- **User impact**: Most users have good experience; 5% might notice slowness

#### P99 (99th Percentile)
- **Definition**: 99% of requests complete within this time; 1% are slower
- **Example**: p99 = 800ms means:
  ```
  ✓ 9,900 out of 10,000 requests complete in ≤800ms
  ✗ 100 out of 10,000 requests take >800ms
  ```
- **Industry guideline**:
  - Web apps: target <1000ms
  - APIs: target <500ms
- **User impact**: Occasional users experience significant slowness

#### P99.9 (99.9th Percentile)
- **Definition**: 99.9% of requests complete within this time; 0.1% are slower
- **Example**: p99.9 = 3000ms means:
  ```
  ✓ 9,990 out of 10,000 requests complete in ≤3000ms
  ✗ 10 out of 10,000 requests take >3000ms
  ```
- **Use case**: SLA compliance, extreme outliers
- **User impact**: Rare users experience very slow responses

### Latency Example from Real Test

```
Test: 10,000 requests at constant load

┌─ Percentile Distribution ─────────────────────────
│
│    Count
│      │     ╱╲
│ 1000 │    ╱  ╲
│      │   ╱    ╲___
│  500 │  ╱          ╲
│      │ ╱             ╲
│      │╱_________________╲___
│      └────────────────────────────→ Latency (ms)
│      0    100   200   300   500   5000
│
├─ Key metrics:
│  ├─ Min:  45ms
│  ├─ p50:  100ms  (half faster than 100ms)
│  ├─ p95:  300ms  (95% faster than 300ms)
│  ├─ p99:  1000ms (99% faster than 1000ms)
│  ├─ p99.9: 3000ms (99.9% faster than 3000ms)
│  └─ Max:  5000ms
│
└─ 5 requests took >1000ms (cache misses, GC pauses)
```

### Why Percentiles Matter

```
Scenario: Your p95 = 1000ms (too high!)

Diagnosis options:
❌ Option 1: "Let's use mean to decide"
   Mean = 150ms (looks fine, misleading!)

✓ Option 2: "Check p95, p99"
   p95 = 1000ms (5% of users suffering)
   p99 = 5000ms (1% experience 5-second waits)
   Root cause: Slow database query on specific conditions
   Fix: Add index, optimize query, increase cache TTL
```

---

## Throughput Metrics

**Throughput** measures how much work the system completes.

### RPS (Requests Per Second)

- **Definition**: How many HTTP requests the system processes per second
- **Example**: 1,000 RPS = system handles 1,000 requests/sec
- **Measurement**: Count successful requests in a 1-second window
- **During load test**:
  ```
  Second 1: 1,000 requests processed ✓ RPS = 1,000
  Second 2: 950 requests processed ⚠ RPS = 950 (degrading under load)
  Second 3: 500 requests processed ❌ RPS = 500 (system saturating)
  ```

### TPS (Transactions Per Second)

- **Definition**: Number of complete business transactions per second
- **Differs from RPS**: One transaction might involve multiple requests
- **Example**: Checkout flow = 5 HTTP requests
  ```
  if RPS = 1,000:
    ├─ One checkout = 5 requests
    └─ TPS = 1,000 ÷ 5 = 200 transactions/sec
  ```
- **When to use**: Business metrics, SLA reporting

### Success Rate & Error Rate

**Success Rate**: Percentage of requests that succeeded (HTTP 2xx, 3xx)

```
Example from load test:
├─ Total requests: 10,000
├─ Successful: 9,950
├─ Failed: 50
├─ Success rate: 99.5%
└─ Error rate: 0.5%

SLA: Must be >99% success
Result: ✓ PASS (99.5% > 99%)
```

**Error Rate**: Percentage that failed (4xx, 5xx, timeouts)

```
Common failure modes:
├─ 4xx errors (4%): Client errors (bad requests)
│  └─ Often: Invalid data, auth failures
├─ 5xx errors (0.5%): Server errors
│  └─ Often: Database down, OOM, unhandled exceptions
└─ Timeouts (0.1%): Request never completed
   └─ Often: Slow database, external service, queue buildup
```

---

## Resource Metrics

While running a load test, monitor your **system resources** on the server being tested. These are usually captured with Datadog.

### CPU Utilization

- **Definition**: Percentage of CPU time being used
- **During load test**:
  ```
  0-20%  : System idle, plenty of headroom
  20-50% : Normal load, healthy
  50-80% : Getting busy, approaching limits
  80-95% : Very busy, risk of slow responses
  95-100%: CPU-bound bottleneck, system saturating
  ```
- **What causes high CPU?**
  - Complex calculations (encryption, compression)
  - Inefficient algorithms
  - Thread contention (locks, synchronized blocks)
  - Garbage collection pauses (Java, Go, Python)

### Memory Usage

- **Definition**: RAM consumed by the application
- **Watch for**:
  ```
  100 requests:  200MB ✓ Normal
  1,000 requests: 250MB ✓ Still reasonable
  10,000 requests: 500MB ✓ Growing as expected
  100,000 requests: 8GB ❌ Memory leak!
  ```
- **Common memory issues**:
  - Memory leaks (objects not released)
  - Growing caches without eviction
  - Connection pool leaks (connections not returned)
  - Message queues filling up

### Disk I/O

- **Definition**: Read/write operations to disk
- **During load test**:
  ```
  Reads: Database queries hitting disk (not in page cache)
  Writes: Log files, database changes, temporary data
  ```
- **Watch for**:
  - High disk I/O → indicates database queries not cached
  - Disk saturation → indicates storage bottleneck
  - Example: SSD writes per second should be <10,000 (AWS limit varies)

### Network I/O

- **Definition**: Bytes sent and received over network
- **During load test**:
  ```
  Inbound: Requests from load tester
  Outbound: Responses to client, external API calls
  ```
- **Common issue**:
  ```
  RPS = 10,000 requests/sec
  Avg response = 10KB
  Outbound = 100,000KB/sec = 100MB/sec
  Network bandwidth = 1Gbps = 125MB/sec
  Headroom = 20% (tight!)
  ```

### Connection Pools

- **Definition**: Active database/service connections
- **During load test**:
  ```
  Pool size: 50 connections (configured max)
  20 load level: 10 connections in use ✓
  100 load level: 45 connections in use ⚠
  200 load level: 50 connections in use + 30 waiting ❌ (queue!)
  ```
- **Problem**: Connection pool exhaustion
  ```
  └─ Connections slow/blocked
     └─ Requests queue up
        └─ More requests arrive
           └─ Queue grows
              └─ Timeouts, cascading failures
  ```

---

## How Metrics Work Together

### Example 1: System Performing Well

```
Load test: 5,000 RPS for 10 minutes

Metrics during test:
├─ p95 latency: 200ms ✓ (target: <300ms)
├─ p99 latency: 400ms ✓ (target: <1000ms)
├─ Success rate: 99.8% ✓ (target: >99%)
├─ CPU: 65% ✓ (healthy, room to grow)
├─ Memory: 1.2GB ✓ (stable, not growing)
├─ Database connections: 30/50 ✓ (headroom)
└─ Disk I/O: Low ✓ (queries cached)

Conclusion: ✅ SYSTEM HEALTHY
Action: Can handle 2-3x more load safely
```

### Example 2: Database Bottleneck

```
Load test: Increasing from 1,000 to 10,000 RPS

Observations:
├─ 1,000 RPS: p95 latency = 50ms, CPU 20%
├─ 2,000 RPS: p95 latency = 75ms, CPU 25%
├─ 5,000 RPS: p95 latency = 300ms, CPU 40%, disk I/O ⬆ ⬆ ⬆
├─ 10,000 RPS: p95 latency = 2000ms, CPU 50%, disk maxed out

Root cause: Disk I/O ceiling
│
└─ Database queries hitting disk (not in cache)
   └─ Each query = disk I/O wait
      └─ As load increases, more queries queue up
         └─ Latency explodes non-linearly

Fix options:
├─ Add caching layer (Redis)
├─ Optimize slow queries (add indexes)
├─ Increase database connection pool
├─ Scale database (read replicas)
└─ Re-test after fix
```

### Example 3: GC Pause Impact

```
Java application under load

JVM GC (Garbage Collection) timeline:
├─ Time 0-45s: Normal operations
│  └─ p95 latency: 100ms, steady
│
├─ Time 45s: Full GC pause (0.5 seconds)
│  └─ All requests block
│     └─ p95 latency spikes to 2000ms+
│     └─ Requests timeout, 0.1% errors
│
├─ Time 45.5s: GC completes
│  └─ Requests resume
│
├─ Time 45.5-50s: High p99 tail
│  └─ Requests queued during GC still processing
│     └─ Takes 2-3 seconds to drain queue
│
└─ Time 50+s: Back to normal

Observation: Every 45 seconds, p95 spikes to 2000ms

Fix options:
├─ Tune JVM heap size (-Xmx, -Xms)
├─ Change GC algorithm (G1GC, ZGC for low latency)
├─ Add more memory to reduce GC frequency
└─ Re-test after tuning
```

---

## Metrics by Load Test Type

### Load Test (Baseline)
Monitor these:
```
├─ p50, p95, p99 latencies ← Should be stable
├─ RPS / TPS ← Should be consistent
├─ Success rate ← Should be >99%
├─ CPU/Memory ← Should be steady
└─ Resource utilization ← Should not be maxing out
```

### Stress Test (Breaking Point)
Monitor:
```
├─ p95, p99, p99.9 latencies ← Will increase
├─ Error rate ← Should increase as you approach limit
├─ RPS plateau ← Where does it max out?
├─ CPU/Memory peaks ← What triggers saturation?
└─ Recovery time ← How long to stabilize?
```

### Soak Test (Long-term Stability)
Monitor over 8-24 hours:
```
├─ Memory trend ← Growing (leak) or stable?
├─ p95 latency trend ← Increasing (degradation) or stable?
├─ Connection count ← Growing (leak) or stable?
├─ GC pause frequency ← Increasing (more pressure)?
└─ Error rate trend ← Any anomalies over time?
```

### Spike Test (Recovery)
Monitor during spike and recovery:
```
├─ Before spike: p95 = 50ms
├─ During spike: p95 = 1000ms (accept 20x increase)
├─ After spike: p95 → 50ms (should recover within 2-3 minutes)
└─ System availability ← Did it stay up?
```

---

## Target Metrics by Service Type

| Service | p95 Target | p99 Target | Success Rate |
|:--------|:-----------|:-----------|:------------|
| **Web App** | <300ms | <1000ms | >99.5% |
| **Mobile API** | <200ms | <500ms | >99.5% |
| **Internal API** | <100ms | <300ms | >99.9% |
| **Real-time** | <50ms | <100ms | >99.9% |
| **Batch/Event** | <5000ms | <30000ms | >99% |
| **Kafka Stream** | <100ms (produce) | <500ms | >99.9% |

---

## Tools for Collecting Metrics

### Gatling Built-in
Metrics are automatically collected:
```
├─ Response times (min, max, percentiles)
├─ Success/error rates
├─ Request counts by endpoint
└─ HTML report with charts
```

### Datadog APM (Recommended for Production)
```
├─ Real-time metrics
├─ Trace-level detail (which database query was slow?)
├─ Custom metrics and annotations
├─ Alert thresholds
└─ Dashboard queries
```

### Application Monitoring
```
├─ New Relic
├─ Dynatrace
├─ Splunk
└─ Your own metrics (StatsD, Prometheus)
```

---

## Next Steps

→ **Read next**: [Load Testing Methodology](03-load-testing-methodology.md) - How to plan and execute tests

