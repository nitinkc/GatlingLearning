# Load Test Analysis

## Overview

The real value of load testing is in the **analysis**. Gatling + Datadog together tell a complete story:

- **Gatling**: What load did I create? What did the system respond?
- **Datadog**: What was happening inside the system during that load?

---

## Step 1: Gatling Report Analysis

### Open the HTML Report

```bash
open target/gatling/sim01_basichttp-*/index.html
```

### Key Sections

#### Global Statistics
```
Total Requests:  1,200
Successful:      1,190 (99.2%)
Failed:          10 (0.8%)

Min Latency:     45ms
Mean Latency:    180ms
P50 Latency:     160ms
P95 Latency:     450ms
P99 Latency:     1200ms
Max Latency:     2100ms

Requests/sec:    40 (average)
Peak RPS:        95
```

### Analysis Questions

```
Q1: Success rate OK?
A1: 99.2% is good (target >99%)

Q2: P95 latency acceptable?
A2: 450ms is good (target <500ms)

Q3: Any sudden failures?
A3: Look at timeline - when did failures happen?

Q4: Latency increasing over time?
A4: Check graph - should be stable or improving
```

---

## Step 2: Correlate with Datadog

### During Test Execution

1. Note the **test start time** from Gatling
2. Open Datadog dashboard
3. Set time range to match test duration
4. Watch metrics in real-time

### Example Timeline

```
10:00:00 - Load test starts (Gatling)
10:00:15 - Datadog shows CPU: 20% → 60%
10:00:30 - Datadog shows p95 latency: 100ms → 300ms
10:00:45 - Datadog shows database queries: 50ms → 200ms
10:01:00 - Load test ends

Correlation: As load increases (Gatling), system resources increase (Datadog)
```

---

## Step 3: Identify Bottlenecks

### Method 1: Look at Datadog Service Map

Shows which service is slowest:

```
┌─────────────┐      ┌──────────────┐      ┌──────────────┐
│ API Gateway │ 5ms  │ Auth Service │ 10ms │ Business     │ 100ms
└─────────────┘  ──→ └──────────────┘  ──→ │ Logic        │
                                           └──────────────┘
                                                   │
                                                   ↓
                                           ┌──────────────┐
                                           │ Database     │ 350ms ← BOTTLENECK!
                                           └──────────────┘
```

### Method 2: Look at Slowest Traces

1. Datadog → Traces
2. Filter: `env:staging test:Lab1`
3. Sort by latency (longest first)
4. Click on p99 trace
5. Examine spans

```
Trace Duration: 465ms
├─ API Gateway: 5ms
├─ Auth: 10ms
├─ Business Logic: 50ms
├─ DB Query: 380ms ← SLOW!
└─ Serialization: 20ms
```

---

## Step 4: Root Cause Analysis

### Bottleneck: Database Query

```
Symptom: DB Query taking 380ms
Investigation:
├─ Query type: SELECT * FROM users WHERE id=?
├─ Execution count during load: 1,200
├─ Sequential query plan: Full table scan
└─ Root cause: Missing index on id column

Optimization:
├─ Action: CREATE INDEX idx_users_id ON users(id)
├─ Expected improvement: 380ms → 20ms
└─ Benefit: P95 latency 450ms → 90ms
```

### Bottleneck: Slow External Service

```
Symptom: HTTP call to external API taking 2000ms
Investigation:
├─ Service: Payment gateway
├─ Latency: Stable at 2000ms
├─ No errors, just slow
└─ Root cause: Payment service is slow (not our code)

Options:
├─ Async call: Don't wait for payment response
├─ Cache: Cache payment status
├─ Timeout: Fail fast after 500ms
└─ Different provider: Switch to faster service
```

### Bottleneck: Lock Contention

```
Symptom: P95 latency starts at 200ms, increases to 1000ms
Investigation:
├─ CPU: Stable
├─ Memory: Stable
├─ Database: Shows lock wait times increasing
└─ Root cause: Multiple users updating same row

Solution:
├─ Reduce lock scope: Smaller transactions
├─ Add retries: Optimistic locking
├─ Sharding: Distribute data to reduce contention
```

---

## Step 5: Make Optimization Decisions

### Before Optimization

```
Gatling Report:
├─ P95 Latency: 450ms
├─ P99 Latency: 1200ms
└─ Success Rate: 99.2%

Datadog Analysis:
├─ Database Query: 380ms (82% of latency)
├─ No CPU bottleneck
├─ No memory issues
```

### Decision

```
Root Cause: Database index missing
Action: Add index on frequently searched column
Expected Result: P95 latency 450ms → 100ms
```

### After Optimization

```
Gatling Report (re-run same load test):
├─ P95 Latency: 100ms ✅ (was 450ms, 78% improvement)
├─ P99 Latency: 250ms ✅ (was 1200ms, 79% improvement)
└─ Success Rate: 99.8% ✅ (was 99.2%)

Datadog Analysis:
├─ Database Query: 20ms ✅ (was 380ms)
└─ Total: 95ms ✅ (was 465ms)
```

---

## Common Analysis Scenarios

### Scenario 1: Linear Latency Increase

```
Graph:
Latency ↑
    |     ╱────────
    |   ╱
    | ╱
    └──────→ Load

Interpretation: System scales linearly
Action: Normal, acceptable behavior
```

### Scenario 2: Sudden Spike

```
Graph:
Latency ↑
    |           ╱─────
    |         ╱
    |       ╱
    |     ╱
    └───────→ Load

Interpretation: Threshold found (cache full? connection pool exhausted?)
Action: Investigate at that load level
```

### Scenario 3: Stable Then Crash

```
Graph:
Latency ↑
    |  ────────╱
    |         ╱
    |        ╱
    |       ╱ CRASH
    └────────→ Load

Interpretation: System breaks at specific load
Action: Find and fix the breaking point
```

---

## Documentation Template

After each load test, document:

```markdown
## Load Test: Lab 1 Basic HTTP

### Test Configuration
- Users: 2 new/sec for 15 seconds
- Total requests: 30
- Duration: 15 seconds
- Date: 2026-04-12

### Gatling Results
- Success rate: 100%
- P95 latency: 180ms
- P99 latency: 450ms
- Max latency: 600ms

### Datadog Analysis
- CPU: peaked at 35%
- Memory: peaked at 2.1GB
- Slowest service: API Gateway (avg 150ms)

### Conclusions
- System behaves well under light load
- No bottlenecks identified
- Ready for more aggressive testing

### Next Steps
- Run ramp test to find performance curve
- Run spike test for resilience
```

---

## Key Takeaways

1. **Compare Gatling metrics with Datadog insights**
2. **Identify bottleneck services via traces**
3. **Root cause analysis guides optimization**
4. **Re-test to verify improvements**
5. **Document findings for team knowledge**

---

## Navigation

**← Previous**: [Dashboard Queries](03-dashboard-queries.md)  
**→ Next**: [Advanced Topics](01-custom-feeders.md)  
**↑ Up**: [Documentation Index](../index.md)

