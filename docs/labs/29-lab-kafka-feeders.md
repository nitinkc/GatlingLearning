# Lab 7: Kafka with Feeders

## Learning Objectives

By the end of this lab, you'll understand:

- ✅ How to inject **realistic data** into Kafka events
- ✅ How to use **CSV feeders** with Kafka producers
- ✅ How to simulate **production-like event flows**
- ✅ How to measure **throughput** under realistic conditions
- ✅ How to correlate **event data** across messages

## Real-World Scenario

Lab 6 produced dummy messages. Now we're realistic:

Your e-commerce system produces these events:
```json
{
  "orderId": "ORD-12345",
  "customerId": "CUST-789",
  "productId": "PROD-456",
  "amount": 99.99,
  "timestamp": "2026-04-12T10:30:00Z"
}
```

You need to load test with:
- Real customer IDs (from CSV)
- Real product IDs (from CSV)
- Realistic amounts (from CSV)
- Different event types (Order, Payment, Shipping)

This simulates actual production traffic patterns.

---

## Concept: Data-Driven Kafka

### Static Messages (Lab 6)
```java
// Same message every time
ProducerRecord<String, String> record = new ProducerRecord<>(
    "orders",
    "order-key",
    "{\"orderId\": \"ORD-123\"}"
);
```

### Dynamic Messages (Lab 7)
```java
// Feed data from CSV
.feed(orderFeeder)
.exec(session -> {
    String message = "{\"orderId\": \"" + session.getString("orderId") + "\", " +
                     "\"customerId\": \"" + session.getString("customerId") + "\"}";
    ProducerRecord<String, String> record = new ProducerRecord<>(
        "orders",
        session.getString("orderId"),  // Dynamic key
        message                         // Dynamic value
    );
    producer.send(record);
    return session;
})
```

---

## Code Pattern: Feeder + Kafka

```java
// 1. Define feeders from CSV files
FeederBuilder<String> orderFeeder = csv("data/orders.csv").circular();
FeederBuilder<String> productFeeder = csv("data/products.csv").random();

// 2. Build scenario that uses feeders
ScenarioBuilder kafkaJourney = scenario("Kafka with Feeders")
    .feed(orderFeeder)
    .feed(productFeeder)
    .exec(session -> {
        String message = "{" +
            "\"orderId\": \"" + session.getString("orderId") + "\", " +
            "\"customerId\": \"" + session.getString("customerId") + "\", " +
            "\"productId\": \"" + session.getString("productId") + "\", " +
            "\"amount\": " + session.getString("amount") +
            "}";
        
        ProducerRecord<String, String> record = new ProducerRecord<>(
            "orders",
            session.getString("orderId"),
            message
        );
        
        producer.send(record);
        return session;
    });

// 3. Setup with load profile
setUp(
    kafkaJourney
        .injectOpen(constantUsersPerSec(100).during(60))
)
```

---

## CSV Feeder Strategies

### Circular (Loop Back)
```java
csv("data/orders.csv").circular()
// Data repeats: row 1 → 2 → 3 → 1 → 2 → 3...
// Use when: Data set is comprehensive, repeat is OK
```

### Random (Shuffle)
```java
csv("data/products.csv").random()
// Data randomized: 3 → 1 → 2 → 3 → 1...
// Use when: Want unpredictable order
```

### Sequential (One Pass)
```java
csv("data/events.csv")
// Data consumed once: 1 → 2 → 3 → (end)
// Use when: Limited data, single pass
```

---

## Running the Lab

```bash
cd /Users/sgovinda/Learn/GatlingLearning

# Verify Docker running
docker ps

# Run data-driven Kafka test
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.kafka.Sim07_KafkaWithFeeders
```

### Expected Results

- ✅ CSV feeders loaded
- ✅ 6000+ messages produced (100 users × 60 sec)
- ✅ Dynamic data in each message
- ✅ No errors
- ✅ High throughput (>100 msg/sec)

---

## Key Takeaways

1. **Feeders enable realistic data** in Kafka events
2. **CSV strategy matters** (circular vs random)
3. **Data diversity** makes load test meaningful
4. **Production-like traffic** requires real data patterns
5. **Throughput** is easy to measure with Kafka

---

## Navigation

**← Previous**: [Lab 6: Kafka Producer](06-lab-kafka-producer.md)  
**→ Next**: [Lab 8: Advanced Patterns](08-lab-advanced-patterns.md)  
**↑ Up**: [Lab Overview](00-lab-overview.md)

