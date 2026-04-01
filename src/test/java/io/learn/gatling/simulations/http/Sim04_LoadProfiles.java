package io.learn.gatling.simulations.http;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

/**
 * ═══════════════════════════════════════════════════════════════
 * LESSON 4 — Load Profiles & Injection Models
 * ═══════════════════════════════════════════════════════════════
 *
 * What you will learn:
 *   1. All major open-model injection steps:
 *       • atOnceUsers        — spike / thundering-herd test
 *       • rampUsers          — gradual ramp over time
 *       • constantUsersPerSec — steady-state load
 *       • rampUsersPerSec    — ramp arrival rate
 *       • stressPeakUsers    — smooth spike (Heaviside)
 *       • incrementUsersPerSec — step-load pattern ← used in dps-load
 *   2. Chaining multiple injection steps with .injectOpen(step1, step2, ...)
 *   3. Throttle — capping RPS regardless of virtual-user count
 *   4. maxDuration — hard time limit for the simulation
 *
 * Profiles to try (change which setUp block is active):
 *   SMOKE, RAMP, STEP_LOAD, SPIKE
 *
 * Run locally:
 *   mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim04_LoadProfiles
 */
public class Sim04_LoadProfiles extends Simulation {

  HttpProtocolBuilder httpProtocol =
      http
          .baseUrl("https://jsonplaceholder.typicode.com")
          .header("Accept", "application/json");

  // A simple scenario — single GET request
  ScenarioBuilder readPost = scenario("Read Post").exec(http("GET /posts/1").get("/posts/1"));

  // ═══════════════════════════════════════════════════════════════
  // Choose ONE of the profiles below by uncommenting its setUp block
  // ═══════════════════════════════════════════════════════════════

  // ── PROFILE A: SMOKE TEST ─────────────────────────────────────
  // Minimal load just to verify the simulation compiles and the API responds.
  // Run this first whenever you write a new simulation.
  {
    setUp(
            readPost.injectOpen(
                atOnceUsers(1) // Inject exactly 1 user immediately
                ))
        .protocols(httpProtocol);
  }

  // ── PROFILE B: RAMP TEST ──────────────────────────────────────
  // Uncomment to use. Gradually increases load to find the breaking point.
  /*
  {
    setUp(
            readPost.injectOpen(
                nothingFor(2),                       // Wait 2 s before starting
                rampUsers(10).during(10),            // Ramp 10 users over 10 s
                constantUsersPerSec(5).during(20),   // Hold 5 users/sec for 20 s
                rampUsersPerSec(5).to(0).during(5)   // Cool-down ramp-down
                ))
        .protocols(httpProtocol)
        .maxDuration(java.time.Duration.ofSeconds(60));
  }
  */

  // ── PROFILE C: STEP LOAD ──────────────────────────────────────
  // Matches the OPEN_STEP_LOAD model used in dps-load.
  // Gradually steps up to find maximum sustainable throughput.
  /*
  {
    setUp(
            readPost.injectOpen(
                // Step 1: 1 user/sec for 10s
                constantUsersPerSec(1).during(10),
                // Step 2: 3 users/sec for 10s
                constantUsersPerSec(3).during(10),
                // Step 3: 6 users/sec for 10s
                constantUsersPerSec(6).during(10),
                // Step 4: 10 users/sec for 10s
                constantUsersPerSec(10).during(10)))
        .protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile(95).lt(1000),
            global().successfulRequests().percent().gte(99.0));
  }
  */

  // ── PROFILE D: SPIKE TEST ─────────────────────────────────────
  // Tests how the system handles a sudden traffic surge.
  /*
  {
    setUp(
            readPost.injectOpen(
                constantUsersPerSec(2).during(10),  // Baseline
                atOnceUsers(50),                     // Spike!
                constantUsersPerSec(2).during(10)   // Recovery
                ))
        .protocols(httpProtocol);
  }
  */
}
