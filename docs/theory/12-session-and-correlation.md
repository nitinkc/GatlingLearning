# Session & Correlation

## What is a Session?

Each virtual user has a **session** - a map of variables that persist throughout their scenario execution.

```java
// Session for User 1
Session {
    userId: "123"           ← From feeder
    username: "alice"       ← From feeder
    email: "alice@example.com"  ← From feeder
    productId: "P456"       ← Extracted from response
    cartId: "cart-789"      ← Extracted from response
}

// Session for User 2 (different instance)
Session {
    userId: "456"
    username: "bob"
    email: "bob@example.com"
    productId: "P789"
    cartId: "cart-001"
}
```

## Correlation: Extracting & Reusing Data

Correlation means extracting data from one response and using it in the next request.

```java
scenario("Correlated Journey")
    // Step 1: Create user, extract ID from response
    .exec(http("Create User")
        .post("/users")
        .body(StringBody("{\"name\": \"John\"}"))
        .check(status().is(201))
        .check(jsonPath("$.userId").saveAs("newUserId")))
    
    // Step 2: Use extracted ID in next request
    .exec(http("Get User Details")
        .get("/users/#{newUserId}"))
    
    // Step 3: Create order for that user
    .exec(http("Create Order")
        .post("/orders")
        .body(StringBody("{\"userId\": \"#{newUserId}\", \"amount\": 99.99}")))
```

## Practical Example: E-commerce Checkout

```java
scenario("E-commerce Checkout")
    .feed(userFeeder)  // userId, email from CSV
    
    // Login
    .exec(http("Login")
        .post("/auth/login")
        .body(StringBody("{\"userId\": \"#{userId}\"}"))
        .check(jsonPath("$.sessionToken").saveAs("token")))
    
    // Browse products
    .exec(http("Browse")
        .get("/products")
        .header("Authorization", "Bearer #{token}"))
        // Note: We can use extracted token in headers!
    
    // Add to cart
    .exec(http("Add Item")
        .post("/cart")
        .body(StringBody("{\"productId\": \"P123\", \"qty\": 1}"))
        .check(jsonPath("$.cartId").saveAs("cartId")))
    
    // Checkout
    .exec(http("Checkout")
        .post("/checkout")
        .body(StringBody("{\"cartId\": \"#{cartId}\", \"token\": \"#{token}\"}")))
```

---

## See Also

- Lab 3: Checks & Validation (demonstrates extraction)
- Lab 7: Kafka with Feeders (demonstrates feeder coordination)

---

## Next Steps

→ **Continue to Labs**: [Lab Overview](../labs/22-lab-overview.md)

