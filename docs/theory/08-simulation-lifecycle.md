# Simulation Lifecycle

## Complete Execution Flow

This document provides a detailed walkthrough of what happens when you run a Gatling simulation from start to finish.

```
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim01_BasicHttp
        │
        ├─→ [PHASE 1: INITIALIZATION]
        ├─→ [PHASE 2: WARM-UP]
        ├─→ [PHASE 3: INJECTION]
        ├─→ [PHASE 4: EXECUTION]
        ├─→ [PHASE 5: COOL-DOWN]
        ├─→ [PHASE 6: REPORTING]
        └─→ [Done: target/gatling/.../ report generated]
```

## Phase-by-Phase Breakdown

### Phase 1: Initialization (< 1 second)
- Load Simulation class
- Parse Protocol, Scenario, setUp()
- Validate syntax and configuration
- Initialize metrics collection system
- Prepare HTTP client infrastructure

### Phase 2: Warm-up (1-5 seconds)
- Establish initial TCP connections
- Perform TLS handshakes
- JVM JIT compiler starts warming up
- Connection pools initialized
- Ready for test traffic

### Phase 3: Injection & Ramp-up
- Users are created according to injection profile
- Each user starts executing scenario independently
- Requests begin being sent
- Metrics start being collected

### Phase 4: Steady State / Execution
- Users continue executing scenarios
- Responses recorded
- Latencies measured
- Errors tracked
- Live console statistics updated

### Phase 5: Cool-down
- Injection stops (no new users created)
- Existing users finish their current scenarios
- In-flight requests complete
- Graceful shutdown

### Phase 6: Reporting
- Aggregate all metrics
- Calculate percentiles (p50, p95, p99)
- Generate HTML report with charts
- Evaluate assertions
- Print summary to console

---

## Per-User Execution Flow

Each virtual user executes the scenario independently:

```
User 1 Timeline:
├─ [0s]   User created, scenario starts
├─ [1s]   GET /posts request sent
├─ [2.5s] Response received, latency = 1500ms
├─ [2.5s] Response validated (check: status = 200)
├─ [2.5s] pause(1) begins
├─ [3.5s] pause complete
├─ [3.5s] GET /posts/1 request sent
├─ [4.2s] Response received, latency = 700ms
├─ [4.2s] Response validated
├─ [4.2s] pause(1) begins
├─ [5.2s] pause complete
├─ [5.2s] GET /comments?postId=1 request sent
├─ [6.0s] Response received, latency = 800ms
├─ [6.0s] Response validated
├─ [6.0s] Scenario complete
└─ [6.0s] User finishes (or loops back to start if configured)

Total: 6 seconds per user, per scenario iteration
```

---

## See Also

- [Gatling Architecture](01-gatling-architecture.md) - Core components
- [Session & Correlation](06-session-and-correlation.md) - Per-user state

---

## Next Steps

→ **Read next**: [HTTP vs Kafka](03-http-vs-kafka.md)

