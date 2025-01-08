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
}
