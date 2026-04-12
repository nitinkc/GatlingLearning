# Performance Testing & Gatling Load Testing

Welcome to a **comprehensive, hands-on guide to performance testing** and the **Gatling load testing framework**. This documentation covers everything from foundational concepts to production-grade testing strategies.

## 🎯 What You'll Learn

### For Beginners
- **What is performance testing?** Why does it matter for your systems?
- **Key metrics**: p95, p99, latency, throughput, and what they mean
- **Load testing methodology**: How to properly design and execute tests
- **Gatling basics**: From your first HTTP request to Kafka event streams

### For Practitioners
- **Advanced load patterns**: Constant, ramp, step, and spike testing strategies
- **Scenario design**: Building realistic user journeys with feeders and data correlation
- **Production monitoring**: Datadog integration, trace analysis, and bottleneck identification
- **Optimization**: Custom feeders, distributed testing, and performance tuning

---

## 📚 Learning Paths

### Path 1: Rapid Start (2-3 hours)
```
1. What is Performance Testing? (15 min)
   ↓
2. Key Metrics Explained (20 min)
   ↓
3. Lab 1: Basic HTTP (20 min)
   ↓
4. Lab 2: HTTP Feeders (20 min)
   ↓
5. Lab 3: Validation (20 min)
   ↓
6. Lab 4: Load Profiles (30 min)
```

**Outcome**: Understand core Gatling concepts and run your first simulations.

---

### Path 2: Complete Mastery (6-8 hours)
```
Foundations (2 hours)
├── What is Performance Testing?
├── Key Metrics & Measurements
├── Load Testing Methodology
├── Open Load Patterns
├── Performance Targets & SLAs
└── Common Pitfalls

Gatling Concepts (1.5 hours)
├── Gatling Architecture
├── Simulation Lifecycle
├── HTTP vs Kafka Patterns
├── Scenarios & Feeders
├── Checks & Assertions
└── Session & Correlation

Practical Labs (2.5 hours)
├── Lab 1-5: HTTP & CRUD operations
├── Lab 6-7: Kafka producer & feeders
└── Lab 8: Advanced patterns

Monitoring (1-2 hours)
├── Datadog Integration
├── Traces & Operations
├── Dashboard Queries
└── Load Test Analysis
```

**Outcome**: Deep expertise in performance testing, Gatling patterns, and production monitoring.

---

### Path 3: Enterprise Setup (Custom)
```
For teams deploying load testing in production:
├── Distributed Testing Architecture
├── CI/CD Integration
├── Custom Feeders for Business Logic
├── Advanced Performance Optimization
└── Multi-service Load Test Coordination
```

---

## 🚀 Quick Start

### 1. Prerequisites
```bash
# Check Java and Maven versions
java -version      # Java 21+
mvn -version       # Maven 3.9+
docker -v          # For Kafka labs (optional)
```

### 2. Clone & Navigate
```bash
cd /Users/sgovinda/Learn/GatlingLearning
```

### 3. Run Your First Simulation
```bash
# HTTP simulation (no Docker needed)
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim01_BasicHttp

# View the report
open target/gatling/*/index.html
```

### 4. Build the Documentation
```bash
pip3 install -r requirements.txt
python3 -m mkdocs serve
# Visit http://127.0.0.1:8000
```

---

## 📖 Documentation Structure

### **Foundations** (1-2 hours)
Understand the "why" and "what" of performance testing before writing code.

- [What is Performance Testing?](theory/01-what-is-performance-testing.md) — Types, why it matters
- [Key Metrics & Measurements](theory/02-key-metrics.md) — Latency, throughput, resources
- [Load Testing Methodology](theory/03-load-testing-methodology.md) — Best practices, workflow
- [Open Load Patterns](theory/04-open-load-patterns.md) — Constant, ramp, step, spike
- [Performance Targets & SLAs](theory/05-performance-targets-slas.md) — Industry benchmarks
- [Common Pitfalls](theory/06-common-pitfalls.md) — What to avoid

### **Gatling Concepts** (1.5-2 hours)
Learn how Gatling works under the hood.

- [Gatling Architecture](theory/07-gatling-architecture.md) — Core components
- [Simulation Lifecycle](theory/08-simulation-lifecycle.md) — Execution flow
- [HTTP vs Kafka Patterns](theory/09-http-vs-kafka.md) — When to use each
- [Scenarios & Feeders](theory/10-scenarios-and-feeders.md) — Data injection
- [Checks & Assertions](theory/11-checks-and-assertions.md) — Validation
- [Session & Correlation](theory/12-session-and-correlation.md) — State management

### **Practical Labs** (3-4 hours)
Build real skills with 8 hands-on labs, progressing from basics to advanced patterns.

- [Lab Overview](labs/22-lab-overview.md) — How to work through labs
- [Lab 1: Basic HTTP](labs/23-lab-basic-http.md) — GET requests, pauses, constant load
- [Lab 2: HTTP Feeders](labs/24-lab-http-feeders.md) — CSV/JSON data injection
- [Lab 3: Checks & Validation](labs/25-lab-checks-validation.md) — Response validation, correlation
- [Lab 4: Load Profiles](labs/26-lab-load-profiles.md) — Smoke, ramp, step, spike tests
- [Lab 5: CRUD Operations](labs/27-lab-crud-operations.md) — PUT, PATCH, DELETE
- [Lab 6: Kafka Producer](labs/28-lab-kafka-producer.md) — Event streaming basics
- [Lab 7: Kafka with Feeders](labs/29-lab-kafka-feeders.md) — Data-driven Kafka tests
- [Lab 8: Advanced Patterns](labs/30-lab-advanced-patterns.md) — Async, throttling, complex scenarios

### **Monitoring & Analysis** (1-2 hours)
Understand how to monitor and analyze load tests with Datadog.

- [Datadog Integration](theory/13-datadog-integration.md) — APM setup, tags
- [Traces, Operations & Spans](theory/14-traces-operations-spans.md) — Understanding traces
- [Dashboard Queries](theory/15-dashboard-queries.md) — Writing queries
- [Load Test Analysis](theory/16-load-test-analysis.md) — Reading results

### **Advanced Topics** (Optional)
For production deployments and complex scenarios.

- [Custom Feeders](theory/17-custom-feeders.md) — Building data generators
- [Advanced Patterns](theory/18-advanced-patterns.md) — Complex scenarios
- [Optimization Tips](theory/19-optimization-tips.md) — JVM tuning, performance
- [Distributed Testing](theory/20-distributed-testing.md) — Multi-machine setups

### **Reference** (Lookup)
Quick reference guides and troubleshooting.

- [Quick Reference](theory/21-quick-reference.md) — Cheat sheet, common patterns
- [Glossary](theory/22-glossary.md) — Terminology explained
- [Run Commands](theory/23-run-commands.md) — All Maven commands
- [FAQ & Troubleshooting](theory/24-faq.md) — Solving common issues

---

## 🎓 How to Use This Guide

### For Self-Paced Learning
1. Start with **Foundations** — read top-to-bottom
2. Move to **Gatling Concepts** — understand the framework
3. Work through **Labs 1-8** in order, running each simulation
4. Explore **Monitoring & Analysis** as you scale tests
5. Reference **Advanced Topics** and **Reference** as needed

### For Structured Training
- **Session 1** (2h): Foundations + Gatling Architecture
- **Session 2** (1.5h): Labs 1-4 (HTTP)
- **Session 3** (1.5h): Labs 5-7 (CRUD, Kafka)
- **Session 4** (1h): Monitoring, Lab 8, Q&A

### For Teams
- Share the [Quick Reference](theory/21-quick-reference.md) as a team cheat sheet
- Use [Run Commands](theory/23-run-commands.md) for CI/CD setup
- Reference [Glossary](theory/22-glossary.md) for terminology alignment
- Build custom labs based on [Advanced Patterns](theory/18-advanced-patterns.md)

---

## 🛠️ Project Structure

This documentation guides you through the **GatlingLearning** project:

```
GatlingLearning/
├── src/test/java/io/learn/gatling/simulations/
│   ├── http/
│   │   ├── Sim01_BasicHttp.java ..................... Lab 1
│   │   ├── Sim02_HttpWithFeeders.java .............. Lab 2
│   │   ├── Sim03_HttpChecks.java ................... Lab 3
│   │   ├── Sim04_LoadProfiles.java ................. Lab 4
│   │   └── Sim05_CRUD.java ......................... Lab 5
│   └── kafka/
│       ├── Sim06_BasicKafkaProducer.java .......... Lab 6
│       └── Sim07_KafkaWithFeeders.java ............ Lab 7
├── src/test/resources/
│   ├── data/
│   │   ├── users.csv, products.csv, stores.csv
│   │   └── posts.json
│   └── bodies/
│       ├── update_post.json, product_event.json
│       └── inventory_event.json
└── docs/ ........................................ This documentation
```

---

## 📊 What You'll Achieve

By completing this learning path, you'll be able to:

✅ **Design** realistic load test scenarios for HTTP and Kafka  
✅ **Understand** key metrics and what they mean for your system  
✅ **Run** simulations with constant, ramp, step, and spike load patterns  
✅ **Validate** responses and extract data for correlation  
✅ **Monitor** tests with Datadog and identify bottlenecks  
✅ **Optimize** system performance based on load test results  
✅ **Build** custom feeders and advanced patterns  
✅ **Deploy** load tests in production with CI/CD integration  

---

## 💡 Key Concepts Preview

### Performance Testing Types
- **Load Testing**: Measure performance under expected load
- **Stress Testing**: Find the breaking point
- **Soak Testing**: Check long-term stability
- **Spike Testing**: Validate recovery from sudden spikes

### Load Patterns You'll Master
- **Constant Load**: Steady traffic (baseline testing)
- **Ramp Load**: Gradual increase to find limits
- **Step Load**: Staircase increases for threshold analysis
- **Spike Load**: Sudden burst for resilience testing

### Gatling Patterns You'll Use
- **HTTP Simulations**: Request-response testing
- **Kafka Simulations**: Event-driven testing
- **Feeders**: Data injection for realistic scenarios
- **Checks**: Response validation and correlation
- **Assertions**: SLA pass/fail criteria

---

## 🤝 Contributing & Feedback

This documentation is a living guide. As you work through labs:

- **Found an issue?** Note it in [FAQ & Troubleshooting](theory/24-faq.md)
- **Have a tip?** Suggest additions to [Quick Reference](theory/21-quick-reference.md)
- **Want to improve?** Every section has a "Further Reading" link

---

## 📖 Additional Resources

### Official Documentation
- [Gatling Official Docs](https://gatling.io/docs/gatling/reference/current/)
- [Gatling HTTP Protocol](https://gatling.io/docs/gatling/reference/current/http/protocol/)
- [Gatling Injection Models](https://gatling.io/docs/gatling/reference/current/core/injection/)

### Related Technologies
- [Datadog APM Documentation](https://docs.datadoghq.com/tracing/)
- [Testcontainers Kafka](https://java.testcontainers.org/modules/kafka/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)

### Community
- [Gatling Discussion Forum](https://gatling.io/community/)
- [Gatling GitHub](https://github.com/gatling/gatling)

---

## 🎯 Next Steps

1. **Start here**: [What is Performance Testing?](theory/01-what-is-performance-testing.md)
2. **Then read**: [Key Metrics & Measurements](theory/02-key-metrics.md)
3. **Build first skill**: [Lab 1: Basic HTTP](labs/23-lab-basic-http.md)
4. **Progress systematically** through all labs

---

**Happy learning! 🚀**
