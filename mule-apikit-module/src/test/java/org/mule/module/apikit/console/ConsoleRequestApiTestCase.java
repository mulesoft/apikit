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

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;


@ArtifactClassLoaderRunnerConfig
public class ConsoleRequestApiTestCase extends MuleArtifactFunctionalTestCase {

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  protected String getConfigFile() {
    return "org/mule/module/apikit/console/api/console-api.xml";
  }

  @Test
  public void getOasApi() throws Exception {

    given().port(serverPort.getNumber())
        .expect()
        .statusCode(200)
        .body(is("{\n" +
            "  \"swagger\": \"2.0\",\n" +
            "  \"info\": {\n" +
            "    \"version\": \"1.0.9-abcd\",\n" +
            "    \"title\": \"Swagger Sample API\",\n" +
            "    \"description\": \"A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification\",\n"
            +
            "    \"termsOfService\": \"http://swagger.io/terms/\",\n" +
            "    \"contact\": {\n" +
            "      \"name\": \"Swagger API Team\",\n" +
            "      \"url\": \"http://swagger.io\"\n" +
            "    },\n" +
            "    \"license\": {\n" +
            "      \"name\": \"Creative Commons 4.0 International\",\n" +
            "      \"url\": \"http://creativecommons.org/licenses/by/4.0/\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"host\": \"my.api.com\",\n" +
            "  \"basePath\": \"/v1\",\n" +
            "  \"schemes\": [\n" +
            "    \"http\",\n" +
            "    \"https\"\n" +
            "  ],\n" +
            "  \"consumes\": [\n" +
            "    \"application/json\"\n" +
            "  ],\n" +
            "  \"produces\": [\n" +
            "    \"application/json\",\n" +
            "    \"application/xml\"\n" +
            "  ],\n" +
            "  \"paths\": {\n" +
            "    \"/pets/{petId}\": {\n" +
            "      \"get\": {\n" +
            "        \"description\": \"Returns a pet based on ID\",\n" +
            "        \"summary\": \"Find pet by ID\",\n" +
            "        \"operationId\": \"getPetsById\",\n" +
            "        \"produces\": [\n" +
            "          \"application/json\",\n" +
            "          \"text/html\"\n" +
            "        ],\n" +
            "        \"parameters\": [\n" +
            "          {\n" +
            "            \"name\": \"petId\",\n" +
            "            \"in\": \"path\",\n" +
            "            \"description\": \"ID of pet that needs to be fetched\",\n" +
            "            \"required\": true,\n" +
            "            \"type\": \"array\",\n" +
            "            \"collectionFormat\": \"csv\",\n" +
            "            \"items\": {\n" +
            "              \"type\": \"string\"\n" +
            "            }\n" +
            "          }\n" +
            "        ],\n" +
            "        \"responses\": {\n" +
            "          \"200\": {\n" +
            "            \"description\": \"pet response\",\n" +
            "            \"schema\": {}\n" +
            "          },\n" +
            "          \"default\": {\n" +
            "            \"description\": \"error payload\",\n" +
            "            \"schema\": {}\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"definitions\": {\n" +
            "    \"Pet\": {\n" +
            "      \"required\": [\n" +
            "        \"name\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"name\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"tag\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"object\"\n" +
            "    },\n" +
            "    \"ErrorModel\": {\n" +
            "      \"required\": [\n" +
            "        \"code\",\n" +
            "        \"message\"\n" +
            "      ],\n" +
            "      \"properties\": {\n" +
            "        \"code\": {\n" +
            "          \"type\": \"integer\",\n" +
            "          \"format\": \"int32\"\n" +
            "        },\n" +
            "        \"message\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"type\": \"object\"\n" +
            "    }\n" +
            "  }\n" +
            "}\n"))
        .when().get("/console-oas/org/mule/module/apikit/console/api/?api");
  }

  @Test
  public void getRaml10Api() throws Exception {
    given().port(serverPort.getNumber())
        .expect()
        .statusCode(200)
        .body(is("#%RAML 1.0\n" +
            "title: hola\n" +
            "/top:\n" +
            "  type:\n" +
            "    library1.foo:\n" +
            "      foo: description\n" +
            "  get:\n" +
            "    description: get something\n" +
            "  (library1.bar): hi\n" +
            "uses:\n" +
            "  library1: library.raml\n"))
        .when().get("/console-raml-10/org/mule/module/apikit/console/api/?api");
  }

  @Test
  public void getRaml10Library() throws Exception {
    given().port(serverPort.getNumber())
        .expect()
        .statusCode(200)
        .body(is("#%RAML 1.0 Library\n" +
            "resourceTypes:\n" +
            "   foo:\n" +
            "    <<foo>>: This is a collection <<foo | !singularize>> and else\n" +
            "annotationTypes:\n" +
            "   bar:\n" +
            "     type: string\n"))
        .when().get("/console-raml-10/org/mule/module/apikit/console/api/library.raml");
  }

  @Test
  public void getRaml08() throws Exception {
    given().port(serverPort.getNumber())
        .expect()
        .statusCode(200)
        .body(is("#%RAML 0.8\n" +
            "title: emtpy body\n" +
            "/bug:\n" +
            "  post:\n" +
            "    body: {}\n"))
        .when().get("/console-raml-08/org/mule/module/apikit/console/api/?api");
  }

}
