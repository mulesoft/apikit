
package org.mule.module.wsapi.rest;

import static com.jayway.restassured.RestAssured.given;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import org.junit.Rule;
import org.junit.Test;

public class ServiceFunctionalTestCase extends FunctionalTestCase
{
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
        return "org/mule/module/wsapi/rest/service-config.xml, org/mule/module/wsapi/test-flows-config.xml";
    }

    // Base URI

    @Test
    public void baseUriPutNotAllowed() throws Exception
    {
        given().expect().response().statusCode(405).when().put("/api");
        // TODO Update once "GET" is supported to assert existence of "Allow" header
        // given().expect().response().statusCode(405).header("Allow", "GET").when().put("/api");
    }

    @Test
    public void baseUriPostNotAllowed() throws Exception
    {
        given().expect().response().statusCode(405).when().post("/api");
        // TODO Update once "GET" is supported to assert existence of "Allow" header
        // given().expect().response().statusCode(405).header("Allow", "GET").when().post("/api");
    }

    @Test
    public void baseUriDeleteNotAllowed() throws Exception
    {
        given().expect().response().statusCode(405).when().delete("/api");
        // TODO Update once "GET" is supported to assert existence of "Allow" header
        // given().expect().response().statusCode(405).header("Allow", "GET").when().delete("/api");
    }

    @Test
    public void baseUriHead() throws Exception
    {
        given().contentType(ContentType.JSON).expect().response().statusCode(405).when().get("/api");
        given().contentType(ContentType.XML).expect().response().statusCode(405).when().get("/api");
        given().contentType(ContentType.TEXT).expect().response().statusCode(405).when().get("/api");
        given().contentType(ContentType.HTML).expect().response().statusCode(405).when().get("/api");
    }

    @Test
    public void baseUriGet() throws Exception
    {
        // No support for 'index' representation currently for any request representation.
        given().contentType(ContentType.JSON).expect().response().statusCode(405).when().get("/api");
        given().contentType(ContentType.XML).expect().response().statusCode(405).when().get("/api");
        given().contentType(ContentType.TEXT).expect().response().statusCode(405).when().get("/api");
        given().contentType(ContentType.HTML).expect().response().statusCode(405).when().get("/api");
    }

    @Test
    public void baseUriGetSwagger() throws Exception
    {
        given().contentType("application/swagger+json")
            .expect()
            .response()
            .statusCode(405)
            .when()
            .get("/api");
    }

}
