package id.my.hendisantika.vertx_crud;

import io.vertx.core.Vertx;

public class App {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MyVerticle());
  }

}
