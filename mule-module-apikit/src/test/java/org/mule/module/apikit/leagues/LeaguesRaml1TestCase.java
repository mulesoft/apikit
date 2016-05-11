/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.leagues;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.port;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mule.module.apikit.Configuration.APPLICATION_RAML;
import static org.mule.module.apikit.util.RegexMatcher.matchesPattern;

import org.junit.Ignore;
import org.junit.Test;

public class LeaguesRaml1TestCase extends LeaguesTestCase
{

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/leagues/leagues-base-flow-config.xml, org/mule/module/apikit/leagues/leagues-raml1-flow-config.xml";
    }

    @Test
    @Ignore //not yet supported
    public void putMultiPartFormData() throws Exception
    {
        given().multiPart("description", "Barcelona Badge")
                .multiPart("image", "bbva.jpg", this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/leagues/bbva.jpg"))
                .expect().statusCode(200)
                .body("upload", is("OK"))
                .when().put("/api/leagues/liga-bbva/badge");
    }

    @Test
    public void getRamlFromApi() throws Exception
    {
        given().header("Accept", APPLICATION_RAML)
            .expect()
                .response().body(containsString("baseUri: http://localhost:" + port + "/api"))
                .header("Content-type", APPLICATION_RAML).statusCode(200)
            .when().get("/api/console/org/mule/module/apikit/leagues/?raml");
    }

    @Test
    public void getRamlFromEmbeddedConsole() throws Exception
    {
        given().header("Accept", APPLICATION_RAML)
                .expect()
                .response().body(containsString("baseUri: http://localhost:" + port + "/api"))
                .header("Content-type", APPLICATION_RAML).statusCode(200)
                .when().get("/api/console/org/mule/module/apikit/leagues/?raml");
    }

    //@Test
    public void getRamlWrongContentType() throws Exception
    {
        // DOES NOT APPLY TO PARSER V2
        //  as raml is hosted in a different path
    }

    @Test
    public void console() throws Exception
    {
        given().header("Accept", "text/html")
            .expect()
                .response().body(allOf(containsString("<title>API Console</title>"),
                                       matchesPattern("(?s).*src=\"org/mule/module/apikit/leagues/\\?raml\".*")))
                .header("Content-type", "text/html").statusCode(200)
            .when().get("/api/console/index.html");
    }

    @Test
    public void consoleDirectory() throws Exception
    {
        given().header("Accept", "text/html")
            .expect()
                .response().body(allOf(containsString("<title>API Console</title>"),
                                       matchesPattern("(?s).*src=\"org/mule/module/apikit/leagues/\\?raml\".*")))
                .header("Content-type", "text/html").statusCode(200)
            .when().get("/api/console/");
    }

}
