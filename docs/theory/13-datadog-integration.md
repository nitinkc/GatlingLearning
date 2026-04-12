# Datadog Integration

## Overview

Datadog APM (Application Performance Monitoring) allows you to:

- **Monitor** your system during load tests in real-time
- **Correlate** application metrics with load test metrics
- **Identify** bottlenecks at the service level
- **Trace** individual requests end-to-end
- **Alert** when performance degradations occur

---

## Why Monitor Load Tests?

### Without Monitoring
```
Gatling Report: p95 = 5000ms
❓ Why is latency so high?
❓ Is it the database? The API gateway? Network?
❓ Where should I optimize?
```

### With Datadog
```
Gatling Report: p95 = 5000ms
Datadog shows: Database queries taking 4500ms
→ Root cause: N+1 query problem
→ Action: Add query cache
```

---

## Setup

### 1. Install Datadog Agent

```bash
# macOS
brew install datadog-agent

# Start agent
brew services start datadog-agent
```

### 2. Set Datadog API Key

```bash
# Get API key from Datadog dashboard
# Set environment variable
export DATADOG_API_KEY="your-api-key-here"
```

### 3. Enable Traces

```bash
# In Datadog Agent config
# Edit: /opt/datadog-agent/etc/datadog.yaml
apm_enabled: true
apm_port: 8126
```

---

## Instrument Your Application

### For Java Applications

Add dependency to `pom.xml`:

```xml
<dependency>
    <groupId>com.datadoghq</groupId>
    <artifactId>dd-java-agent</artifactId>
    <version>1.20.0</version>
</dependency>
```

### Run with Agent

```bash
java -javaagent:/path/to/dd-java-agent.jar \
     -Ddd.service=my-api \
     -Ddd.env=staging \
     -Ddd.trace.sample.rate=1.0 \
     -jar my-app.jar
```

---

## Tagging Best Practices

Add custom tags to correlate with Gatling tests:

```bash
# Tag your test run
export DD_TAGS="env:staging,test:load-test-001,version:1.0"

# Tag in Gatling
// In your simulation
System.setProperty("dd.tags", "env:staging,test_name:Lab1_BasicHttp");
```

---

## Metrics to Monitor

### System Metrics
- **CPU Usage**: Should increase with load
- **Memory**: Monitor for leaks
- **Disk I/O**: Database bottleneck indicator
- **Network**: Bandwidth saturation

### Application Metrics
- **Requests per second**: RPS
- **Error rate**: Should stay <1%
- **P95 latency**: Response time tail latency
- **Apdex**: User satisfaction score

### Database Metrics
- **Query latency**: Breakdown by query
- **Connection pool**: Exhaustion indicator
- **Slow queries**: >1000ms queries
- **Lock contention**: Concurrent access issues

---

## Creating Dashboards

### Custom Dashboard Example

```
1. Database Performance
   ├─ Avg Query Time (by query type)
   ├─ Query Count (by table)
   ├─ Connection Pool Usage
   └─ Slow Query Alert

2. API Performance
   ├─ Requests per Second
   ├─ P95 Latency (by endpoint)
   ├─ Error Rate
   └─ Status Code Distribution

3. System Resources
   ├─ CPU %
   ├─ Memory %
   ├─ Disk I/O
   └─ Network Bandwidth
```

---

## Key Metrics During Load Test

| Metric | Baseline | Under Load | Action |
|--------|----------|-----------|--------|
| p95 latency | 100ms | <500ms | Good |
| p95 latency | 100ms | >2000ms | Investigate |
| Error rate | <0.1% | <1% | Acceptable |
| Error rate | <0.1% | >5% | Critical |
| CPU | 20% | <80% | Good |
| CPU | 20% | >95% | Bottleneck |
| Memory | 2GB | <6GB | Good |
| Memory | 2GB | >8GB | Leak? |

---

## Key Takeaways

1. **Datadog provides context** for Gatling metrics
2. **Tagging** links tests to metrics
3. **Dashboards** enable quick analysis
4. **Alerting** catches regressions
5. **Traces** reveal bottlenecks

---

## Navigation

**← Previous**: [Lab 8: Advanced Patterns](../labs/30-lab-advanced-patterns.md)  
**→ Next**: [Traces, Operations & Spans](14-traces-operations-spans.md)  
**↑ Up**: [Documentation Index](../index.md)

