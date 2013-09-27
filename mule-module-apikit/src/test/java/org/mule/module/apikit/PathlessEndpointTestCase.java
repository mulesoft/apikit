package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.port;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class PathlessEndpointTestCase extends FunctionalTestCase
{
    @Rule
    public DynamicPort serverPortPathless = new DynamicPort("serverPortPathless");
    @Rule
    public DynamicPort serverPortEmptyPath = new DynamicPort("serverPortEmptyPath");
    @Rule
    public DynamicPort serverPortSlashPath = new DynamicPort("serverPortSlashPath");
    @Rule
    public DynamicPort serverPortAddressSlashPath = new DynamicPort("serverPortAddressSlashPath");

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/pathless/pathless-config.xml";
    }

    @Test
    public void consolePathless() throws Exception
    {
        RestAssured.port = serverPortPathless.getNumber();
        console();
    }

    @Test
    public void ramlPathless() throws Exception
    {
        RestAssured.port = serverPortPathless.getNumber();
        raml();
    }

    @Test
    public void consoleEmptyPath() throws Exception
    {
        RestAssured.port = serverPortEmptyPath.getNumber();
        console();
    }

    @Test
    public void ramlEmptyPath() throws Exception
    {
        RestAssured.port = serverPortEmptyPath.getNumber();
        raml();
    }

    @Test
    public void consoleSlashPath() throws Exception
    {
        RestAssured.port = serverPortSlashPath.getNumber();
        console();
    }

    @Test
    public void ramlSlashPath() throws Exception
    {
        RestAssured.port = serverPortSlashPath.getNumber();
        raml();
    }

    @Test
    public void consoleAddressSlashPath() throws Exception
    {
        RestAssured.port = serverPortAddressSlashPath.getNumber();
        console("api");
    }

    @Test
    public void ramlAddressSlashPath() throws Exception
    {
        RestAssured.port = serverPortAddressSlashPath.getNumber();
        raml("api");
    }

    private void console()
    {
        console("");
    }

    private void console(String path)
    {
        given().header("Accept", "text/html")
            .expect()
                .response().body(allOf(containsString("<title>api:Console</title>"),
                                       containsString("src=\"http://localhost:" + port + "/" + path)))
                .header("Content-type", "text/html").statusCode(200)
            .when().get(path + "/console/index.html");
    }

    private void raml()
    {
        raml("");
    }

    private void raml(String path)
    {
        given().header("Accept", "application/raml+yaml")
            .expect()
                .response().body(allOf(containsString("title"),
                                       containsString("Endpoint API")))
                .statusCode(200)
            .when().get(path + "/");
    }

}
