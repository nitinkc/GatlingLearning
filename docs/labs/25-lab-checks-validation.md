# Lab 3: Checks & Validation

## Learning Objectives

By the end of this lab, you'll understand:

- ✅ How to validate responses with **checks**
- ✅ How to **extract data** from responses (JSON, headers, etc.)
- ✅ How to use extracted data in **subsequent requests** (correlation)
- ✅ How to handle **multi-step user journeys** with data flow
- ✅ How to debug validation failures

## Real-World Scenario

Your e-commerce API has evolved. Now users don't just browse products; they:

1. **Search** for products → API returns a list with product IDs
2. **Extract** the first product ID from the response
3. **View product details** using that ID
4. **Add product to cart** → API returns a cart ID
5. **Retrieve cart** using the cart ID to verify the product was added

---

## Concept: Checks & Extraction

### What Are Checks?

A **check** validates that a response meets your expectations:

```java
.check(
    status().is(200),                    // Status code must be 200
    bodyString().contains("productId")   // Response body contains "productId"
)
```

If a check fails, the request is marked as **FAILED**.

### What is Extraction?

**Extraction** pulls data from a response and saves it to the user's session:

```java
.check(
    jsonPath("$.products[0].id").saveAs("firstProductId")
)

// Later:
.exec(http("Get Details").get("/products/#{firstProductId}"))
```

---

## Code Pattern: Multi-Step Journey

```java
// Step 1: Search and extract
.exec(http("Search").get("/search")
    .check(jsonPath("$[0].id").saveAs("productId")))
.pause(1)

// Step 2: Use extracted data
.exec(http("Get Details")
    .get("/product/#{productId}"))
.pause(1)

// Step 3: Extract more data
.exec(http("Get Reviews")
    .get("/product/#{productId}/reviews")
    .check(jsonPath("$[0].id").saveAs("reviewId")))
.pause(1)

// Step 4: Use chained extractions
.exec(http("Get Review Details")
    .get("/review/#{reviewId}"))
```

---

## Running the Lab

```bash
cd /Users/sgovinda/Learn/GatlingLearning

mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim03_HttpChecks
```

### Expected Results

- ✅ 240+ total requests
- ✅ >99% success rate
- ✅ p95 latency <2000ms
- ✅ All checks pass

---

## Key Takeaways

1. **Checks validate** that responses match expectations
2. **Extraction enables** multi-step user journeys
3. **Session variables** link requests together
4. **JSONPath** is essential for JSON extraction
5. **Failed checks** mark requests as failures

---

## Navigation

**← Previous**: [Lab 2: HTTP Feeders](24-lab-http-feeders.md)  
**→ Next**: [Lab 4: Load Profiles](26-lab-load-profiles.md)  
**↑ Up**: [Lab Overview](22-lab-overview.md)
