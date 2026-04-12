# Distributed Testing

## Overview

When a single machine can't generate enough load, distribute testing across multiple machines.

### Single Machine Limitation
```
1 machine: 10,000 concurrent users max
Need: 50,000 concurrent users
Solution: Distribute across 5 machines
```

---

## Architecture

```
┌─────────────────────────────────────────┐
│     Gatling Enterprise Controller       │
│  (Coordinates and aggregates results)   │
└─────────────────────────────────────────┘
         ↙          ↓          ↖
    ┌────────┐ ┌────────┐ ┌────────┐
    │Agent 1 │ │Agent 2 │ │Agent 3 │
    │3,000   │ │3,000   │ │3,000   │
    │users   │ │users   │ │users   │
    │10Gbps  │ │10Gbps  │ │10Gbps  │
    └────────┘ └────────┘ └────────┘
        ↓          ↓          ↓
    ┌──────────────────────────────┐
    │   Target System Under Test   │
    │   (Receives 30,000 users)    │
    └──────────────────────────────┘
```

---

## Setup Options

### Option 1: Gatling Enterprise (Recommended)

```
✓ Managed platform
✓ Automatic agent coordination
✓ Built-in result aggregation
✓ Visual reporting
✓ Compliance features
✗ Requires subscription
```

### Option 2: Open Source Distributed Setup

```
✓ Free
✓ Full control
✗ Manual coordination
✗ Manual result aggregation
✗ More operational overhead
```

---

## Open Source Setup

### Step 1: Prepare Agents

On each agent machine:
```bash
# Machine 1, 2, 3 (Ubuntu servers)
curl -X PUT -d -u admin:admin http://localhost:8080/gatling/data/simulation \
     -H "Content-Type: application/json" \
     -d @simulation.json
```

### Step 2: Configure Simulation

```java
// Reference simulation on agents
// Each agent runs same simulation with different user offset

public class Sim_DistributedLoad extends Simulation {
    
    // Get agent ID (0, 1, 2, etc.)
    String agentId = System.getProperty("gatling.agentId", "0");
    
    // Distribute users across agents
    // Agent 0: users 0-9999
    // Agent 1: users 10000-19999
    // Agent 2: users 20000-29999
    
    int userOffset = Integer.parseInt(agentId) * 10000;
    
    ScenarioBuilder scenario = scenario("Distributed Load")
        .feed(userFeeder.offset(userOffset))
        .exec(http("Request").get("/api"));
}
```

### Step 3: Run on Each Agent

```bash
# Agent 1
mvn gatling:test \
  -Dgatling.simulationClass=Sim_DistributedLoad \
  -Dgatling.agentId=0

# Agent 2
mvn gatling:test \
  -Dgatling.simulationClass=Sim_DistributedLoad \
  -Dgatling.agentId=1

# Agent 3
mvn gatling:test \
  -Dgatling.simulationClass=Sim_DistributedLoad \
  -Dgatling.agentId=2
```

### Step 4: Aggregate Results

```bash
# Collect results from each agent
# Manually merge CSV files:

cat agent1/results.csv agent2/results.csv agent3/results.csv > combined.csv

# Calculate aggregated metrics
# Total requests = sum of all agents
# P95 latency = p95 of combined data
```

---

## Synchronization Challenges

### Problem 1: Agents Start at Different Times

```
Agent 1: starts at 10:00:00
Agent 2: starts at 10:00:05  ← 5 second delay
Agent 3: starts at 10:00:10  ← 10 second delay

Result: Load is staggered, not simultaneous
```

### Solution: Synchronized Start

```java
// Use barrier to wait for all agents
Barrier barrier = new Barrier(3);  // 3 agents

// All agents wait at barrier
barrier.await();  // Blocks until all 3 reach this point

// Then start simultaneously
setUp(scenario.injectOpen(...))
```

---

## Data Collection & Aggregation

### Per-Agent Results

```
Agent 1: 10,000 requests, p95=450ms, errors=2
Agent 2: 10,000 requests, p95=480ms, errors=3
Agent 3: 10,000 requests, p95=520ms, errors=1
```

### Aggregated Results

```
Total: 30,000 requests
P95: ((450*10000 + 480*10000 + 520*10000) / 30000) = 483ms
Errors: 2 + 3 + 1 = 6 (0.02% error rate)
```

---

## Network Bandwidth Considerations

### Bandwidth Required

```
Per user: ~1MB data per second
Per machine (1000 concurrent users): ~1Gbps
Per machine (10,000 concurrent users): ~10Gbps

3 machines with 10,000 users each:
├─ Each machine: 10Gbps
├─ Total to target: 30Gbps
└─ Network: Must have ≥30Gbps capacity
```

### Network Planning

```
Datacenter network: Typically 10Gbps per server
3 servers: 30Gbps total available
3 servers hitting target: 30Gbps required
Result: Perfect fit (but no headroom)

Better: Use 5 machines with 6,000 users each
├─ Per machine: 6Gbps
├─ Total: 30Gbps (same)
└─ Headroom: Yes, less contention
```

---

## Best Practices

### 1. Network Isolation

```
Agents and target on same network
└─ Minimize latency

Avoid routing through internet
└─ Variable latency ruins test
```

### 2. Time Synchronization

```
# All machines must have synchronized clocks
ntpdate -u ntp.ubuntu.com  # Sync to NTP

# Verify
timedatectl  # Check clock is synchronized
```

### 3. Resource Sizing

```
Per agent machine:
├─ CPU: 16 cores (for 10,000 users)
├─ RAM: 32GB (for 10,000 users)
├─ Network: 10Gbps+ NIC
└─ Storage: Fast SSD for logging
```

### 4. Monitoring Agents

```
Monitor each agent during test:
├─ CPU: Should not exceed 80%
├─ Memory: Should not exceed 80%
├─ Network: Should not exceed 90%

If exceeded: Add more agents, reduce per-agent users
```

---

## Troubleshooting

### Issue: Uneven Load Distribution

```
Agent 1: 10,000 requests
Agent 2: 8,000 requests
Agent 3: 9,000 requests

Problem: Agents started at different times
Solution: Add synchronization barrier
```

### Issue: Agent Runs Out of Memory

```
Error: OutOfMemoryError
Solution: Reduce users per agent or increase JVM heap
```

### Issue: Network Bandwidth Maxed

```
Observation: Network at 100%, latency high
Solution: Add more agents with fewer users each
```

---

## Gatling Enterprise Alternative

For production-grade distributed testing:

```
Pros:
✓ Automatic scaling (0-100,000+ users)
✓ Managed cloud infrastructure
✓ Built-in reporting
✓ Real-time dashboards
✓ Compliance features

Cons:
✗ Cost ($$$)
✗ Less control
✗ Vendor lock-in

Use when: Load >50,000 users, team size >5, budget available
```

---

## Key Takeaways

1. **Distributed testing** = Multiple machines generating load
2. **Coordination** = Synchronize start, aggregate results
3. **Network bandwidth** = Plan for 10-30Gbps
4. **Agent sizing** = 10,000 users per 16-core machine
5. **Monitoring** = Watch CPU, memory, network on each agent
6. **Gatling Enterprise** = Simplified alternative for large tests

---

## Navigation

**← Previous**: [Optimization Tips](03-optimization-tips.md)  
**→ Next**: [Quick Reference]](01-quick-reference.md)  
**↑ Up**: [Documentation Index](../index.md)

