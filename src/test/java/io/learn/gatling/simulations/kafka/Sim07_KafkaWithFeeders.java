package io.learn.gatling.simulations.kafka;

import static io.gatling.javaapi.core.CoreDsl.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.*;
import io.learn.gatling.common.EmbeddedKafka;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * ═══════════════════════════════════════════════════════════════
 * LESSON 7 — Kafka with Feeders & Template Payloads
 * ═══════════════════════════════════════════════════════════════
 *
 * What you will learn:
 *   1. Using a CSV feeder to parameterize Kafka messages
 *      (mirrors what dps-load does with its config JSON + CSV feeder)
 *   2. Loading a JSON payload template from disk and replacing
 *      placeholders with feeder data — no EL, pure Java string ops
 *   3. Multiple scenarios on the same Kafka broker (different topics)
 *   4. Using Kafka message keys for partition routing
 *   5. Async (fire-and-forget) vs sync Kafka sends
 *   6. Simulating back-pressure with pauses
 *
 * Files used:
 *   src/test/resources/data/products.csv   — product data feeder
 *   src/test/resources/bodies/product_event.json — message template
 *
 * Run locally:
 *   mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.kafka.Sim07_KafkaWithFeeders
 */
@Slf4j
public class Sim07_KafkaWithFeeders extends Simulation {

  private static final String BOOTSTRAP_SERVERS = EmbeddedKafka.bootstrapServers();
  private static final String ORDERS_TOPIC = "orders";
  private static final String INVENTORY_TOPIC = "inventory-updates";

  private static final KafkaProducer<String, String> PRODUCER = buildProducer();
  private static final ObjectMapper MAPPER = new ObjectMapper();

  // ── Load the JSON template once at startup ────────────────────
  // Template lives at: src/test/resources/bodies/product_event.json
  // It uses simple {{placeholder}} markers (not Gatling EL) so we
  // can replace them in pure Java, independent of the Gatling session.
  private static final String ORDER_TEMPLATE = loadTemplate("bodies/product_event.json");
  private static final String INVENTORY_TEMPLATE = loadTemplate("bodies/inventory_event.json");

  // ── CSV Feeders ────────────────────────────────────────────────
  // Each CSV row is injected into the Gatling session.
  // We read session values in the exec() lambda to build the message.
  FeederBuilder<String> productFeeder = csv("data/products.csv").circular();
  FeederBuilder<String> storeFeeder   = csv("data/stores.csv").circular();

  // ── Scenario 1: Order events ───────────────────────────────────
  ScenarioBuilder sendOrderEvent =
      scenario("Send order event")
          .feed(productFeeder)
          .exec(
              session -> {
                // Read feeder values from the Gatling session
                String productId   = session.getString("productId");
                String productName = session.getString("productName");
                String price       = session.getString("price");

                // Replace {{placeholder}} tokens in the template
                String message =
                    ORDER_TEMPLATE
                        .replace("{{orderId}}", "ord-" + UUID.randomUUID().toString().substring(0, 8))
                        .replace("{{productId}}", productId)
                        .replace("{{productName}}", productName)
                        .replace("{{price}}", price)
                        .replace("{{timestamp}}", String.valueOf(System.currentTimeMillis()));

                try {
                  // Use productId as the Kafka message key → same product always
                  // lands on the same partition (ordered per product)
                  ProducerRecord<String, String> record =
                      new ProducerRecord<>(ORDERS_TOPIC, productId, message);

                  // Async send — callback logs errors but doesn't block
                  PRODUCER.send(
                      record,
                      (metadata, exception) -> {
                        if (exception != null) {
                          log.error("Failed to send order event: {}", exception.getMessage());
                        } else {
                          log.debug(
                              "Order event sent → topic={} partition={} offset={}",
                              metadata.topic(), metadata.partition(), metadata.offset());
                        }
                      });

                  return session.markAsSucceeded();
                } catch (Exception e) {
                  log.error("Error sending order event: {}", e.getMessage());
                  return session.markAsFailed();
                }
              })
          // Simulate realistic think-time between orders
          .pause(Duration.ofMillis(100), Duration.ofMillis(500));

  // ── Scenario 2: Inventory update events ───────────────────────
  ScenarioBuilder sendInventoryEvent =
      scenario("Send inventory update")
          .feed(storeFeeder)
          .feed(productFeeder)
          .exec(
              session -> {
                String storeId   = session.getString("storeId");
                String storeName = session.getString("storeName");
                String productId = session.getString("productId");

                String message =
                    INVENTORY_TEMPLATE
                        .replace("{{storeId}}", storeId)
                        .replace("{{storeName}}", storeName)
                        .replace("{{productId}}", productId)
                        .replace("{{quantity}}", String.valueOf((int) (Math.random() * 100)))
                        .replace("{{timestamp}}", String.valueOf(System.currentTimeMillis()));

                try {
                  // Sync send for inventory — we want guaranteed delivery for stock changes
                  PRODUCER
                      .send(new ProducerRecord<>(INVENTORY_TOPIC, storeId, message))
                      .get();
                  return session.markAsSucceeded();
                } catch (Exception e) {
                  log.error("Error sending inventory event: {}", e.getMessage());
                  return session.markAsFailed();
                }
              });

  {
    setUp(
            // Run both scenarios concurrently on the same (embedded) Kafka broker
            sendOrderEvent.injectOpen(
                rampUsersPerSec(1).to(10).during(10),
                constantUsersPerSec(10).during(20)),
            sendInventoryEvent.injectOpen(
                constantUsersPerSec(3).during(30)))
        .maxDuration(Duration.ofSeconds(60))
        .assertions(
            global().successfulRequests().percent().gte(99.0));
  }

  // ─────────────────────────────────────────────────────────────
  // Helpers
  // ─────────────────────────────────────────────────────────────

  private static KafkaProducer<String, String> buildProducer() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.ACKS_CONFIG, "1");
    props.put(ProducerConfig.RETRIES_CONFIG, "3");
    // Batch messages for efficiency when sending async
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
    props.put(ProducerConfig.LINGER_MS_CONFIG, "5");
    return new KafkaProducer<>(props);
  }

  private static String loadTemplate(String resourcePath) {
    try (InputStream in =
        Sim07_KafkaWithFeeders.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new IllegalArgumentException("Template not found on classpath: " + resourcePath);
      }
      return new String(in.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException("Failed to load template: " + resourcePath, e);
    }
  }
}
