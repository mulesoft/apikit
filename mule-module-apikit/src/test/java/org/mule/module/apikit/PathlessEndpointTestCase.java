package org.mule.module.apikit;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.port;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

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
    public void consoleEmptyPath() throws Exception
    {
        RestAssured.port = serverPortEmptyPath.getNumber();
        console();
    }

    @Test
    public void consoleSlashPath() throws Exception
    {
        RestAssured.port = serverPortSlashPath.getNumber();
        console();
    }

    private void console()
    {
        given().header("Accept", "text/html")
            .expect()
                .response().body(allOf(containsString("<title>api:Console</title>"),
                                       containsString("src=\"http://localhost:" + port + "/\"")))
                .header("Content-type", "text/html").statusCode(200)
            .when().get("/console/index.html");
    }

}
