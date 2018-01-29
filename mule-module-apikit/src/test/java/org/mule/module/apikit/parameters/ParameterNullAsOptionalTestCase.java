package org.mule.module.apikit.parameters;

import com.jayway.restassured.RestAssured;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.junit4.rule.SystemProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.mule.module.apikit.HttpRestRequest.NULLABLE_AS_OPTIONAL_PROPERTY_NAME;

public class ParameterNullAsOptionalTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Rule
    public SystemProperty systemProperty = new SystemProperty(NULLABLE_AS_OPTIONAL_PROPERTY_NAME, "true");

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
        return "org/mule/module/apikit/parameters/parameter-null-as-optional-config.xml";
    }

    @Test
    public void validMultipartFormProvided() throws Exception
    {
        given().param("required", "value")
                .param("optional", (Object) null)
                .param("optionalarray", (Object) null)
                .expect().response().statusCode(200)
                .when().get("/api/resource");
    }

}