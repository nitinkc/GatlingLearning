# Glossary

## Performance Testing Terms

**Load Testing** - Testing system under expected load
**Stress Testing** - Testing beyond expected load to find limits
**Soak Testing** - Testing at normal load for extended time
**Spike Testing** - Testing sudden traffic surge and recovery

## Metrics

**p95 (95th percentile)** - 95% of requests complete within this time
**p99 (99th percentile)** - 99% of requests complete within this time
**TPS** - Transactions per second
**RPS** - Requests per second
**SLA** - Service Level Agreement (performance commitment)

## Gatling Concepts

**Scenario** - Sequence of actions a virtual user performs
**Feeder** - Source of data (CSV file, JSON, random generator)
**Protocol** - Configuration for a test (HTTP, Kafka, etc.)
**Session** - Per-user state, variables
**Assertion** - Pass/fail criteria for test
**Check** - Validation of individual response

## Load Patterns

**Constant Load** - Steady rate of users/requests
**Ramp Load** - Gradually increasing load
**Step Load** - Discrete increases in load levels
**Spike Load** - Sudden increase then decrease

---

## More Terms

**Latency** - Time from request to response
**Throughput** - Requests/transactions per second
**Concurrent Users** - Virtual users active simultaneously
**Open Model** - Unlimited concurrent users, fixed arrival rate
**Closed Model** - Fixed number of users looping
**Think-time** - Pause simulating user reading time
**Correlation** - Extracting data from one response for next request

---

## Next Steps

→ **FAQ**: [FAQ & Troubleshooting](04-faq.md)

