# Advanced Patterns

## Overview

Advanced patterns handle complex, real-world scenarios:

- **Conditional branches** - Different flows based on conditions
- **Loops** - Repeat actions multiple times
- **Groups** - Organize sub-scenarios
- **Async operations** - Non-blocking patterns
- **Error handling** - Gracefully handle failures
- **Dynamic data** - Change behavior based on responses

---

## Pattern 1: Conditional Branches

### Simple If/Then

```java
.exec(
    http("Check balance")
        .get("/account/balance")
        .check(jsonPath("$.amount").saveAs("balance"))
)
.doIf(session -> {
    double balance = Double.parseDouble(session.getString("balance"));
    return balance < 100;  // Condition
})
    .then(
        http("Request funds")
            .post("/request-funds?amount=500")
    )
```

### If/Then/Else

```java
.exec(
    http("Check membership")
        .get("/user/membership")
        .check(jsonPath("$.tier").saveAs("tier"))
)
.doIfOrElse(session -> session.getString("tier").equals("premium"))
    .then(
        http("Premium benefits")
            .get("/premium-features")
    )
    .orElse(
        http("Free benefits")
            .get("/free-features")
    )
```

---

## Pattern 2: Loops

### Repeat Loop

```java
.repeat(5)  // Execute 5 times
    .on(
        exec(http("Search")
            .get("/search?q=phone")
        )
        .pause(1)
    )
```

### While Loop

```java
.asLongAs(session -> session.getInt("count") < 10)
    .on(
        exec(session -> {
            int count = session.getInt("count");
            return session.set("count", count + 1);
        })
        .exec(http("Request")
            .get("/data/" + session.getInt("count"))
        )
        .pause(1)
    )
```

### Foreach Loop

```java
.exec(session -> {
    List<String> productIds = List.of("P1", "P2", "P3", "P4", "P5");
    return session.set("productIds", productIds);
})
.foreach("#{productIds}", "productId")
    .on(
        exec(http("View product")
            .get("/products/#{productId}")
        )
        .pause(1)
    )
```

---

## Pattern 3: Groups

### Organize Sub-Scenarios

```java
// Browsing group
ScenarioBuilder browsing = scenario("Browsing")
    .exec(http("Homepage").get("/"))
    .pause(2)
    .exec(http("Products").get("/products"))
    .pause(1)
    .exec(http("Filters").get("/products?category=electronics"))
    .pause(1)
    .exec(http("Sort").get("/products?sort=price"));

// Purchasing group
ScenarioBuilder purchasing = scenario("Purchasing")
    .exec(http("Add to cart").post("/cart"))
    .pause(1)
    .exec(http("Checkout").post("/checkout"))
    .pause(2)
    .exec(http("Payment").post("/payment"));

// Complete journey
ScenarioBuilder fullJourney = scenario("Full Journey")
    .group(browsing)      // First: browse
    .pause(5)             // Think time
    .group(purchasing);   // Then: purchase

// Use
setUp(fullJourney.injectOpen(constantUsersPerSec(10).during(60)))
```

---

## Pattern 4: Error Handling

### Try/Catch Pattern

```java
.tryMax(3)  // Retry up to 3 times
    .on(
        exec(http("Fetch data")
            .get("/api/data")
            .check(status().is(200))
        )
    )
    .exitHereIf(session -> session.getBoolean("_counterexceeded"))
    // If retries exceeded, continue without failing
```

### Graceful Degradation

```java
.exec(http("Try Premium API")
    .get("/api/v2/data")
    .check(status().not(500))
)
.doIfOrElse(session -> session.getString("_status").equals("OK"))
    .then(
        // Premium API succeeded
        exec(session -> session.set("apiVersion", "v2"))
    )
    .orElse(
        // Premium API failed, use fallback
        exec(http("Fallback to Standard API")
            .get("/api/v1/data")
        )
        .exec(session -> session.set("apiVersion", "v1"))
    )
```

---

## Pattern 5: Dynamic Request Bodies

### Use Session Variables in JSON

```java
.feed(userFeeder)  // {name: "Alice", age: 30, city: "NYC"}
.exec(http("Create profile")
    .post("/profile")
    .body(StringBody(
        "{\"name\": \"#{name}\", \"age\": #{age}, \"city\": \"#{city}\"}"
    ))
)
```

### Extract and Reuse

```java
.exec(http("Get product")
    .get("/products/#{productId}")
    .check(jsonPath("$.price").saveAs("price"))
    .check(jsonPath("$.tax").saveAs("tax"))
)
.pause(1)
.exec(http("Add to cart")
    .post("/cart")
    .body(StringBody(
        "{\"productId\": \"#{productId}\", \"price\": #{price}, \"tax\": #{tax}}"
    ))
)
```

---

## Pattern 6: Complex Multi-Step Journeys

### E-Commerce Journey

```java
ScenarioBuilder ecommerceJourney = scenario("E-Commerce")
    // Step 1: Search
    .feed(searchTermFeeder)
    .exec(http("Search")
        .get("/search?q=#{searchTerm}")
        .check(jsonPath("$[0].id").saveAs("productId"))
    )
    .pause(2)
    
    // Step 2: View product
    .exec(http("View product")
        .get("/products/#{productId}")
        .check(jsonPath("$.price").saveAs("price"))
    )
    .pause(3)
    
    // Step 3: Conditional - Sometimes add to cart
    .doIf(session -> Math.random() > 0.3)  // 70% add to cart
        .then(
            exec(http("Add to cart")
                .post("/cart")
                .body(StringBody("{\"productId\": \"#{productId}\"}"))
            )
            .pause(1)
            
            // Step 4: Proceed to checkout
            .exec(http("Checkout")
                .post("/checkout")
                .check(jsonPath("$.orderId").saveAs("orderId"))
            )
            .pause(2)
            
            // Step 5: Payment
            .exec(http("Payment")
                .post("/payment")
                .body(StringBody("{\"orderId\": \"#{orderId}\", \"amount\": #{price}}"))
            )
        )
    
    // Step 6: Always view confirmation
    .doIfOrElse(session -> session.contains("orderId"))
        .then(
            exec(http("Order confirmation")
                .get("/orders/#{orderId}")
            )
        )
        .orElse(
            exec(http("Continue browsing")
                .get("/products")
            )
        );
```

---

## Pattern 7: State Management

### Track User State

```java
.exec(session -> {
    // Initialize user state
    session.set("userState", "logged_out");
    session.set("cartItems", 0);
    session.set("totalSpent", 0.0);
    return session;
})

// State transitions
.exec(http("Login")
    .post("/login")
    .check(status().is(200))
)
.exec(session -> session.set("userState", "logged_in"))

.repeat(5).on(
    exec(http("Shop")
        .get("/products")
    )
    .exec(session -> {
        int items = session.getInt("cartItems");
        return session.set("cartItems", items + 1);
    })
)

.exec(http("Checkout")
    .post("/checkout")
)
.exec(session -> session.set("userState", "checked_out"))
```

---

## Key Takeaways

1. **Conditional logic enables branching**
2. **Loops repeat actions efficiently**
3. **Groups organize complex scenarios**
4. **Error handling ensures robustness**
5. **Session state enables complex workflows**
6. **Realistic scenarios require all patterns combined**

---

## Navigation

**← Previous**: [Custom Feeders](01-custom-feeders.md)  
**→ Next**: [Optimization Tips](03-optimization-tips.md)  
**↑ Up**: [Documentation Index](../index.md)

