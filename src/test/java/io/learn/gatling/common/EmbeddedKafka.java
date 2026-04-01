package io.learn.gatling.common;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * ═══════════════════════════════════════════════════════════════
 * EmbeddedKafka — Shared Testcontainers Kafka broker
 * ═══════════════════════════════════════════════════════════════
 *
 * Starts a REAL Kafka broker inside Docker via Testcontainers.
 * Because it is static, the container is started once and reused
 * across all Kafka simulations in the same JVM process.
 *
 * Requirements:
 *   • Docker must be running locally (or in the CI runner)
 *   • No external Kafka cluster, no environment variables, no secrets
 *
 * The container is automatically stopped when the JVM exits
 * (Ryuk garbage-collection).
 */
@Slf4j
public class EmbeddedKafka {

  // Confluent Platform image — includes a full Kafka broker
  private static final KafkaContainer CONTAINER =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

  static {
    log.info("Starting embedded Kafka container (this may take 20-40 s the first time) ...");
    CONTAINER.start();
    log.info("Embedded Kafka started. Bootstrap servers: {}", CONTAINER.getBootstrapServers());
  }

  /** Returns the bootstrap servers address, e.g. {@code PLAINTEXT://localhost:12345}. */
  public static String bootstrapServers() {
    return CONTAINER.getBootstrapServers();
  }
}
