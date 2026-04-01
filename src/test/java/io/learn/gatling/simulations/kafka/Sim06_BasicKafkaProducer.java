package io.learn.gatling.simulations.kafka;

import static io.gatling.javaapi.core.CoreDsl.*;

import io.gatling.javaapi.core.*;
import io.learn.gatling.common.EmbeddedKafka;
import java.time.Duration;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * ═══════════════════════════════════════════════════════════════
 * LESSON 6 — Basic Kafka Producer Simulation
 * ═══════════════════════════════════════════════════════════════
 *
 * What you will learn:
 *   1. How Kafka simulations differ from HTTP simulations:
 *       • No built-in Gatling Kafka protocol (unlike HTTP)
 *       • We use exec(session -> ...) to call Kafka directly from Java
 *       • Success/failure is tracked manually via session.markAsSucceeded()
 *   2. Starting an embedded Kafka broker via Testcontainers (no cloud needed)
 *   3. Creating a KafkaProducer and sending messages
 *   4. Sending messages synchronously (send().get()) vs fire-and-forget
 *   5. Structuring the Gatling scenario around Kafka actions
 *
 * Prerequisites:
 *   • Docker must be running (Testcontainers uses it to start the Kafka broker)
 *
 * Run locally:
 *   mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.kafka.Sim06_BasicKafkaProducer
 *
 * How to verify messages arrived (optional, after run):
 *   docker ps   ← find the Kafka container name
 *   docker exec <container> kafka-console-consumer --bootstrap-server localhost:9092 \
 *     --topic orders --from-beginning --max-messages 10
 */
@Slf4j
public class Sim06_BasicKafkaProducer extends Simulation {

  // ── 1. Start embedded Kafka ────────────────────────────────────
  // EmbeddedKafka is a static singleton — the broker starts once,
  // before any virtual users are injected.
  private static final String BOOTSTRAP_SERVERS = EmbeddedKafka.bootstrapServers();
  private static final String TOPIC = "orders";

  // ── 2. Build a KafkaProducer ───────────────────────────────────
  // This is a standard Apache Kafka client producer.
  // It is shared across all virtual users (thread-safe).
  private static final KafkaProducer<String, String> PRODUCER = buildProducer();

  private static KafkaProducer<String, String> buildProducer() {
    Properties props = new Properties();
    // Where to connect
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    // How to serialize keys and values to bytes
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    // Acknowledgement strategy:
    //   "all"  → wait for all replicas to confirm (safe, slower)
    //   "1"    → wait for leader only
    //   "0"    → fire-and-forget (fastest, may lose data)
    props.put(ProducerConfig.ACKS_CONFIG, "1");
    // Retry up to 3 times on transient errors
    props.put(ProducerConfig.RETRIES_CONFIG, "3");
    return new KafkaProducer<>(props);
  }

  // ── 3. Scenario ───────────────────────────────────────────────
  ScenarioBuilder sendOrder =
      scenario("Send order to Kafka")

          // exec(session -> ...) lets you run arbitrary Java code inside Gatling.
          // The lambda receives the current virtual user's session and must return it.
          .exec(
              session -> {
                // Build a simple JSON message
                String orderId = "order-" + System.currentTimeMillis();
                String message =
                    """
                    {
                      "orderId": "%s",
                      "product": "Espresso",
                      "quantity": 2,
                      "price":   4.50
                    }
                    """.formatted(orderId);

                try {
                  // ProducerRecord(topic, key, value)
                  // key = orderId ensures all events for the same order go to the same partition
                  ProducerRecord<String, String> record =
                      new ProducerRecord<>(TOPIC, orderId, message);

                  // send().get() blocks until the broker acknowledges the message.
                  // For pure fire-and-forget, remove .get() — but then errors are silent.
                  PRODUCER.send(record).get();

                  log.debug("Sent message for orderId={}", orderId);

                  // Tell Gatling this "request" succeeded
                  return session.markAsSucceeded();
                } catch (Exception e) {
                  log.error("Failed to send Kafka message: {}", e.getMessage());
                  // Tell Gatling this "request" failed (counted in the error rate)
                  return session.markAsFailed();
                }
              });

  {
    setUp(sendOrder.injectOpen(constantUsersPerSec(5).during(10)))
        .maxDuration(Duration.ofSeconds(30))
        .assertions(
            // All messages should be sent successfully
            global().successfulRequests().percent().gte(100.0));
  }
}
