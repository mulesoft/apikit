package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.port;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class ConfigurationTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPortDefault = new DynamicPort("serverPortDefault");
    @Rule
    public DynamicPort serverPortCustom = new DynamicPort("serverPortCustom");
    @Rule
    public DynamicPort serverPortNoConsole = new DynamicPort("serverPortNoConsole");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/config/configuration-test-config.xml";
    }

    @Test
    public void resourceOnDefaultConfig() throws Exception
    {
        RestAssured.port = serverPortDefault.getNumber();
        given()
            .expect().log().everything()
                .response().body(is("some resources"))
                .statusCode(200)
            .when().get("/default/resource");
    }

    @Test
    public void consoleOnDefaultConfig() throws Exception
    {
        RestAssured.port = serverPortDefault.getNumber();
        given().header("Accept", "text/html")
            .expect().log().everything()
                .response().body(allOf(containsString("<title>Api Console</title>"),
                                       containsString("src=\"http://localhost:" + port + "/default\"")))
                .header("Content-type", "text/html").statusCode(200)
            .when().get("/default/console/");
    }

    @Test
    public void resourceOnCustomConsolePathConfig() throws Exception
    {
        RestAssured.port = serverPortCustom.getNumber();
        given()
            .expect().log().everything()
                .response().body(is("some resources"))
                .statusCode(200)
            .when().get("/custom/resource");
    }

    @Test
    public void consoleOnCustomConsolePathConfig() throws Exception
    {
        RestAssured.port = serverPortCustom.getNumber();
        given().header("Accept", "text/html")
            .expect().log().everything()
                .response().body(allOf(containsString("<title>Api Console</title>"),
                                       containsString("src=\"http://localhost:" + port + "/custom\"")))
                .header("Content-type", "text/html").statusCode(200)
            .when().get("/custom/custom/");
    }
    @Test
    public void resourceOnNoConsoleConfig() throws Exception
    {
        RestAssured.port = serverPortNoConsole.getNumber();
        given()
            .expect().log().everything()
                .response().body(is("some resources"))
                .statusCode(200)
            .when().get("/no-console/resource");
    }

    @Test
    public void consoleOnNoConsoleConfig() throws Exception
    {
        RestAssured.port = serverPortNoConsole.getNumber();
        given().header("Accept", "text/html")
            .expect().log().everything()
                .response().body(containsString("resource not found"))
                .statusCode(404)
            .when().get("/no-console/console/");
    }

}
