# Lab 5: CRUD Operations

## Learning Objectives

By the end of this lab, you'll understand:

- ✅ How to implement **PUT requests** (update entire resource)
- ✅ How to implement **PATCH requests** (partial update)
- ✅ How to implement **DELETE requests** (resource removal)
- ✅ How to use **request bodies** with JSON payloads
- ✅ How to load request bodies from **files**
- ✅ How to test complete CRUD workflows

## Real-World Scenario

So far you've tested GET and POST. But real APIs do more:

- **POST** `/users` → Create user, get back ID
- **GET** `/users/:id` → Retrieve user
- **PUT** `/users/:id` → Replace entire user
- **PATCH** `/users/:id` → Update only some fields
- **DELETE** `/users/:id` → Remove user

A realistic performance test must include the full CRUD lifecycle.

---

## Concept: CRUD Methods

### POST (Create)
```java
.exec(http("Create user")
    .post("/users")
    .body(StringBody("{\"name\": \"Alice\", \"email\": \"alice@example.com\"}"))
    .check(status().is(201))
    .check(jsonPath("$.id").saveAs("userId"))
)
```

### GET (Read)
```java
.exec(http("Get user")
    .get("/users/#{userId}")
    .check(status().is(200))
)
```

### PUT (Update - Replace All)
```java
.exec(http("Update user")
    .put("/users/#{userId}")
    .body(StringBody("{\"name\": \"Bob\", \"email\": \"bob@example.com\"}"))
    .check(status().is(200))
)
```

### PATCH (Update - Partial)
```java
.exec(http("Patch user")
    .patch("/users/#{userId}")
    .body(StringBody("{\"email\": \"newemail@example.com\"}"))
    .check(status().is(200))
)
```

### DELETE (Remove)
```java
.exec(http("Delete user")
    .delete("/users/#{userId}")
    .check(status().is(204))
)
```

---

## Code Pattern: Complete CRUD Scenario

```java
ScenarioBuilder crudJourney = scenario("CRUD Operations")
    // 1. CREATE
    .exec(http("Create user")
        .post("/users")
        .body(StringBody("{\"name\": \"Test User\", \"email\": \"test@example.com\"}"))
        .check(status().is(201))
        .check(jsonPath("$.id").saveAs("userId"))
    )
    .pause(1)
    
    // 2. READ
    .exec(http("Get user")
        .get("/users/#{userId}")
        .check(status().is(200))
        .check(jsonPath("$.name").is("Test User"))
    )
    .pause(1)
    
    // 3. UPDATE (PUT)
    .exec(http("Update user (PUT)")
        .put("/users/#{userId}")
        .body(StringBody("{\"name\": \"Updated User\", \"email\": \"updated@example.com\"}"))
        .check(status().is(200))
    )
    .pause(1)
    
    // 4. PARTIAL UPDATE (PATCH)
    .exec(http("Patch user (PATCH)")
        .patch("/users/#{userId}")
        .body(StringBody("{\"email\": \"patch@example.com\"}"))
        .check(status().is(200))
    )
    .pause(1)
    
    // 5. DELETE
    .exec(http("Delete user")
        .delete("/users/#{userId}")
        .check(status().is(204))
    );
```

---

## Loading Request Bodies from Files

```java
// Instead of: .body(StringBody("{...}"))
// Use: .body(new FileBody("bodies/create_user.json"))

.exec(http("Create user from file")
    .post("/users")
    .body(new FileBody("bodies/create_user.json"))
    .check(status().is(201))
)
```

---

## Running the Lab

```bash
cd /Users/sgovinda/Learn/GatlingLearning

mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim05_CRUD
```

### Expected Results

- ✅ 200+ total requests (40 per user)
- ✅ >99% success rate
- ✅ p95 latency <2000ms
- ✅ All CRUD operations work
- ✅ POST returns 201
- ✅ GET returns 200
- ✅ PUT/PATCH return 200
- ✅ DELETE returns 204

---

## Key Takeaways

1. **POST** = Create (201 Created)
2. **GET** = Read (200 OK)
3. **PUT** = Replace all (200 OK)
4. **PATCH** = Update partial (200 OK)
5. **DELETE** = Remove (204 No Content)
6. **Status codes matter** - verify in checks
7. **Body loading** from files keeps code clean

---

## Navigation

**← Previous**: [Lab 4: Load Profiles](04-lab-load-profiles.md)  
**→ Next**: [Lab 6: Kafka Producer](06-lab-kafka-producer.md)  
**↑ Up**: [Lab Overview](00-lab-overview.md)

