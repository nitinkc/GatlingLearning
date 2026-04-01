package io.learn.gatling.simulations.http;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

/**
 * ═══════════════════════════════════════════════════════════════
 * LESSON 3 — Checks & Assertions
 * ═══════════════════════════════════════════════════════════════
 *
 * What you will learn:
 *   1. Checking HTTP status codes
 *   2. Extracting values from JSON response bodies with jsonPath
 *   3. Saving extracted values into the session for use in later requests
 *   4. Assertions — defining SLAs (e.g. 95th percentile < 500ms)
 *   5. Chaining requests using session-saved values (correlation)
 *
 * Target API: https://jsonplaceholder.typicode.com  (free, no auth)
 *
 * Run locally:
 *   mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim03_HttpChecks
 */
public class Sim03_HttpChecks extends Simulation {

  HttpProtocolBuilder httpProtocol =
      http
          .baseUrl("https://jsonplaceholder.typicode.com")
          .header("Accept", "application/json")
          .header("Content-Type", "application/json");

  FeederBuilder<String> userFeeder = csv("data/users.csv").circular();

  // ── Scenario: Create a post, then verify it can be fetched ────
  ScenarioBuilder createAndVerify =
      scenario("Create post and verify")
          .feed(userFeeder)

          // ── Step 1: POST to create a post ─────────────────────
          .exec(
              http("POST create post")
                  .post("/posts")
                  .body(
                      StringBody(
                          """
                          {
                            "title": "Load test post by #{username}",
                            "body":  "This post was created during a Gatling simulation.",
                            "userId": #{userId}
                          }
                          """))
                  // .check(...) validates the response and can extract values
                  .check(
                      // 1. Assert status is 201 Created
                      status().is(201),

                      // 2. Extract the "id" from the response JSON and save it
                      //    as "createdPostId" in the virtual user's session.
                      //    Format: jsonPath("$.field").saveAs("sessionKey")
                      jsonPath("$.id").saveAs("createdPostId"),

                      // 3. Assert the "title" field exists as a String in the response.
                      //    jsonPath returns JsonOfTypeMultipleFind — use .ofString()
                      //    to narrow the type, then .exists() to assert it's present.
                      jsonPath("$.title").ofString().exists()))

          .pause(1)

          // ── Step 2: GET the post we just "created" ─────────────
          // NOTE: JSONPlaceholder is a mock — it doesn't persist POSTs.
          //       POST returns id=101 always. We use it to demonstrate
          //       session correlation (reading a saved session value).
          .exec(
              http("GET created post")
                  // #{createdPostId} was saved in Step 1's .check(...)
                  .get("/posts/#{createdPostId}")
                  .check(
                      status().is(200),
                      // Verify the response body is not empty
                      bodyString().exists()))

          .pause(1)

          // ── Step 3: Conditional logic with doIf ───────────────
          // doIf evaluates a session expression; executes the block only if true.
          .doIf(session -> Integer.parseInt(session.getString("userId")) <= 5)
          .then(
              exec(
                  http("GET user details (only for userId <= 5)")
                      .get("/users/#{userId}")
                      .check(status().is(200))));

  // ── Global Assertions ─────────────────────────────────────────
  // These run AFTER the simulation finishes and define pass/fail SLAs.
  // The build will fail (exit code 1) if any assertion is violated.
  {
    setUp(createAndVerify.injectOpen(constantUsersPerSec(3).during(20)))
        .protocols(httpProtocol)
        .assertions(
            // 95th percentile response time should be under 3000ms
            global().responseTime().percentile(95).lt(3000),
            // Success rate must be >= 99%
            global().successfulRequests().percent().gte(99.0),
            // Maximum response time should not exceed 10000ms
            global().responseTime().max().lt(10000));
  }
}
