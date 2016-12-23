/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.schema;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import org.junit.Rule;
import org.junit.Test;

public class XxeAttackTestCase extends FunctionalTestCase
{
    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");
    @Rule
    public DynamicPort serverPort2 = new DynamicPort("serverPort2");
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
        return "org/mule/module/apikit/schema/xxe-attack-config.xml";
    }

    @Test
    public void xxeAttack() throws Exception
    {
        Response post = given().log().all()
                .body("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><!DOCTYPE foo [<!ENTITY xxead812 SYSTEM \"src/test/resources/org/mule/module/apikit/schema/twin-cam.yaml\"> ]><a>&xxead812;</a>")
                .contentType("application/xml")
                .expect().statusCode(400)
                .when().post("/api/test");
        String response = post.getBody().asString();
        assertThat(response, not(containsString("League Schema")));
    }

    @Test
    public void xxeAttack2() throws Exception
    {
        given().log().all()
                .body("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><!DOCTYPE xxeattack PUBLIC \"foo\" \"http://localhost:" + serverPort2.getValue() + "/\"><a>1</a>")
                .contentType("application/xml")
                .expect().statusCode(400)
                .when().post("/api/test");
    }
}
