# Traces, Operations & Spans

## Overview

Distributed tracing shows the **complete journey** of a single request through your system:

```
User Request
    ↓
API Gateway (5ms)
    ↓
Authentication Service (10ms)
    ↓
Business Logic (100ms)
    ↓
Database Query (400ms) ← BOTTLENECK
    ↓
Response Serialization (50ms)
    ↓
Network (35ms)
────────────────
Total: 600ms
```

---

## What is a Trace?

A **trace** is a complete record of a request's journey through your entire distributed system, from the initial entry point to the final response. Traces show you exactly what happened at every step — not just aggregate metrics.

### Trace Anatomy

```
Incoming Request (HTTP)
    ↓
Entry Service (Trace Starts)
    ├─ Operation 1: Authenticate User
    │   └─ Span: Check cache
    │   └─ Span: Query database
    │
    ├─ Operation 2: Validate Request
    │   └─ Span: Schema validation
    │
    ├─ Operation 3: Call Downstream Service A
    │   └─ Span: Network call to Service A
    │       └─ Nested Operation in Service A: Process Data
    │           └─ Span: Database query
    │           └─ Span: Cache write
    │
    ├─ Operation 4: Call Downstream Service B
    │   └─ Span: Network call to Service B
    │
    └─ Operation 5: Build Response
        └─ Span: Serialize response
    ↓
Response Sent (Trace Ends)
```

---

## Spans vs Operations vs Traces

| Term | Definition | Example |
|------|-----------|---------|
| **Trace** | Complete request journey across all services | `GET /api/orders` start-to-finish |
| **Span** | Individual unit of work (DB query, network call, function) | `db.query: SELECT * FROM orders` |
| **Operation** | Named logical grouping of spans | `POST /api/orders`, `userService.authenticate()` |
| **Service** | The application/microservice doing the work | `order-api`, `user-service`, `payment-service` |

### Visual Example

```
Trace ID: abc123def456
├─ Span 1: request_handler (operation: POST /api/orders)
│   ├─ Span 1.1: auth_check (operation: authenticate_user)
│   │   ├─ Span 1.1.1: redis_get (operation: cache_lookup)
│   │   │   Duration: 2ms  |  Tags: cache_hit=true
│   │
│   ├─ Span 1.2: validate_order (operation: validate_request)
│   │   └─ Span 1.2.1: schema_validate — Duration: 1ms
│   │
│   ├─ Span 1.3: rpc_call_payment_service (operation: process_payment)
│   │   Service: payment-service
│   │   Duration: 150ms
│   │
│   Duration: 156ms total (entire request)
```

---

## What are Operations?

An **operation** is the name assigned to a span describing the work being done. It's how Datadog groups similar work together for filtering and analysis.

### Examples of Operations

```java
span.setOperationName("GET /api/users/{id}")        // HTTP Request
span.setOperationName("db.query.select")             // Database
span.setOperationName("redis.get")                   // Cache
span.setOperationName("payment-service.process")     // RPC/Service Call
span.setOperationName("kafka.send")                  // Message Queue
span.setOperationName("calculate_discount")          // Custom business logic
```

### Setting Operations in Code

```java
public Order processOrder(String orderId) {
    Span span = tracer.spanBuilder("process_order")
        .setAttribute("order.id", orderId)
        .startSpan();

    try {
        Order order = fetchOrder(orderId);

        Span paymentSpan = tracer.spanBuilder("process_payment")
            .setParent(span)
            .setAttribute("payment.amount", order.getTotal())
            .startSpan();
        try {
            processPayment(order);
        } finally {
            paymentSpan.end();
        }
        return order;
    } finally {
        span.end();
    }
}
```

---

## What are Annotations (Tags)?

**Annotations** are markers/tags added to spans to provide context and searchability. They let you filter, search, and group traces in Datadog.

### Standard Tags

```java
span.setTag("http.method", "POST");
span.setTag("http.status_code", 200);
span.setTag("http.url", "/api/orders");
span.setTag("error", true);
span.setTag("error.message", "Timeout");
```

### Custom Business Tags

```java
span.setTag("user.id", "user-123");
span.setTag("order.id", "order-456");
span.setTag("environment", "production");
span.setTag("feature.flag", "new-checkout");
span.setTag("cache.hit", true);
```

### Performance Tags

```java
span.setTag("db.rows_returned", 100);
span.setTag("api.response_size_bytes", 4096);
span.setTag("queue.message_size", 1024);
span.setTag("retry.attempt", 2);
```

### Adding Annotations in a Controller

```java
@PostMapping
public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
    Span span = Span.current();

    span.setAllAttributes(Attributes.builder()
        .put("customer.id", request.getCustomerId())
        .put("order.total_amount", request.getTotal())
        .put("order.item_count", request.getItems().size())
        .build());

    try {
        Order saved = orderService.saveOrder(request);
        span.setAttribute("order.id", saved.getId());
        return ResponseEntity.status(201).body(saved);
    } catch (Exception e) {
        span.setAttribute("error", true);
        span.setAttribute("error.message", e.getMessage());
        throw e;
    }
}
```

---

## Client Operations: What HTTP Requests Show

When Datadog instruments your app, it captures all **outbound HTTP calls** (client-side tracing):

```
Outbound HTTP Call: Service A → Service B

Operation: POST https://payment-service:8080/api/process-payment

Span Details:
├─ Duration: 145ms
├─ Method: POST
├─ Response Status: 200
├─ Request Body Size: 512 bytes
├─ Response Body Size: 1024 bytes
└─ Tags:
    ├─ http.status_code: 200
    ├─ http.method: POST
    ├─ peer.service: payment-service
    ├─ span.kind: client
    └─ error: false
```

### What You Can Extract from Client Traces

**1. Service Dependencies** — see exactly who calls who:
```
order-api   →  payment-service  (all HTTP calls)
            →  user-service
payment-service → bank-gateway  (external APIs)
user-service    → redis cache
```

**2. Latency Breakdown**:
```
Request to order-api: GET /api/orders
├─ Request parsing:           1ms
├─ Authentication:            3ms
├─ Call payment-service:    145ms  ← Slowest part!
│   └─ payment-service:     140ms
├─ Database write:            5ms
└─ Response serialization:    2ms
Total: 156ms
```

**3. Database Query Details**:
```
├─ db.query: SELECT * FROM users WHERE id = ?  — 5ms, rows: 1
├─ db.query: INSERT INTO orders (...)          — 8ms, rows: 1
└─ db.query: UPDATE inventory SET stock = ...  — 3ms, rows: 1
Total database time: 16ms (out of 156ms)
```

**4. Cache Operations**:
```
├─ redis.get: user:123    — 2ms, cache.hit: true  ✓
├─ redis.set: order:456   — 1ms, ttl: 3600
└─ redis.del: user:123:tmp — 1ms
Cache hit rate: 85% ✓
```

**5. Error Propagation**:
```
bank-gateway timeout (500)
  → payment-service returns 503
    → order-api returns 503 to client

Full error chain visible in one trace
```

---

## Tracing Architecture in Datadog

```
Your Application (with APM Agent)
│
├─ Generates Spans (automatic instrumentation)
│  ├─ HTTP requests/responses
│  ├─ Database queries
│  ├─ Cache operations
│  ├─ Kafka sends/receives
│  └─ Custom code (manual)
│
├─ Adds Tags/Annotations
│  ├─ service name, environment
│  ├─ http.status_code, error
│  └─ custom business tags
│
└─ Datadog Agent → Datadog Backend
   └─ Correlates spans by trace ID
   └─ Builds waterfall view
   └─ Calculates latencies
   └─ Detects anomalies
```

### Java APM Setup

```bash
java -javaagent:dd-java-agent.jar \
     -Ddd.service=order-api \
     -Ddd.env=staging \
     -Ddd.version=1.0.0 \
     -Ddd.trace.sample.rate=0.5 \
     -jar application.jar
```

---

## Example: Complete Trace During Load Test

During a Gatling load test at 1000 TPS:

```
Trace ID: load-test-xyz-001
Request: POST /api/transaction (from Gatling at t=0s)

Timeline:
0ms    → Request enters order-api
2ms    → Check cache (redis.get) — cache.hit=true — 2ms
4ms    → Validate transaction — 2ms
6ms    → Call payment-service (POST /payment/authorize) — 140ms SLOW!
         Inside payment-service:
         ├─ Validate payment:    10ms
         ├─ Check fraud service: 60ms  ← Even slower!
         │   └─ fraud-service calls external API: 55ms
         ├─ Database insert:      5ms
         └─ Return
146ms  → Write to Kafka (kafka.produce) — topic: rtdx-salestxn — 5ms
151ms  → Send response — http.status_code=200

Total: 151ms

Insights:
  ✗ fraud-service (60ms) → Scale or add caching
  ✗ payment-service bottleneck → Consider batch processing
  ✓ Kafka write (5ms) → Fast, no action needed
```

---

## How to Query Traces in Datadog

```
# Find all slow requests (p95 > 500ms)
duration:[500ms TO *] service:order-api

# Find failed Kafka operations
error:true resource_name:"kafka.produce"

# Find slow downstream calls
service:payment-service duration:[100ms TO *]

# Find traces for specific load test
tags.load_test:true tags.scenario:kafkaCreateScenario

# Correlate high latency with high CPU
duration:[500ms TO *]  +  metric:system.cpu.user > 80%
```

---

## Reading Datadog Traces

### Timeline View

```
0ms────50ms────100ms────150ms────200ms────250ms────300ms
│
├─ API Gateway ┤ (5ms)
├─ Auth Service        ┤ (10ms)
├─ Business Logic ═══════════════════════════════ (400ms)
│  ├─ SQL Query                    ══════════════ (350ms)
│  └─ Cache Check      ┤ (5ms)
└─ Serialization                                  ┤ (50ms)
```

---

## Identifying Bottlenecks

### Pattern 1: Slow Database Query
```
Trace: 600ms total
├─ DB Query: 400ms ← 67% of total time
└─ Everything else: 200ms
```
**Action**: Add index, add cache, fix N+1

### Pattern 2: Slow Remote Service
```
Trace: 5000ms total
├─ Remote Service Call: 4500ms ← 90%
└─ Everything else: 500ms
```
**Action**: Add timeout, retry, cache, or async

### Pattern 3: Lock Contention
```
├─ SQL Query: 50ms (normal)
├─ Lock Wait: 900ms ← HIGH
└─ Total: 950ms
```
**Action**: Fix concurrent access, add sharding

---

## Common Bottleneck Patterns

| Pattern | Indicator | Action |
|---------|-----------|--------|
| Slow DB | SQL spans >100ms | Add index, cache |
| N+1 Query | Many small SQL spans | Fix query logic |
| Lock Contention | Lock wait spans | Add sharding |
| Slow Remote Call | HTTP span >1000ms | Cache, timeout, async |
| Memory GC | GC spans in traces | Tune JVM |
| Thread Starvation | High queue wait | Increase threads |

---

## Best Practices

```java
// Good: Clear operation name with tags
span.setOperationName("user.lookup.by_id");
span.setTag("user_id", userId);
span.setTag("database.name", "users");

// Bad: Vague name, no tags
span.setOperationName("query");
```

```java
// Always mark errors
if (exception != null) {
    span.setError(true);
    span.setTag("error.type", exception.getClass().getName());
    span.setTag("error.message", exception.getMessage());
}
```

---

## Key Takeaways

1. **Trace** = complete request journey end-to-end
2. **Span** = individual unit of work
3. **Operation** = logical name for what the span does
4. **Annotation/Tag** = searchable context on a span
5. **Client operations** = all outbound HTTP calls your service makes
6. **Waterfall reveals** sequential vs parallel bottlenecks
7. **Use tags** to filter traces from load tests specifically

---

## Navigation

**← Previous**: [Datadog Integration](13-datadog-integration.md)  
**→ Next**: [Dashboard Queries](15-dashboard-queries.md)  
**↑ Up**: [Documentation Index](../index.md)
