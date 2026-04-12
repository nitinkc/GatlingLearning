# FAQ & Troubleshooting

## Common Issues

### Issue: Simulation won't compile

**Error**: `Simulation class not found`

**Cause**: Class path issue or file doesn't exist

**Fix**:
```bash
# Verify file exists
ls src/test/java/io/learn/gatling/simulations/http/Sim01_BasicHttp.java

# Clean and rebuild
rm -rf target/
mvn clean compile

# Try running again
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim01_BasicHttp
```

---

### Issue: Connection refused to API

**Error**: `java.net.ConnectException: Connection refused`

**Cause**: Target API not running or unreachable

**Fix**:
```bash
# Test API manually
curl https://jsonplaceholder.typicode.com/posts

# Check if it's a network issue
ping jsonplaceholder.typicode.com

# Use different API for testing
# Edit simulation baseUrl temporarily
```

---

### Issue: CSV feeder file not found

**Error**: `java.io.FileNotFoundException: data/users.csv`

**Cause**: Feeder file path incorrect or missing

**Fix**:
```bash
# Verify file exists
ls src/test/resources/data/users.csv

# Check exact filename and path
find src/test/resources -name "*.csv"

# Ensure pom.xml includes test resources
# <testResources>
#   <testResource>
#     <directory>src/test/resources</directory>
#   </testResource>
# </testResources>
```

---

### Issue: Docker not found (for Kafka labs)

**Error**: `docker: command not found` or `Docker daemon not accessible`

**Cause**: Docker not installed or not running

**Fix**:
```bash
# Check Docker installed
docker -v

# Start Docker (macOS)
open -a Docker

# Wait for Docker to start, then
mvn gatling:test -Dgatling.simulationClass=...Sim06...
```

---

### Issue: Assertion failed

**Error**: `Assertions failed`

**Cause**: SLAs not met during test

**Fix**:
```bash
# Review HTML report to see actual metrics
open target/gatling/*/index.html

# Check what assertion failed
# Example: p95 latency = 600ms but target was 500ms

# Options:
# 1. Relax the SLA (if reasonable)
# 2. Optimize the code (if possible)
# 3. Scale infrastructure
# 4. Re-run test (could be temporary spike)
```

---

### Issue: Test takes too long

**Cause**: Load injection period too long

**Fix**:
```java
// Change from:
constantUsersPerSec(100).during(600)  // 10 minutes!

// To:
constantUsersPerSec(100).during(60)   // 1 minute
```

---

### Issue: Out of memory (OOM)

**Error**: `java.lang.OutOfMemoryError: Java heap space`

**Cause**: Too many simulated users for available memory

**Fix**:
```bash
# Increase JVM heap
export MAVEN_OPTS="-Xmx2g"
mvn gatling:test -Dgatling.simulationClass=...

# Or reduce load in simulation
constantUsersPerSec(100).during(300)  # Instead of 1000/sec
```

---

## General Troubleshooting Steps

### 1. Check Prerequisites

```bash
java -version          # Java 21+?
mvn -version          # Maven 3.9+?
docker -v             # Docker running? (if needed)
```

### 2. Clean Project

```bash
mvn clean
rm -rf target/
rm -rf ~/.m2/repository
```

### 3. Rebuild

```bash
mvn clean compile
mvn gatling:test -Dgatling.simulationClass=...
```

### 4. Check Logs

```bash
# Maven verbose output
mvn -X gatling:test -Dgatling.simulationClass=...

# Check console output for errors
```

### 5. Review Report

```bash
# Always check HTML report for actual metrics
open target/gatling/*/index.html
```

---

## Questions?

1. **Read**: Check relevant documentation section
2. **Search**: Is this covered in Quick Reference or Glossary?
3. **Experiment**: Try modifying the lab code
4. **Iterate**: Re-run with adjustments

---

## Next Steps

→ Back to **Labs**: [Lab Overview](../labs/22-lab-overview.md)

→ Or continue to **Advanced Topics**: Optimization, custom feeders, distributed testing

