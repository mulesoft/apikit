/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.console;

import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@ArtifactClassLoaderRunnerConfig
public class ConsoleRamlApiTestCase extends MuleArtifactFunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/console/console-with-router.xml";
  }

  @Test
  public void getRootRaml() {
    given().port(serverPort.getNumber())
        .expect()
        .statusCode(200)
        .body(is("#%RAML 1.0\n" +
            "title: Simple API\n" +
            "baseUri: http://www.google.com\n" +
            "types:\n" +
            "  Person:\n" +
            "    type: object\n" +
            "    additionalProperties: true\n" +
            "    properties:\n" +
            "      name:\n" +
            "        type: string\n" +
            "        required: true\n" +
            "/resources:\n" +
            "  get:\n" +
            "    responses:\n" +
            "      \"200\":\n" +
            "        body:\n" +
            "          application/json:\n" +
            "            type: any\n" +
            "          text/xml:\n" +
            "            type: any\n" +
            "/types-test:\n" +
            "  post:\n" +
            "    body:\n" +
            "      application/json:\n" +
            "        type: object\n" +
            "        additionalProperties: true\n" +
            "        properties:\n" +
            "          name:\n" +
            "            type: string\n" +
            "            required: true\n" +
            "    responses:\n" +
            "      \"200\": {}\n"))
        .when().get("/console/api?api");
  }

}
