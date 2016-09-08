/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.leagues;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Ignore;
import org.junit.Test;

public class LeaguesHttpListenerTestCase extends LeaguesTestCase
{

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/apikit/leagues/leagues-base-flow-config.xml, org/mule/module/apikit/leagues/leagues-http-listener-flow-config.xml";
    }

    @Test @Ignore //MULE-8142
    public void putMultiPartFormData() throws Exception
    {
        given().multiPart("description", "Barcelona Badge")
                .multiPart("image", "bbva.jpg", this.getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/leagues/bbva.jpg"))
                .expect().statusCode(200)
                .body("upload", is("OK"))
                .header("non-blocking", isNonBlocking())
                .when().put("/api/leagues/liga-bbva/badge");
    }

}
