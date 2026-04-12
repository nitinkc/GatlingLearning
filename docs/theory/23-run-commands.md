# Run Commands Reference

## Basic Commands

### Run a Single Simulation

```bash
cd /Users/sgovinda/Learn/GatlingLearning

mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim01_BasicHttp
```

### All HTTP Simulations

```bash
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim02_HttpWithFeeders
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim03_HttpChecks
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim04_LoadProfiles
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim05_CRUD
```

### Kafka Simulations (Requires Docker)

```bash
docker ps  # Verify Docker running

mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.kafka.Sim06_BasicKafkaProducer
mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.kafka.Sim07_KafkaWithFeeders
```

### View HTML Report

```bash
# macOS
open target/gatling/*/index.html

# Linux
xdg-open target/gatling/*/index.html

# Windows (Git Bash)
start target/gatling/*/index.html
```

---

## Useful Variations

### Clean Before Running

```bash
mvn clean gatling:test -Dgatling.simulationClass=...
```

### Verbose Output

```bash
mvn -X gatling:test -Dgatling.simulationClass=...
```

### Skip Tests (compile only)

```bash
mvn compile
```

### Run All Simulations

```bash
mvn gatling:test  # Runs all in classpath
```

---

## Troubleshooting Commands

### Check Maven Version

```bash
mvn -version
```

### Check Java Version

```bash
java -version
```

### Check Docker

```bash
docker ps
docker logs  # See container logs
```

### Clear Cache

```bash
rm -rf target/gatling
rm -rf ~/.m2/repository  # Full cache clear (slow)
```

---

## Next Steps

→ **FAQ**: [FAQ & Troubleshooting](04-faq.md)

