# Performance Targets & SLAs

## What is an SLA?

An **SLA (Service Level Agreement)** is a commitment about how your system will perform.

```
Example SLA for e-commerce API:

"The system will respond to 99.9% of requests within 500ms p95 latency
during normal business hours (8am-11pm). Monthly uptime must exceed 99.95%."

Components:
├─ Success rate: 99.9% of requests succeed
├─ Latency target: 500ms at p95 percentile
├─ Window: Normal business hours
└─ Uptime: 99.95% monthly availability
```

### Why SLAs Matter

- **For customers**: Clear expectations about service quality
- **For engineers**: Measurable target to design for
- **For ops**: Alerting threshold if SLA breaches
- **For business**: Credibility, compliance, penalties if missed

---

## Industry-Standard Targets

### By Service Type

#### Web Applications (User-Facing)

```
Target SLAs:
├─ p95 latency: <300ms
├─ p99 latency: <1000ms
├─ p99.9 latency: <3000ms
├─ Success rate: >99.5%
└─ Uptime: >99.5% (43 minutes downtime/month acceptable)

Examples:
├─ E-commerce checkout
├─ Social media feed
├─ Email inbox
└─ Video streaming
```

#### Mobile APIs

```
Target SLAs:
├─ p95 latency: <200ms
├─ p99 latency: <500ms
├─ p99.9 latency: <2000ms
├─ Success rate: >99.5%
└─ Uptime: >99.9% (4.3 minutes downtime/month)

Why stricter than web?
├─ Users on cellular (high latency sensitivity)
├─ Battery impact (slow response drains battery)
├─ UX expectations (instant response)
└─ Often critical workflows (payments, health)
```

#### Internal/Backend APIs

```
Target SLAs:
├─ p95 latency: <100ms
├─ p99 latency: <300ms
├─ p99.9 latency: <1000ms
├─ Success rate: >99.9%
└─ Uptime: >99.99% (26 seconds downtime/month)

Why stricter?
├─ Used by other critical services
├─ Cascading failures if slow
├─ Should be faster than public API
├─ Architectural assumption: internal calls are fast
```

#### Real-Time Systems (Trading, Gaming)

```
Target SLAs:
├─ p95 latency: <50ms
├─ p99 latency: <100ms
├─ p99.9 latency: <200ms
├─ Success rate: >99.99%
└─ Uptime: >99.99%

Why ultra-strict?
├─ Milliseconds = real financial impact
├─ Player experience depends on < 100ms latency
├─ Regulatory requirements (trading)
└─ Competitive disadvantage if slower
```

#### Batch/Background Processing

```
Target SLAs:
├─ Latency: <5-30 seconds (entire job)
├─ p99 latency: <30000ms
├─ Success rate: >99% (some retry OK)
└─ Uptime: >99%

Why more relaxed?
├─ User not waiting for response
├─ Can retry later if failed
├─ Throughput matters more than latency
├─ 1-2 minute delay is acceptable
```

#### Kafka/Message Streaming

```
Target SLAs:
├─ Producer latency (send to Kafka): <100ms p95
├─ Consumer lag (behind producer): <1000ms
├─ Success rate: >99.9% (messages not lost)
└─ Uptime: >99.99%

Why different metrics?
├─ Producer latency: How fast can you send messages
├─ Consumer lag: How far behind is processing
├─ Throughput: Messages per second matters most
```

---

## Setting Your Own SLAs

### Step 1: Understand Your Users

```
Question: What do YOUR users tolerate?

Research methods:
├─ Google Analytics: Bounce rate vs page load time
├─ User surveys: "Is 500ms acceptable?"
├─ Competitor analysis: What do others target?
└─ Industry data: Benchmarks for your domain

Google data:
├─ <1 second: Excellent experience
├─ 1-3 seconds: Acceptable
├─ >3 seconds: 40% bounce rate
├─ >5 seconds: 75% bounce rate
```

### Step 2: Understand Your Constraints

```
Question: What can YOUR system realistically achieve?

Assessment:
├─ Current latency (baseline test now)
├─ Current infrastructure limits
├─ Team capability (can we optimize?)
├─ Timeline (when do we need to hit target?)
└─ Budget (can we scale infrastructure?)

Example:
├─ Current p95: 800ms (too high)
├─ Target p95: 300ms (ambitious)
├─ Gap: 500ms improvement needed
├─ Achievable via: Database optimization, caching, scaling
├─ Timeline: 3 months (reasonable for most improvements)
```

### Step 3: Define SLA Components

```
Template:

Service: [Name]

Response Time:
├─ p95: [target]ms (95% of requests faster than this)
├─ p99: [target]ms (99% of requests faster than this)
└─ p99.9: [target]ms (for extreme cases)

Success Rate:
├─ Goal: >[percentage]% success
├─ Acceptable error types: [4xx, 5xx, timeout]
└─ Unacceptable: [data corruption, cascading failures]

Availability/Uptime:
├─ Target: [percentage]% uptime
├─ Calculation: (total_time - downtime) / total_time
├─ Allowable downtime: [X minutes per month]
└─ Excludes: Planned maintenance windows

Conditions:
├─ Time window: [peak hours, business hours, 24/7]
├─ Load assumption: [X concurrent users, Y RPS]
└─ Geography: [specific regions or global]

Example SLA:

Service: User Profile API

Response Time:
├─ p95: 200ms
├─ p99: 500ms
└─ p99.9: 1000ms

Success Rate:
├─ Goal: 99.9%
└─ Errors: <0.1%

Availability:
├─ Target: 99.95% uptime
└─ Allows: 22 minutes downtime/month

Conditions:
├─ Peak hours: 8am-11pm PT
├─ Load: 5000 RPS
└─ Regions: US-East primary, US-West for failover
```

---

## Uptime Percentages Explained

| Percentage | Downtime per Year | Downtime per Month |
|:-----------|:------------------|:-------------------|
| **99%** | 87.6 hours | 7.2 hours |
| **99.5%** | 43.8 hours | 3.6 hours |
| **99.9%** | 8.76 hours | 43 minutes |
| **99.95%** | 4.38 hours | 22 minutes |
| **99.99%** | 52.56 minutes | 4.3 minutes |
| **99.999%** | 5.26 minutes | 26 seconds |

---

## Measurement & Validation

### How to Measure Against SLAs

```
During load test:

1. Run test to expected load for 15-30 minutes
2. Measure all metrics
3. Compare against SLA targets

Example:

SLA Target: p95 <500ms, success >99%
Test Result: p95 = 480ms, success = 99.2%

✅ PASS: Both metrics met
```

### When to Fail SLA

```
SLA Target: p95 <300ms

Test Result: p95 = 350ms
├─ 50ms over target
├─ Is this acceptable?
│  ├─ Marginal failure (close)
│  ├─ Worth investigating: Database slow by 50ms?
│  └─ Decision: Fix or adjust SLA
│
└─ Action: Optimize and re-test

Test Result: p95 = 1000ms
├─ 700ms over target
├─ Unacceptable
├─ Root cause: Database bottleneck (found via Datadog)
└─ Action: Fix bottleneck, re-test, retry SLA validation
```

### Alerting on SLA Breaches

Set up Datadog alerts to notify when SLA breaches:

```yaml
Example Alert:

Name: p95 latency exceeded SLA
Condition: avg:trace.web.request.duration{service:my-api}.percentile(95) > 500

Actions on breach:
├─ Send to Slack: "p95 latency is #{ value | round 0 }}ms (SLA: 500ms)"
├─ PagerDuty: "Page on-call engineer"
└─ Auto-remediation: "Scale service if CPU > 80%"
```

---

## SLA vs Percentile Confusion

Many teams get confused about what their SLA really means.

### ❌ Bad SLA Definition
```
"Response time SLA: 500ms"
├─ Ambiguous: Mean? p95? p99?
├─ Confusing: Does 1% slow responses break SLA?
└─ Problem: Can't measure compliance
```

### ✅ Good SLA Definition
```
"Response time SLA: p95 < 500ms"
├─ Clear: 95th percentile latency
├─ Measurable: Easy to verify in Gatling/Datadog
├─ Acceptable: Allows 5% of requests to be slower
└─ Problem: If p95 = 501ms, SLA fails (strict boundary)
```

### ✅ Better SLA Definition
```
"Response time SLA: p95 < 500ms ± 10ms"
├─ Clear: 95th percentile, 500ms target
├─ Allowable variance: ±10ms (501-509ms is OK)
├─ Practical: Accounts for noise, natural variance
└─ Measurement: 510ms+ is considered breach
```

---

## Dynamic SLAs

Some teams set different SLAs for different scenarios:

```
E-commerce search API:

Normal hours (10am-9pm):
├─ p95: 300ms
├─ p99: 800ms
└─ Success: >99.5%

Peak hours (Black Friday):
├─ p95: 500ms (relaxed, more users)
├─ p99: 1500ms
└─ Success: >99% (0.1% errors acceptable)

Off-peak (midnight-10am):
├─ p95: 200ms (strict, should be fast when not busy)
├─ p99: 500ms
└─ Success: >99.9%

Rationale:
├─ Peak hours: Users tolerate slower responses
├─ Off-peak: System has headroom, should be faster
├─ Normal: Balanced targets
```

---

## Common SLA Mistakes

### ❌ Mistake 1: Mean Latency in SLA

```
Bad: "Average response time < 500ms"
├─ Hides outliers
├─ 1 slow request can pull up average
└─ Misleading metric

Better: "p95 latency < 500ms"
├─ Guarantees 95% of users have acceptable experience
└─ Doesn't hide the 5% who suffer
```

### ❌ Mistake 2: SLA Too Strict

```
Bad: "p99.99 latency < 100ms"
├─ Nearly impossible to achieve
├─ Costs 10x infrastructure
└─ Team burns out trying to hit it

Better: "p99 latency < 500ms"
├─ Achievable with good engineering
├─ Cost-effective
└─ Realistic targets
```

### ❌ Mistake 3: SLA Too Loose

```
Bad: "p95 latency < 10000ms"
├─ Users hate 10-second waits
├─ No incentive to optimize
└─ Business impact: Users leave

Better: "p95 latency < 300ms"
├─ Users happy
├─ Engineering incentive: Optimize
└─ Business aligned
```

### ❌ Mistake 4: Ignoring p99

```
Bad: Monitor only p95, ignore p99
├─ 5% of users experience terrible latency
├─ No visibility into tail behavior
└─ Surprised by outlier complaints

Better: Monitor p95, p99, and p99.9
├─ See full distribution
├─ Catch tail issues early
└─ Comprehensive understanding
```

---

## Testing to Verify SLAs

### Load Test Checklist

```
Before running test:
☐ SLAs defined (p95, p99, success rate)
☐ Test duration planned (15-30 min for baseline)
☐ Load level chosen (expected + 2-3x headroom)
☐ Assertions configured in Gatling
☐ Datadog monitoring enabled
☐ Team notified

Running test:
☐ Smoke test passes (1 user)
☐ System stable during ramp-up
☐ Metrics stable during hold phase
☐ No cascading failures observed
☐ Error rate stays acceptable

After test:
☐ p95 vs SLA target ✓ or ✗
☐ p99 vs SLA target ✓ or ✗
☐ Success rate vs SLA target ✓ or ✗
☐ Uptime: No downtime during test ✓
☐ Root cause analysis if failed
└─ Fix, optimize, re-test
```

---

## Kafka-Specific SLA Targets

| Service Type | p95 Target | p99 Target | p99.9 Target |
|---|---|---|---|
| **User-Facing Web** | <300ms | <1000ms | <3000ms |
| **Mobile App API** | <200ms | <500ms | <2000ms |
| **Internal Service** | <100ms | <300ms | <1000ms |
| **Batch Processing** | <5000ms | <30000ms | N/A |
| **Real-time Analytics** | <1000ms | <5000ms | <30000ms |
| **Kafka Produce** | <100ms | <500ms | — |
| **Kafka Consumer Lag** | — | — | <500ms per msg |

---

## Sample Datadog Monitors

### High p99 Latency Alert

```json
{
  "name": "High p99 Latency Alert",
  "type": "metric alert",
  "query": "avg:trace.web.request.duration{service:my-api,resource_name:/api/users}.percentile(99) > 1000",
  "threshold": 1000,
  "alert_message": "p99 latency exceeded 1 second"
}
```

### Kafka Consumer Lag Alert

```json
{
  "name": "Kafka Consumer Lag Alert",
  "type": "metric alert",
  "query": "avg:kafka.consumer_group.lag{group:my-consumer-group} > 10000",
  "threshold": 10000,
  "alert_message": "Consumer lag exceeded 10k messages"
}
```

---

## Navigation

**← Previous**: [Open Load Patterns](04-open-load-patterns.md)  
**→ Next**: [Common Pitfalls](06-common-pitfalls.md)  
**↑ Up**: [Documentation Index](../index.md)

