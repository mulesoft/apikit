/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.transform;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class JsonToStringTransformationTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    protected String getConfigFile()
    {
        return "org/mule/module/apikit/transform/test-config.xml";
    }

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    /*
    * This test was created due to SE-7162. Please refer to it for more info
    * */
    @Test
    public void jsonToStringTransformation() throws Exception
    {
        // check that the non-apikit flow works
        given().body("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><entity/>")
                .expect().response()
                    .statusCode(200)
                    .body(is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><entity/>"))
                .when().post("/");

        // call flow that will require a transformation
            given().expect().response()
                .statusCode(200)
                    .header("Content-Type", "application/xml")
                    .body(is("{  \"ping\": \"ok\" }"))
                .when().get("/api/ping");

        // check non-apikit flow again to check that is still working
        given().body("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><entity/>")
                .expect().response()
                    .statusCode(200)
                    .body(is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><entity/>"))
                .when().post("/");
    }

}
