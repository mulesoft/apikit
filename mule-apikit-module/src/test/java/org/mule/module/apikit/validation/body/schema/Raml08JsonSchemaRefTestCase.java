package org.mule.module.apikit.validation.body.schema;

import static com.jayway.restassured.RestAssured.given;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

@ArtifactClassLoaderRunnerConfig
public class Raml08JsonSchemaRefTestCase extends MuleArtifactFunctionalTestCase
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
        return "org/mule/module/apikit/validation/body/schema/raml-08-with-schema-in-include.xml";
    }

    @Test
    public void JsonSchemaRef()
    {
        given().body("{\n" +
                     "\"response\":\n" +
                     "{ \"age\": 15 }\n" +
                     "}")
                .contentType("application/json")
                .expect()
                .statusCode(400)//.body(is("bad request"))
                .when().put("/api/jsonschema");
    }
}
