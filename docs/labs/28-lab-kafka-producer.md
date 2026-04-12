# Lab 6: Kafka Producer

## Learning Objectives

By the end of this lab, you'll understand:

- ✅ How to load test **event streaming** systems
- ✅ How to produce messages to **Kafka topics**
- ✅ How to use **Testcontainers** for Kafka setup
- ✅ How to measure **message production performance**
- ✅ How Kafka differs from HTTP request-response

## Real-World Scenario

Your company is moving to event-driven architecture. Instead of HTTP APIs, services communicate via Kafka:

- E-commerce service publishes `OrderCreated` events
- Inventory service subscribes and updates stock
- Analytics service subscribes and tracks metrics

You need to load test: **"Can Kafka handle 10,000 orders/second?"**

Kafka is different from HTTP:
- No request-response cycle
- Publish-and-forget (asynchronous)
- Topics and partitions for scalability

---

## Concept: Kafka vs HTTP

### HTTP Load Test
```
User 1: GET /api/products → [wait] → Response 200 ✓
User 2: GET /api/products → [wait] → Response 200 ✓
```

Synchronous, user waits for response.

### Kafka Load Test
```
User 1: Produce OrderCreated event → [return immediately]
User 2: Produce OrderCreated event → [return immediately]
Kafka: [asynchronously processes events]
```

Asynchronous, producer doesn't wait.

---

## Code Pattern: Kafka Producer

```java
// 1. Start Kafka with Testcontainers
KafkaContainer kafka = new KafkaContainer(...)
    .withEmbeddedZookeeper();
kafka.start();

// 2. Create producer in scenario
.exec(session -> {
    KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProperties);
    ProducerRecord<String, String> record = new ProducerRecord<>(
        "orders",
        "order-" + session.getUserId(),
        "{\"orderId\": \"123\", \"amount\": 99.99}"
    );
    producer.send(record);
    producer.close();
    return session;
})
```

---

## Prerequisites: Docker

Kafka labs require Docker:

```bash
# Start Docker
open -a Docker  # macOS

# Verify Docker is running
docker ps

# If not running: Install from https://www.docker.com/products/docker-desktop
```

---

## Running the Lab

```bash
cd /Users/sgovinda/Learn/GatlingLearning

# Ensure Docker is running
docker ps

# Run Kafka producer test
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.kafka.Sim06_BasicKafkaProducer
```

### Expected Results

- ✅ Kafka broker starts (Testcontainers)
- ✅ 1000+ messages produced
- ✅ No producer errors
- ✅ Latency <100ms per message (typically faster)
- ✅ Test completes without errors

---

## Troubleshooting

### Issue: "Docker daemon not accessible"
```
Error: Could not connect to Docker daemon
```

**Fix**:
1. Start Docker: `open -a Docker` (macOS)
2. Wait 10 seconds for Docker to fully start
3. Verify: `docker ps`
4. Retry test

### Issue: "Port 9092 already in use"
```
Error: Bind exception on port 9092
```

**Fix**:
1. Check if Kafka already running: `docker ps`
2. Stop container: `docker stop <container-id>`
3. Retry test

### Issue: "Testcontainers pull timeout"
```
Error: Timed out waiting for image pull
```

**Fix**:
1. Docker image is large (~1GB)
2. First run will be slow (5-10 minutes)
3. Subsequent runs use cached image (fast)
4. Ensure stable internet connection

---

## Key Takeaways

1. **Kafka is asynchronous** - no request-response
2. **Testcontainers** simplifies Kafka setup
3. **Message production** is much faster than HTTP
4. **No latency waiting** for responses
5. **Topics** organize messages by type
6. **Scaling** requires understanding partitions

---

## Navigation

**← Previous**: [Lab 5: CRUD Operations](05-lab-crud-operations.md)  
**→ Next**: [Lab 7: Kafka with Feeders](07-lab-kafka-feeders.md)  
**↑ Up**: [Lab Overview](00-lab-overview.md)

