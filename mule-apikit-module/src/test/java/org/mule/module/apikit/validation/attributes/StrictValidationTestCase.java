package org.mule.module.apikit.validation.attributes;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static com.jayway.restassured.RestAssured.given;

@ArtifactClassLoaderRunnerConfig
public class StrictValidationTestCase extends MuleArtifactFunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  public int getTestTimeoutSecs() {
    return 6000;
  }

  @Override
  protected void doSetUp() throws Exception {
    RestAssured.port = serverPort.getNumber();
    super.doSetUp();
  }

  @Override
  protected String getConfigResources() {
    return "org/mule/module/apikit/validation/strict-validation/strict-validation-config.xml";
  }

  @Test
  public void failWhenSendingNonDefinedQueryParam() throws Exception {
    given().queryParam("param2", "value")
        .expect()
        .statusCode(404)
        .when().get("/resources");
  }

  @Test
  public void failWhenSendingNonDefinedHeader() throws Exception {
    given().header("header2", "value")
        .expect()
        .statusCode(404)
        .when().get("/resources");
  }

  @Test
  public void sucessWhenSendingDefinedHeader() throws Exception {
    given().header("header1", "value")
        .expect()
        .statusCode(404)
        .when().get("/resources");
  }
}
