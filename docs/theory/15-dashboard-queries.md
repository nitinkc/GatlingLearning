# Dashboard Queries

## Overview

Datadog Query Language (DQL) lets you write custom queries to:

- **Filter** metrics by tags
- **Aggregate** data over time
- **Compare** baseline vs load test
- **Alert** on anomalies
- **Build** custom dashboards

---

## Query Basics

### Basic Syntax

```
metric_name{tag1:value1, tag2:value2} | aggregation_function
```

### Example Queries

#### 1. Average Response Time by Endpoint

```
avg:trace.http.request.duration{env:staging}
  by {resource_name}
```

Result:
```
GET /users: 250ms
GET /users/:id: 350ms
POST /users: 400ms
```

#### 2. Error Rate Over Time

```
100 * (
  sum:trace.http.errors{env:staging} 
  / 
  sum:trace.http.requests{env:staging}
)
```

Result:
```
t=0min: 0.05%
t=5min: 0.08%
t=10min: 0.12% ← increasing
```

#### 3. P95 Latency by Service

```
p95:trace.http.request.duration{env:staging}
  by {service}
```

Result:
```
api-service: 450ms
db-service: 800ms
cache-service: 50ms
```

#### 4. Database Query Latency Distribution

```
avg:trace.sql.query.duration{env:staging}
  by {database.statement}
```

Result:
```
SELECT * FROM users: 25ms
SELECT * FROM orders: 150ms
UPDATE inventory SET: 200ms ← SLOW
```

#### 5. Request Count During Load Test

```
sum:trace.requests{test:Lab1}
```

Result shows total requests produced by test

---

## Common Dashboard Queries for Load Tests

### System-Level Metrics

```
# CPU Usage
avg:system.cpu.user{env:staging}

# Memory Usage
avg:system.mem.pct_usable{env:staging}

# Disk I/O
avg:system.io.reads{env:staging}
avg:system.io.writes{env:staging}
```

### Application-Level Metrics

```
# Request Rate
sum:trace.requests{env:staging}

# Error Rate
sum:trace.errors{env:staging} / sum:trace.requests{env:staging}

# Response Time
p50:trace.http.request.duration{env:staging}
p95:trace.http.request.duration{env:staging}
p99:trace.http.request.duration{env:staging}
```

### Database Metrics

```
# Query Rate
sum:trace.sql.queries{env:staging}

# Slow Queries
sum:trace.sql.queries{env:staging, duration:>1000ms}

# Connection Pool Usage
avg:db.pool.usage{env:staging}
```

---

## Filtering Techniques

### By Environment

```
metric_name{env:staging}
metric_name{env:production}
```

### By Test Name

```
metric_name{test:Lab1_BasicHttp}
metric_name{test:Lab4_LoadProfile}
```

### By Service

```
metric_name{service:api}
metric_name{service:database}
metric_name{service:cache}
```

### By Endpoint

```
metric_name{resource_name:GET /users}
metric_name{resource_name:POST /orders}
```

### Combined Filters

```
metric_name{env:staging, test:Lab1, service:api}
```

---

## Comparison Queries

### Compare Baseline vs Load Test

```
# During load test
avg:trace.http.request.duration{test:Lab1}  # = 250ms

# Baseline (no load)
avg:trace.http.request.duration{env:staging, test:baseline}  # = 150ms

# Difference = 100ms (67% increase)
```

### Detect Regressions

```
# Query that returns >1000ms = regression
p95:trace.http.request.duration{service:api} > 1000
```

---

## Building Custom Dashboards

### Step 1: Create Dashboard

```
Datadog → Dashboards → New Dashboard
```

### Step 2: Add Widgets

```
Widget Type: Timeseries
Metric: p95:trace.http.request.duration{env:staging}
Title: "API P95 Latency During Load Test"
```

### Step 3: Add Multiple Queries

```
Widget: "System Health"
├─ CPU: avg:system.cpu.user{env:staging}
├─ Memory: avg:system.mem.pct_usable{env:staging}
├─ Disk I/O: sum:system.io{env:staging}
└─ Network: avg:system.net.bytes{env:staging}
```

### Step 4: Tag Dashboard

```
Tags: "load-testing", "Lab1", "staging"
```

---

## Troubleshooting Queries

### Query Returns No Data

```
❌ metric_name{env:staging}
✅ Check if metric exists: avg:metric_name
✅ Check tags: metric_name{env:*}
```

### Query Returns Wrong Data

```
❌ Wrong filter: metric_name{test:Lab1}
✅ Verify exact tag value: metric_name{test_name:Lab1_BasicHttp}
```

### Query Syntax Error

```
❌ metric_name{tag1:value1, tag2:value2}  (missing space)
✅ metric_name{tag1:value1,tag2:value2}
```

---

## Key Takeaways

1. **Queries filter and aggregate metrics**
2. **Tags enable precise filtering**
3. **Baselines enable comparison**
4. **Dashboards consolidate multiple queries**
5. **Regular expressions enable pattern matching**

---

## Navigation

**← Previous**: [Traces, Operations & Spans](02-traces-operations-spans.md)  
**→ Next**: [Load Test Analysis](04-load-test-analysis.md)  
**↑ Up**: [Documentation Index](../index.md)

