# Custom Feeders

## Overview

Custom feeders generate **unlimited, realistic data** on-the-fly without pre-built CSV files.

Use cases:
- Generate unique email addresses
- Generate valid credit card numbers
- Generate realistic timestamps
- Generate data with constraints (age >18, balance >0)
- Generate data that changes over time

---

## CSV Feeders vs Custom Feeders

### CSV Feeder (Limited)
```java
csv("data/users.csv")
// ✓ Simple
// ✓ Easy to set up
// ✗ Limited to rows in file
// ✗ Can't generate unlimited data
// ✗ Can't apply logic/constraints
```

### Custom Feeder (Flexible)
```java
Iterator<Map<String, Object>> feeder = Stream.generate(() -> {
    Map<String, Object> map = new HashMap<>();
    map.put("email", "user" + UUID.randomUUID() + "@example.com");
    return map;
}).iterator();

// ✓ Generate unlimited unique data
// ✓ Apply logic and constraints
// ✓ Generate realistic patterns
// ✓ No file required
```

---

## Basic Custom Feeder

### Template

```java
Iterator<Map<String, Object>> customFeeder = 
    Stream.generate(() -> {
        Map<String, Object> map = new HashMap<>();
        
        // Generate field 1
        map.put("fieldName1", generateValue1());
        
        // Generate field 2
        map.put("fieldName2", generateValue2());
        
        return map;
    }).iterator();

// Use in scenario
.feed(new SequenceFeeder(customFeeder))
.exec(http("Request")
    .get("/endpoint?field1=#{fieldName1}&field2=#{fieldName2}")
)
```

---

## Example 1: Email Generator

### Code

```java
Iterator<Map<String, Object>> emailFeeder = 
    Stream.generate(() -> {
        Map<String, Object> map = new HashMap<>();
        int randomNum = new Random().nextInt(100000);
        map.put("email", "user" + randomNum + "@example.com");
        return map;
    }).iterator();

// Use
.feed(new SequenceFeeder(emailFeeder))
.exec(http("Register")
    .post("/register?email=#{email}")
)
```

### Result

```
First iteration: email=user42156@example.com
Second iteration: email=user87234@example.com
Third iteration: email=user12389@example.com
(unique every time)
```

---

## Example 2: Credit Card Generator

### Code

```java
Iterator<Map<String, Object>> creditCardFeeder = 
    Stream.generate(() -> {
        Map<String, Object> map = new HashMap<>();
        Random random = new Random();
        
        // Visa prefix: 4532 (valid test number)
        String cardNum = "4532" + String.format("%012d", random.nextLong() % 1000000000000L);
        
        map.put("cardNumber", cardNum);
        map.put("expiry", "12/25");
        map.put("cvv", String.format("%03d", random.nextInt(1000)));
        
        return map;
    }).iterator();

// Use
.feed(new SequenceFeeder(creditCardFeeder))
.exec(http("Payment")
    .post("/payment")
    .body(StringBody("{\"cardNumber\": \"#{cardNumber}\", \"cvv\": \"#{cvv}\"}"))
)
```

---

## Example 3: Timestamp Generator

### Code

```java
Iterator<Map<String, Object>> timestampFeeder = 
    Stream.generate(() -> {
        Map<String, Object> map = new HashMap<>();
        
        // Current time + random offset (last 7 days)
        long now = System.currentTimeMillis();
        long sevenDaysMs = 7 * 24 * 60 * 60 * 1000L;
        long randomOffset = new Random().nextLong() % sevenDaysMs;
        long timestamp = now - randomOffset;
        
        map.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .format(new Date(timestamp)));
        
        return map;
    }).iterator();

// Use
.feed(new SequenceFeeder(timestampFeeder))
.exec(http("Create Event")
    .post("/events")
    .body(StringBody("{\"timestamp\": \"#{timestamp}\"}"))
)
```

---

## Example 4: Constrained Data (Age >18)

### Code

```java
Iterator<Map<String, Object>> personFeeder = 
    Stream.generate(() -> {
        Map<String, Object> map = new HashMap<>();
        Random random = new Random();
        
        // Generate age between 18 and 80
        int age = 18 + random.nextInt(62);
        
        // Generate name
        String[] names = {"Alice", "Bob", "Charlie", "Diana", "Eve"};
        String name = names[random.nextInt(names.length)];
        
        map.put("age", age);
        map.put("name", name);
        
        return map;
    }).iterator();

// Constraint: Only use if age >= 18
.feed(new SequenceFeeder(personFeeder))
.doIf(session -> Integer.parseInt(session.getString("age")) >= 18)
    .then(
        http("Register Adult")
            .post("/register?name=#{name}&age=#{age}")
    )
```

---

## Example 5: Realistic Product Names

### Code

```java
Iterator<Map<String, Object>> productFeeder = 
    Stream.generate(() -> {
        Map<String, Object> map = new HashMap<>();
        Random random = new Random();
        
        String[] adjectives = {"Premium", "Ultra", "Deluxe", "Professional", "Standard"};
        String[] nouns = {"Widget", "Gadget", "Tool", "Utility", "Device"};
        String[] categories = {"Pro", "Plus", "Max", "Basic", "Essential"};
        
        String productName = adjectives[random.nextInt(adjectives.length)] + " " +
                            nouns[random.nextInt(nouns.length)] + " " +
                            categories[random.nextInt(categories.length)];
        
        double price = 10 + random.nextDouble() * 1000; // $10-$1010
        
        map.put("productName", productName);
        map.put("price", String.format("%.2f", price));
        
        return map;
    }).iterator();
```

---

## Performance Considerations

### Good Feeder (Efficient)

```java
Iterator<Map<String, Object>> efficientFeeder = 
    Stream.generate(() -> {
        Map<String, Object> map = new HashMap<>();
        map.put("id", UUID.randomUUID().toString());  // Fast
        return map;
    }).iterator();
```

### Bad Feeder (Inefficient)

```java
Iterator<Map<String, Object>> inefficientFeeder = 
    Stream.generate(() -> {
        // ❌ Creating new objects every iteration
        // ❌ Complex logic
        // ❌ Database calls
        Map<String, Object> map = new HashMap<>();
        
        // This would slow down the load test
        User user = database.getRandomUser();  // SLOW!
        map.put("userId", user.getId());
        
        return map;
    }).iterator();
```

---

## Key Takeaways

1. **Custom feeders generate unlimited data**
2. **No file storage needed**
3. **Apply logic and constraints**
4. **Stream.generate() creates infinite iterator**
5. **Keep feeder logic efficient (fast)**
6. **Realistic data = realistic load test**

---

## Navigation

**← Previous**: [Load Test Analysis](16-load-test-analysis.md)  
**→ Next**: [Advanced Patterns](18-advanced-patterns.md)  
**↑ Up**: [Documentation Index](../index.md)

