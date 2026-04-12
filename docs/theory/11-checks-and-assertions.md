# Checks & Assertions

## Checks: Validating Responses

Checks validate that responses meet expectations. Failed checks mark requests as failed.

```java
scenario("User Journey with Checks")
    .exec(http("Get Users")
        .get("/api/users")
        .check(status().is(200))  // HTTP status must be 200
        .check(jsonPath("$.users").exists())  // Field must exist
    )
    .exec(http("Get User Details")
        .get("/api/users/123")
        .check(status().in(200, 304))  // Accept 200 or 304 (not modified)
        .check(bodyString().contains("John")))  // Body contains text
```

## Assertions: Test Pass/Fail

Assertions define SLA requirements. Test fails if any assertion fails.

```java
setUp(
    scenario.injectOpen(constantUsersPerSec(100).during(300))
)
.assertions(
    global().responseTime().p95().lt(500),       // p95 < 500ms
    global().responseTime().p99().lt(1000),      // p99 < 1000ms
    global().successfulRequests().percent().gt(99.0)  // >99% success
)
```

---

## See Also

- Lab 3: Checks & Validation
- Key Metrics & Measurements

---

## Next Steps

→ **Read next**: [Session & Correlation](06-session-and-correlation.md)

