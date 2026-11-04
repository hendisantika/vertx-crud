package id.my.hendisantika.vertx_crud;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;

/**
 * Created by IntelliJ IDEA.
 * Project : vertx-crud
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 08/01/25
 * Time: 18.23
 * To change this template use File | Settings | File Templates.
 */
public class App {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MyVerticle())
      .onFailure(err -> {
        System.out.println("Deployment failed: " + err);
        vertx.close();
      });
  }

  public static class MyVerticle extends VerticleBase {

    private Pool client;

    @Override
    public Future<?> start() {
      // Configure MySQL connection (reactive client, connections are created lazily)
      MySQLConnectOptions connectOptions = new MySQLConnectOptions()
        .setHost(env("DB_HOST", "localhost"))
        .setPort(Integer.parseInt(env("DB_PORT", "3306")))
        .setDatabase(env("DB_NAME", "testdb"))
        .setUser(env("DB_USER", "root"))
        .setPassword(env("DB_PASSWORD", "root"));

      client = MySQLBuilder.pool()
        .with(new PoolOptions().setMaxSize(30))
        .connectingTo(connectOptions)
        .using(vertx)
        .build();

      // Create a router object
      Router router = Router.router(vertx);

      // Enable parsing of request bodies
      router.route().handler(BodyHandler.create());

      router.get("/users").handler(this::handleGetResource);
      router.get("/users/:username").handler(this::handleGetByIdResource);
      router.post("/users").handler(this::handlePostResource);
      router.delete("/users/:username").handler(this::handleDeleteByIdResource);
      router.put("/users/:username").handler(this::handleUpdateResource);

      // Start the HTTP server
      return vertx.createHttpServer()
        .requestHandler(router)
        .listen(Integer.parseInt(env("HTTP_PORT", "8080")))
        .onSuccess(server -> System.out.println("Server started on port " + server.actualPort()))
        .onFailure(err -> System.out.println("Server start failed: " + err));
    }

    private static String env(String key, String defaultValue) {
      String value = System.getenv(key);
      return value == null || value.isBlank() ? defaultValue : value;
    }

    // Handler for GET /users
    private void handleGetResource(RoutingContext ctx) {
      client.query("SELECT * FROM usert")
        .execute()
        .onSuccess(rows -> {
          JsonArray result = new JsonArray();
          rows.forEach(row -> result.add(row.toJson()));
          ctx.response()
            .putHeader("content-type", "application/json")
            .end(result.encode());
        })
        .onFailure(ctx::fail);
    }

    // Handler for GET /users/:username
    private void handleGetByIdResource(RoutingContext ctx) {
      String username = ctx.pathParam("username");

      client.preparedQuery("SELECT * FROM usert WHERE username = ?")
        .execute(Tuple.of(username))
        .onSuccess(rows -> {
          if (rows.size() > 0) {
            ctx.response()
              .putHeader("content-type", "application/json")
              .end(rows.iterator().next().toJson().encode());
          } else {
            ctx.response().setStatusCode(404).end("User not found");
          }
        })
        .onFailure(ctx::fail);
    }

    // Handler for DELETE /users/:username
    private void handleDeleteByIdResource(RoutingContext ctx) {
      String username = ctx.pathParam("username");

      if (username == null || username.isEmpty()) {
        ctx.response().setStatusCode(400).end("Username parameter is required");
        return;
      }

      client.preparedQuery("DELETE FROM usert WHERE username = ?")
        .execute(Tuple.of(username))
        .onSuccess(rows -> {
          if (rows.rowCount() > 0) {
            ctx.response().setStatusCode(200).end("User deleted successfully");
          } else {
            ctx.response().setStatusCode(404).end("User not found");
          }
        })
        .onFailure(ctx::fail);
    }

    // Handler for POST /users
    private void handlePostResource(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();

      if (body == null) {
        ctx.response().setStatusCode(400).end("JSON body is required");
        return;
      }

      System.out.println("Received JSON body: " + body.encodePrettily());

      String username = body.getString("username");
      String email = body.getString("email");

      if (username == null || username.isEmpty() || email == null || email.isEmpty()) {
        ctx.response().setStatusCode(400).end("Username and email are required");
        return;
      }

      client.preparedQuery("INSERT INTO usert (username, email) VALUES (?, ?)")
        .execute(Tuple.of(username, email))
        .onSuccess(rows -> ctx.response().setStatusCode(201).end("Username created"))
        .onFailure(ctx::fail);
    }

    // Handler for PUT /users/:username
    private void handleUpdateResource(RoutingContext ctx) {
      JsonObject body = ctx.body().asJsonObject();

      if (body == null) {
        ctx.response().setStatusCode(400).end("JSON body is required");
        return;
      }

      System.out.println("Received JSON body: " + body.encodePrettily());

      String username = ctx.pathParam("username");

      StringBuilder sql = new StringBuilder("UPDATE usert SET ");
      Tuple params = Tuple.tuple();
      boolean first = true;

      // Dynamically add fields to be updated
      if (body.containsKey("email")) {
        if (!first) sql.append(", ");
        sql.append("email = ?");
        params.addString(body.getString("email"));
        first = false;
      }

      // Ensure at least one field is being updated
      if (params.size() == 0) {
        ctx.response().setStatusCode(400).end("No fields to update");
        return;
      }

      sql.append(" WHERE username = ?");
      params.addString(username);

      client.preparedQuery(sql.toString())
        .execute(params)
        .onSuccess(rows -> {
          if (rows.rowCount() == 0) {
            ctx.response().setStatusCode(404).end("User not found");
          } else {
            ctx.response().setStatusCode(200).end("User updated successfully");
          }
        })
        .onFailure(ctx::fail);
    }

    @Override
    public Future<?> stop() {
      // Close the database client when stopping the verticle
      return client != null ? client.close() : Future.succeededFuture();
    }
  }
}
