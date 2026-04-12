# HTTP vs Kafka Patterns

## Key Differences

| Aspect | HTTP | Kafka |
|--------|------|-------|
| **Pattern** | Request-Response (sync) | Pub-Sub (async) |
| **Latency** | End-to-end (request+response) | Produce latency + consumer lag |
| **Measurement** | Response time | Time-to-message + processing lag |
| **Success** | HTTP 200 response | Message consumed and processed |
| **Failure** | HTTP error code | Message lost, lag, partition rebalance |

## HTTP Simulations

Synchronous request-response pattern. Client waits for server response.

```
Client (Gatling)          Server
    │                       │
    ├─→ Request ────────→  │
    │                       ├─ Process request
    │  ← Response ←────────┤
    │                       │
    └─ Record latency
       (time between send and receive)
```

**Use case**: REST APIs, microservices, web applications

## Kafka Simulations

Asynchronous pub-sub pattern. Producer sends, consumer processes independently.

```
Producer (Gatling)    Kafka Broker    Consumer
    │                    │                │
    ├─→ Send msg ──→ ────┤                │
    │                    │── stored        │
    │ (Latency: just │                    │
    │  to broker, not│                    │
    │  to consumer)  │                    │
    │                    ├─→ Message ────→ │
    │                    │                 ├─ Process
    │                    │                 └─ Commit offset
    │                    │
    └─ Continue (don't wait for consumer)
```

**Use case**: Event streaming, asynchronous processing, decoupled systems

---

## When to Use HTTP

- ✅ Testing REST APIs
- ✅ Synchronous request-response
- ✅ Microservice-to-microservice calls
- ✅ User-facing APIs (web, mobile)
- ✅ Any request-response interaction

## When to Use Kafka

- ✅ Testing message producers
- ✅ Asynchronous event streaming
- ✅ High-throughput scenarios (thousands of messages/sec)
- ✅ Decoupled systems (producer doesn't care about consumer)
- ✅ Consumer lag analysis
- ✅ Multi-consumer scenarios

---

## Real-World Example

### E-commerce: Order Processing (both patterns)

**HTTP pattern** (immediate feedback):
```
Client browser
  │
  └─→ POST /api/orders ──→ Order Service
                           │
                           ├─ Save to database
                           ├─ Charge payment
                           ├─ Reserve inventory
                           │
                           └─→ 200 OK (order created)
       
       Client gets immediate response
       Can show "Order Confirmed" immediately
```

**Kafka pattern** (async, decoupled):
```
Order Service
  │
  └─→ Send "OrderCreated" event to Kafka ──→ Kafka Broker
                                             │
                                             ├─→ Email Service (processes async)
                                             ├─→ Inventory Service (processes async)
                                             ├─→ Analytics Service (processes async)
                                             └─→ Shipping Service (processes async)
  
  Order Service doesn't wait for all services
  Returns immediately to client
  Services process independently, at own pace
  If one fails, others still work (resilience)
```

---

## Labs Using Each Pattern

### HTTP Labs
- Lab 1: Basic HTTP
- Lab 2: HTTP with Feeders
- Lab 3: Checks & Validation
- Lab 4: Load Profiles
- Lab 5: CRUD Operations

### Kafka Labs
- Lab 6: Kafka Producer
- Lab 7: Kafka with Feeders

---

## Next Steps

→ **Read next**: [Scenarios & Feeders](04-scenarios-and-feeders.md)

