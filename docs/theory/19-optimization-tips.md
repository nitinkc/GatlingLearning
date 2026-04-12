# Optimization Tips

## Overview

After identifying bottlenecks via load testing, apply these optimizations:

- **Application optimizations** - Code-level improvements
- **Database optimizations** - Query and indexing improvements
- **JVM tuning** - Java runtime configuration
- **Infrastructure scaling** - Hardware and cloud resources

---

## Application Optimizations

### 1. Caching

#### Problem
```
Request → Database Query (400ms) → Response
Every request queries database
```

#### Solution
```
Request → Check Cache (5ms)
            ├─ Hit → Return (5ms total) ✓
            └─ Miss → Query (400ms) → Cache → Return
```

#### Code
```java
@Cacheable("users")
public User getUser(String userId) {
    return userRepository.findById(userId);
}
```

#### Impact
- Cache hits: 5ms (was 400ms)
- Cache misses: 405ms (400ms query + 5ms cache)
- With 80% hit rate: Average 85ms (was 400ms)
- **Improvement: 78%**

### 2. Connection Pooling

#### Problem
```
User 1: Get connection from pool (300ms - slow)
User 2: Get connection from pool (300ms - slow)
...
Users 100+: Pool exhausted, queue buildup
```

#### Solution
```
// Configure pool size and reuse
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

#### Impact
- Before: Connection acquisition 300ms
- After: Connection reuse 1ms
- **Improvement: 99%**

### 3. Lazy Loading

#### Problem
```java
public class Order {
    @OneToMany  // Loads all 1000 items immediately
    List<OrderItem> items;
}

// Each order query returns 1000+ items = large memory
```

#### Solution
```java
public class Order {
    @OneToMany(fetch = FetchType.LAZY)  // Load only when accessed
    List<OrderItem> items;
}
```

#### Impact
- Memory per order: 50MB (was 2GB)
- Load time: 10ms (was 200ms)
- **Improvement: 95%**

---

## Database Optimizations

### 1. Indexing

#### Problem
```sql
SELECT * FROM orders WHERE customer_id = 123
-- Full table scan: 500ms
```

#### Solution
```sql
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
```

#### Impact
- Before: 500ms (full table scan)
- After: 5ms (index lookup)
- **Improvement: 99%**

### 2. Query Optimization

#### Problem: N+1 Query
```java
List<customers> = getAll();        // 1 query
for (customer : customers) {
    customer.orders = getOrders();  // N additional queries
}
// Total: 1 + N queries
```

#### Solution: Join Query
```sql
SELECT c.*, o.* FROM customers c
LEFT JOIN orders o ON c.id = o.customer_id
WHERE c.active = true
-- 1 query instead of 1 + N
```

#### Impact
- Before: 100 queries (100ms each = 10,000ms)
- After: 1 query (100ms)
- **Improvement: 99%**

### 3. Connection Pool Tuning

#### Problem
```
Max connections: 5
Load: 100 concurrent users
Result: 95 users waiting for connection
```

#### Solution
```properties
# Increase pool size
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10

# With 50 connections, handle 50 concurrent users efficiently
```

---

## JVM Tuning

### 1. Heap Size

#### Problem
```
Default: -Xmx512m (only 512MB)
Load: 100 users × 5MB each = 500MB
Result: Garbage collection pauses, OutOfMemoryError
```

#### Solution
```bash
# Increase heap
java -Xmx8g -Xms8g -jar app.jar
```

#### Impact
- Fewer GC pauses
- More stable performance
- **Improvement: 20-30%**

### 2. Garbage Collection Tuning

#### Problem
```
Default GC: Stops all threads during collection
Load test: Sudden 1-2 second pauses
Latency spike!
```

#### Solution
```bash
# Use low-pause GC
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar app.jar
```

#### Impact
- GC pauses: 1000ms → 200ms
- Latency spikes: Eliminated
- **Improvement: 80%**

### 3. String Deduplication

#### Problem
```
Many duplicate strings in memory
Wasted heap
```

#### Solution
```bash
java -XX:+UseStringDeduplication -jar app.jar
```

#### Impact
- Heap efficiency: +10-15%

---

## Infrastructure Scaling

### 1. Horizontal Scaling (Add Machines)

#### Before
```
Single machine: 1,000 req/sec max
Bottleneck: CPU at 100%
```

#### After
```
3 machines: 3,000 req/sec
Load balanced across machines
CPU: 33% on each
```

### 2. Vertical Scaling (Better Hardware)

#### Before
```
Small VM: 2 CPU, 4GB RAM
Capacity: 500 req/sec
```

#### After
```
Large VM: 8 CPU, 32GB RAM
Capacity: 2,000 req/sec
```

### 3. Database Optimization

#### Before
```
Single database: 1,000 queries/sec max
```

#### After
```
Replication: Master + 2 replicas for reads
Sharding: Data split across 3 shards
Result: 3,000 queries/sec
```

---

## Optimization Workflow

```
1. RUN LOAD TEST
   └─ Identify bottleneck

2. INVESTIGATE
   ├─ Application traces
   ├─ Database query logs
   ├─ JVM metrics (GC, memory)
   └─ System metrics (CPU, disk)

3. OPTIMIZE
   ├─ Apply fastest wins first
   ├─ Measure improvement
   └─ Re-run test

4. VERIFY
   ├─ Check p95 latency improved
   ├─ Verify no regressions
   └─ Update documentation

5. REPEAT
   └─ Find next bottleneck
```

---

## ROI Analysis

### Example: Add Database Cache

```
Cost: $500 (Redis cluster)
Improvement: p95 latency 500ms → 100ms

Before: Customer abandonment 40%
After: Customer abandonment 5%

Revenue impact: +$100,000/month

ROI: (100,000 - 500) / 500 = 199x (per month!)
```

---

## Key Takeaways

1. **Caching** = 10-100x faster responses
2. **Indexing** = 100x faster queries
3. **Connection pooling** = Eliminate bottlenecks
4. **JVM tuning** = Reduce GC pauses
5. **Scaling** = Handle more load
6. **Measure** = Verify improvement with re-testing

---

## Next Steps

→ **Read next**: [Distributed Testing](04-distributed-testing.md) - Load test across multiple machines

