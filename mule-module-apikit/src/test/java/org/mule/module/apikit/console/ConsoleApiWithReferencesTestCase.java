/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import java.util.Collections;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.port;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mule.module.apikit.Configuration.APPLICATION_RAML;

public class ConsoleApiWithReferencesTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/console/console-api-with-references.xml";
    }

    @Test
    public void console()
    {
        given().header("Accept", "text/html")
                .expect()
                .response().body(containsString("<title>API Console</title>"))
                .header("Content-type", "text/html").statusCode(200)
                .when().get("console/index.html");
    }

    @Test
    public void consoleResource()
    {
        given().header("Accept", "text/css")
                .expect()
                .response().body(containsString(".CodeMirror"))
                .header("Content-type", "text/css").statusCode(200)
                .when().get("console/styles/api-console-light-theme.css");
    }


    @Test
    public void apiResources()
    {
        String[] apiResources = new String[]{"address.raml","company-example.json","partner.raml","data-type.raml","library.raml","company.raml"};

        for (String resource: apiResources){
            given().header("Accept", "*/*")
                    .expect().response().statusCode(200)
                    .when().get("console/references/" + resource);
        }
    }

    @Test
    public void consoleEscapeNotFoundResponses()
    {
        given().header("Accept", "text/html")
                .expect()
                .response().body(containsString("/&lt;script&gt;alert('hello')%3B&lt;/script&gt;.html"))
                .when().get("console/<script>alert('hello')%3B</script>.html");
    }
}
