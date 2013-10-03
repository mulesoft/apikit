package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class ParametersTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/parameters/parameters-config.xml";
    }

    @Test
    public void requiredQueryParamNotProvided() throws Exception
    {
        given()
                .expect().response().statusCode(400)
                .when().get("/api/resources");
    }

    @Test
    public void requiredQueryParamProvided() throws Exception
    {
        given()
                .expect().response().statusCode(200)
                .when().get("/api/resources?first=I");
    }

    @Test
    public void invalidBooleanQueryParamProvided() throws Exception
    {
        given()
                .expect().response().statusCode(400)
                .when().get("/api/resources?first=I&third=yes");
    }

    @Test
    public void raml() throws Exception
    {
        given().header("Accept", "application/raml+yaml")
            .expect()
                .response().body(allOf(containsString("baseUri"),
                                       containsString("http://localhost:" + serverPort.getNumber() + "/api")))
                .statusCode(200).when().get("/api");
    }

}
