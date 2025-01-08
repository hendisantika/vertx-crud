package id.my.hendisantika.vertx_crud;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

public class App extends AbstractVerticle {

  private JDBCClient client;

  @Override
  public void start(Promise<Void> startPromise) {
    // Configure MySQL connection
    client = JDBCClient.create(vertx, new JsonObject()
      .put("url", "jdbc:mysql://localhost:3306/testdb")
      .put("driver_class", "com.mysql.cj.jdbc.Driver")
      .put("user", "root")
      .put("password", "root")
      .put("max_pool_size", 30));

    // Create a router object
    Router router = Router.router(vertx);

    // Enable parsing of request bodies
    router.route().handler(BodyHandler.create());

    // Define GET endpoint
    router.get("/users").handler(this::handleGetResource);
    router.get("/users/:username").handler(this::handleGetByIdResource);
    // Define POST endpoint
    router.post("/users").handler(this::handlePostResource);

    router.delete("/users/:username").handler(this::handleDeleteByIdResource);
    router.put("/users/:username").handler(this::handleUpdateResource);


    // Start the HTTP server
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080, ar -> {
        if (ar.succeeded()) {
          System.out.println("Server started on port 8080");
          startPromise.complete();
        } else {
          System.out.println("Server start failed: " + ar.cause());
          startPromise.fail(ar.cause());
        }
      });
  }

  // Handler for GET /api/resource
  private void handleGetResource(RoutingContext ctx) {
    client.query("SELECT * FROM usert", res -> {
      if (res.succeeded()) {
        ctx.response()
          .putHeader("content-type", "application/json")
          .end(res.result().toJson().encode());
      } else {
        ctx.fail(500);
      }
    });
  }

  private void handleGetByIdResource(RoutingContext ctx) {
    String username = ctx.request().getParam("username");

    // Check if username parameter is provided


    // Prepare SQL query with parameter
    String sql = "SELECT * FROM usert WHERE username = ?";
    JsonArray params = new JsonArray().add(username);

    // Execute the SQL query
    client.queryWithParams(sql, params, res -> {
      if (res.succeeded()) {
        // Retrieve the query result
        if (res.result().getNumRows() > 0) {
          JsonObject user = res.result().getRows().get(0);
          ctx.response()
            .putHeader("content-type", "application/json")
            .end(user.encode());
        } else {
          // Handle case where no user is found with the given username
          ctx.response().setStatusCode(404).end("User not found");
        }
      } else {
        // Handle query execution failure
        ctx.fail(500);
      }
    });
  }


  private void handleDeleteByIdResource(RoutingContext ctx) {
    String username = ctx.request().getParam("username");

    // Check if username parameter is provided
    if (username == null || username.isEmpty()) {
      ctx.response().setStatusCode(400).end("Username parameter is required");
      return;
    }

    // Prepare SQL query with parameter
    String sql = "DELETE FROM usert WHERE username = ?";
    JsonArray params = new JsonArray().add(username);

    // Execute the SQL DELETE query
    client.updateWithParams(sql, params, res -> {
      if (res.succeeded()) {
        if (res.result().getUpdated() > 0) {
          ctx.response().setStatusCode(200).end("User deleted successfully");
        } else {
          ctx.response().setStatusCode(404).end("User not found");
        }
      } else {
        // Handle query execution failure
        ctx.fail(500);
      }
    });
  }
}
