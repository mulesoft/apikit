
package org.mule.module.apikit.rest.resource;

import static com.jayway.restassured.RestAssured.given;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

public class BaseResourceFunctionalTestCase extends FunctionalTestCase
{

    @Override
    public int getTestTimeoutSecs()
    {
        return 600;
    }

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/rest/service-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    // Base URI

    @Test
    public void putNotSupported() throws Exception
    {
        given().expect()
            .response()
            .statusCode(405)
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .when()
            .put("/api");
        given().expect()
            .response()
            .statusCode(405)
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .when()
            .put("/api/");
    }

    @Test
    public void postNotSupported() throws Exception
    {
        given().expect()
            .response()
            .statusCode(405)
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .when()
            .post("/api");
        given().expect()
            .response()
            .statusCode(405)
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .when()
            .post("/api/");
    }

    @Test
    public void deleteNotSupported() throws Exception
    {
        given().expect()
            .response()
            .statusCode(405)
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .when()
            .delete("/api");
        given().expect()
            .response()
            .statusCode(405)
            .header("Allow", Matchers.equalToIgnoringCase("GET"))
            .when()
            .delete("/api/");
    }

    @Test
    public void headSupported() throws Exception
    {
        given().expect()
            .response()
            .statusCode(200)
            .body(Matchers.equalToIgnoringCase(""))
            .when()
            .head("/api");
        given().expect()
            .response()
            .statusCode(200)
            .body(Matchers.equalToIgnoringCase(""))
            .when()
            .head("/api/");
    }

    @Test
    public void getUnsupportedContentTypes() throws Exception
    {
        given().header("Accept", "application/json").expect().response().statusCode(406).when().get("/api");
        given().header("Accept", "text/pain").expect().response().statusCode(406).when().get("/api");
    }

}
