package io.learn.gatling.simulations.http;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

/**
 * ═══════════════════════════════════════════════════════════════
 * LESSON 5 — Request Bodies, PUT, PATCH, DELETE
 * ═══════════════════════════════════════════════════════════════
 *
 * What you will learn:
 *   1. PUT   — full replacement of a resource
 *   2. PATCH — partial update of a resource
 *   3. DELETE — deleting a resource
 *   4. RawFileBody — loading request body from a file on disk
 *   5. ElFileBody  — same but with EL (#{variable}) substitution
 *   6. Programmatic request bodies — building JSON dynamically in Java
 *   7. Reading response headers with header()
 *
 * Target API: https://jsonplaceholder.typicode.com  (free, no auth)
 *
 * Run locally:
 *   mvn gatling:test -Dgatling.simulationClass=io.learn.gatling.simulations.http.Sim05_CRUD
 */
public class Sim05_CRUD extends Simulation {

  HttpProtocolBuilder httpProtocol =
      http
          .baseUrl("https://jsonplaceholder.typicode.com")
          .header("Content-Type", "application/json")
          .header("Accept", "application/json");

  FeederBuilder<String> userFeeder = csv("data/users.csv").circular();

  // ── Scenario: Full CRUD journey ───────────────────────────────
  ScenarioBuilder crudJourney =
      scenario("CRUD journey")
          .feed(userFeeder)

          // CREATE — POST with inline JSON body
          .exec(
              http("POST create post")
                  .post("/posts")
                  .body(
                      StringBody(
                          """
                          {
                            "title": "Title from #{username}",
                            "body":  "Some body text",
                            "userId": #{userId}
                          }
                          """))
                  .check(status().is(201), jsonPath("$.id").saveAs("postId")))
          .pause(1)

          // READ — GET using the saved postId
          // NOTE: JSONPlaceholder mock always returns id=101 from POST;
          //       we use /posts/1 to show a real GET after save.
          .exec(
              http("GET post")
                  .get("/posts/1")
                  .check(status().is(200)))
          .pause(1)

          // UPDATE — PUT replaces the entire resource
          .exec(
              http("PUT update post")
                  .put("/posts/1")
                  // ElFileBody reads bodies/update_post.json and substitutes #{variables}
                  .body(ElFileBody("bodies/update_post.json"))
                  .check(status().is(200), jsonPath("$.title").saveAs("updatedTitle")))
          .pause(1)

          // PARTIAL UPDATE — PATCH changes only specified fields
          .exec(
              http("PATCH title only")
                  .patch("/posts/1")
                  .body(StringBody("{\"title\": \"Patched by #{username}\"}"))
                  .check(status().is(200)))
          .pause(1)

          // DELETE
          .exec(
              http("DELETE post")
                  .delete("/posts/1")
                  .check(status().is(200)));

  {
    setUp(crudJourney.injectOpen(rampUsers(5).during(10))).protocols(httpProtocol);
  }
}
