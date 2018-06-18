/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.given;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;

public class RouterRemoteRamlTestCase extends AbstractMultiParserFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/router-remote-raml/remote-raml.xml";
  }

  @Test
  public void simpleRouting() throws Exception {
    given().header("Accept", "*/*")
        .expect()
        .response().body(is("hello"))
        .statusCode(200)
        .when().get("/api/resources");
  }
}
