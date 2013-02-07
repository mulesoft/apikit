
package org.mule.module.wsapi.rest;

import static com.jayway.restassured.RestAssured.given;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import org.junit.Rule;
import org.junit.Test;

public class ServiceFunctionalTestCase extends FunctionalTestCase
{
    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    protected void doSetUp() throws Exception
    {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigResources()
    {
        return "org/mule/module/wsapi/rest/service-config.xml, org/mule/module/wsapi/test-flows-config.xml";
    }

    @Test
    public void getBookmarkURIJSON() throws Exception
    {
        given().contentType(ContentType.JSON).expect().response().statusCode(404).when().get("/api");
    }

}
