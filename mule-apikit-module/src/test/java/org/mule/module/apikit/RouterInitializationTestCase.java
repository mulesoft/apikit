package org.mule.module.apikit;

import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class RouterInitializationTestCase extends AbstractRouterInitializationTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/raml-handler/" + path;
  }

  @Test
  public void simpleRouting() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is("hello"))
        .statusCode(200)
        .when().get("/api/test");
  }
}
