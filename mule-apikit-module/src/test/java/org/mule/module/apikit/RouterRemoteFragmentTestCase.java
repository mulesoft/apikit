/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class RouterRemoteFragmentTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/remote-fragment/remote-fragment.xml";
  }

  @Test
  public void simpleRouting() throws Exception {
    given().header("Accept", "*/*")
        .queryParam("code", "ABC-123")
        .queryParam("size", "medium")
        .expect()
        .response().body(is("hello"))
        .statusCode(200)
        .when().get("/api/test");
  }
}
