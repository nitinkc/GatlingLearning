package io.learn.gatling.simulations.http;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

/**
 * ═══════════════════════════════════════════════════════════════
 * LESSON 2 — HTTP with Feeders (Data-Driven Simulations)
 * ═══════════════════════════════════════════════════════════════
 *
 * What you will learn:
 *   1. What a Feeder is — injecting dynamic data per virtual user
 *   2. CSV feeders  → src/test/resources/data/users.csv
 *   3. JSON feeders → src/test/resources/data/posts.json
 *   4. Referencing feeder values inside requests using #{variable}
 *   5. Feeder strategies: queue, random, circular, shuffle
 *   6. Ramp injection model (rampUsersPerSec)
 *
 * Target API: https://jsonplaceholder.typicode.com  (free, no auth)
 *
 * Run locally:
 *   mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim02_HttpWithFeeders
 */
public class Sim02_HttpWithFeeders extends Simulation {

  HttpProtocolBuilder httpProtocol =
      http
          .baseUrl("https://jsonplaceholder.typicode.com")
          .header("Content-Type", "application/json")
          .header("Accept", "application/json");

  // ── Feeder 1: CSV ─────────────────────────────────────────────
  // Reads src/test/resources/data/users.csv
  // Each row becomes a Map<String,Object> injected into the session.
  // Columns: userId, username
  //
  // Strategy options (call after csv(...)):
  //   .queue()    — default; feeds rows in order, fails if runs out
  //   .circular() — wraps around when all rows are consumed ✓ best for load tests
  //   .random()   — picks a random row each time
  //   .shuffle()  — shuffles once, then reads in order
  FeederBuilder<String> userFeeder = csv("data/users.csv").circular();

  // ── Feeder 2: JSON array ───────────────────────────────────────
  // Reads src/test/resources/data/posts.json
  // Each element in the JSON array becomes a Map injected into the session.
  // Fields: postId, title, body
  FeederBuilder<Object> postFeeder = jsonFile("data/posts.json").random();

  // ── Scenario 1: Fetch user's posts ────────────────────────────
  // #{userId} is the Gatling Expression Language (EL) — it reads
  // the value of "userId" from the current session (put there by the feeder).
  ScenarioBuilder fetchUserPosts =
      scenario("Fetch user posts")
          .feed(userFeeder) // Inject one row from the CSV into this virtual user's session
          .exec(
              http("GET posts by user")
                  // #{userId} will be replaced with the value from the CSV row
                  .get("/posts?userId=#{userId}"))
          .pause(1)
          .exec(
              http("GET user profile")
                  .get("/users/#{userId}"));

  // ── Scenario 2: Create a post from JSON feeder ────────────────
  ScenarioBuilder createPost =
      scenario("Create post from feeder")
          .feed(postFeeder) // Inject one element from the JSON array
          .exec(
              http("POST create post")
                  .post("/posts")
                  // StringBody uses EL to substitute #{title} and #{body} from the session
                  .body(
                      StringBody(
                          """
                          {
                            "title": "#{title}",
                            "body":  "#{body}",
                            "userId": 1
                          }
                          """)));

  {
    setUp(
            // Ramp from 1 user/sec up to 5 users/sec over 10 seconds, then hold for 10s
            fetchUserPosts
                .injectOpen(rampUsersPerSec(1).to(5).during(10), constantUsersPerSec(5).during(10)),
            createPost
                .injectOpen(constantUsersPerSec(2).during(15)))
        .protocols(httpProtocol);
  }
}
