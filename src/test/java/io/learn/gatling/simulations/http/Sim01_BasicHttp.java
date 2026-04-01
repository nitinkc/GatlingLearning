package io.learn.gatling.simulations.http;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

/**
 * ═══════════════════════════════════════════════════════════════
 * LESSON 1 — Basic HTTP Simulation
 * ═══════════════════════════════════════════════════════════════
 *
 * What you will learn:
 *   1. The three building blocks of every Gatling simulation:
 *        • HttpProtocol  — "where to send requests"
 *        • ScenarioBuilder — "what virtual users do"
 *        • setUp()           — "how many users, how fast"
 *   2. Making simple GET requests
 *   3. Pauses between requests (think-time)
 *   4. Constant-rate load injection (constantUsersPerSec)
 *
 * Target API: https://jsonplaceholder.typicode.com  (free, no auth)
 *
 * Run locally:
 *   mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim01_BasicHttp
 */
public class Sim01_BasicHttp extends Simulation {

  // ── 1. HTTP Protocol ──────────────────────────────────────────
  // Defines the base URL and common headers for every request in
  // this simulation. Shared across scenarios via .protocols(http).
  HttpProtocolBuilder httpProtocol =
      http
          // All relative URLs will be prefixed with this base URL
          .baseUrl("https://jsonplaceholder.typicode.com")
          // Content-type header sent with every request
          .header("Content-Type", "application/json")
          // Accept header
          .header("Accept", "application/json")
          // Gatling follows redirects automatically by default.
          // Warm up the connection pool before the real load starts
          .warmUp("https://jsonplaceholder.typicode.com");

  // ── 2. Scenario ───────────────────────────────────────────────
  // A scenario is a sequence of actions a virtual user will execute.
  // Think of it as a user journey: "browse posts, then read one post".
  ScenarioBuilder browsePosts =
      scenario("Browse Posts") // A label shown in the HTML report

          // GET /posts — fetch all posts
          .exec(
              http("GET all posts") // The request name shown in the report
                  .get("/posts"))

          // Pause simulates think-time (the time a real user spends reading).
          // Without pauses, Gatling fires requests as fast as possible.
          .pause(1)

          // GET /posts/1 — fetch a single post by ID
          .exec(http("GET single post").get("/posts/1"))

          .pause(1)

          // GET /comments?postId=1 — query parameter style
          .exec(
              http("GET comments for post")
                  .get("/comments")
                  .queryParam("postId", "1")); // adds ?postId=1 to the URL

  // ── 3. setUp() ────────────────────────────────────────────────
  // Wire everything together:
  //   • Inject users into the scenario
  //   • Attach the HTTP protocol
  //
  // constantUsersPerSec(n).during(d) → ramp n users/sec steadily for d seconds
  {
    setUp(
            browsePosts
                .injectOpen(
                    // Inject 2 new virtual users every second for 15 seconds
                    // → 30 total users, each executing the scenario once
                    constantUsersPerSec(2).during(15)))
        .protocols(httpProtocol);
  }
}
