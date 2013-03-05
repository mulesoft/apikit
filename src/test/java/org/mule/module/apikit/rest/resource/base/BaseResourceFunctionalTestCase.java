
package org.mule.module.apikit.rest.resource.base;

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
        return 6000;
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
        return "org/mule/module/apikit/rest/resource/base/base-functional-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    public void updateNotSupported() throws Exception
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
    public void createNotSupported() throws Exception
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
    public void retrieve() throws Exception
    {
        given().header("Accept", "text/html").expect().response().statusCode(200).when().get("/api");
        given().header("Accept", "text/html").expect().response().statusCode(200).when().get("/api/");
    }

    @Test
    public void retrieveUnauthorized() throws Exception
    {
        given().header("Accept", "text/html")
            .expect()
            .response()
            .header("Content-Length", "0")
            .statusCode(401)
            .when()
            .get("/protectedapi");
        given().header("Accept", "text/html")
            .expect()
            .response()
            .header("Content-Length", "0")
            .statusCode(401)
            .when()
            .get("/protectedapi/");
    }

    @Test
    public void exists() throws Exception
    {
        given().expect().response().statusCode(200).header("Content-Length", "0").when().head("/api");
        given().expect().response().statusCode(200).header("Content-Length", "0").when().head("/api/");
    }

    @Test
    public void retreiveUnsupportedContentTypes() throws Exception
    {
        given().header("Accept", "application/json").expect().response().statusCode(406).when().get("/api");
        given().header("Accept", "text/pain").expect().response().statusCode(406).when().get("/api");
    }

}
