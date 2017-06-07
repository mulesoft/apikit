/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import com.jayway.restassured.RestAssured;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@ArtifactClassLoaderRunnerConfig
public class Schema08TestCase extends MuleArtifactFunctionalTestCase
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
        return "org/mule/module/apikit/validation/body/schema/schema-config.xml";
        //TODO change raml to use the json schema autogenerator property (APIKIT-783)
    }

    @Test
    public void putValidJson() throws Exception
    {
        given().body("{\"username\":\"gbs\",\"firstName\":\"george\",\"lastName\":\"bernard shaw\",\"emailAddresses\":[\"gbs@ie\"]}")
                .contentType("application/json")
            .expect()
                .statusCode(201)
                .body(is("hello"))
            .when().put("/api/currentuser");
    }

    @Test
    public void putInvalidJson() throws Exception
    {
        given().body("{\"username\":\"gbs\",\"firstName\":\"george\",\"lastName\":\"bernard shaw\"}")
                .contentType("application/json")
                .expect()
                .statusCode(400)//.body(is("bad request"))
                .when().put("/api/currentuser");
    }

    @Test
    public void putValidXml() throws Exception
    {
        given().body("<user xmlns=\"http://mulesoft.org/schemas/sample\" username=\"gbs\" firstName=\"george\" lastName=\"bernard shaw\">" +
                     "<email-addresses><email-address>gbs@ie</email-address></email-addresses></user>")
                .contentType("text/xml")
            .expect()
                .statusCode(201)
                .body(is("hello"))
            .when().put("/api/currentuser");
    }

    @Test
    public void putInvalidXml() throws Exception
    {
        given().body("<user xmlns=\"http://mulesoft.org/schemas/sample\" username=\"gbs\" firstName=\"george\" lastName=\"bernard shaw\">" +
                     "<email-addresses></email-addresses></user>")
                .contentType("text/xml")
            .expect()
                .statusCode(400)
            .when().put("/api/currentuser");
    }
}
