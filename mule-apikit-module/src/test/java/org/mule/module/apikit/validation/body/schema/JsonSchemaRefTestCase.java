/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema;

import static com.jayway.restassured.RestAssured.given;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import com.jayway.restassured.RestAssured;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@ArtifactClassLoaderRunnerConfig
public class JsonSchemaRefTestCase extends MuleArtifactFunctionalTestCase
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
    protected String getConfigFile()
    {
        return "org/mule/module/apikit/validation/body/schema/json-schema-ref-config.xml";
    }

    @Test
    public void validSchema() throws Exception
    {
        given()
                .body("{\"/\": {\"fstype\": \"ext4\", \"device\": \"/dev/hda\"}, \"swap\": {\"fstype\": \"ext4\", \"device\": \"/dev/hdb\"}}")
                .contentType("application/json")
                .expect().statusCode(200)
                .when().put("/api/resource");
    }

    @Test
    public void invalidSchema() throws Exception
    {
        given()
                .body("{\"/\": {\"fstype\": \"ext4\", \"device\": \"/dev/hda\"}, \"swap\": {\"fstype\": \"ext4\"}}")
                .contentType("application/json")
                .expect().statusCode(400)
                .when().put("/api/resource");
    }

    @Test
    public void validGlobalSchema() throws Exception
    {
        given()
                .body("{\"/\": {\"fstype\": \"ext4\", \"device\": \"/dev/hda\"}, \"swap\": {\"fstype\": \"ext4\", \"device\": \"/dev/hdb\"}}")
                .contentType("application/json")
                .expect().statusCode(200)
                .when().put("/api/global");
    }

    @Test
    public void invalidGlobalSchema() throws Exception
    {
        given()
                .body("{\"/\": {\"device\": \"/dev/hda\"}, \"swap\": {\"fstype\": \"ext4\", \"device\": \"/dev/hdb\"}}")
                .contentType("application/json")
                .expect().statusCode(400)
                .when().put("/api/global");
    }

    @Test
    public void validGlobalIncludeSchema() throws Exception
    {
        given()
                .body("{\"/\": {\"fstype\": \"ext4\", \"device\": \"/dev/hda\"}, \"swap\": {\"fstype\": \"ext4\", \"device\": \"/dev/hdb\"}}")
                .contentType("application/json")
                .expect().statusCode(200)
                .when().put("/api/global-include");
    }

    @Test
    public void invalidGlobalIncludeSchema() throws Exception
    {
        given()
                .body("{\"/\": {\"device\": \"/dev/hda\"}, \"swap\": {\"fstype\": \"ext4\", \"device\": \"/dev/hdb\"}}")
                .contentType("application/json")
                .expect().statusCode(400)
                .when().put("/api/global-include");
    }

}
