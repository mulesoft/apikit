package org.mule.module.apikit.baker;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import java.io.File;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class BakerTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/baker/baker-flow-config.xml";
    }

    @Test
    @Ignore
    public void wait4() throws Exception
    {
        System.out.println("PORT: " + RestAssured.port);
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void postIssue() throws Exception
    {
        given()
                .multiPart("payload", "required by mule")
                .multiPart("name", "one")
                .multiPart("title", "being null")
                .multiPart("date", "2013-08-01")
                .multiPart("cover", "mulepolitan-1.jpg", getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/baker/mulepolitan-1.jpg"))
                .multiPart("hpub", "mulepolitan-1.zip", getClass().getClassLoader().getResourceAsStream("org/mule/module/apikit/baker/mulepolitan-1.zip"))
                .multiPart("info", "some info")
                .multiPart("product_id", "one")
                .contentType("multipart/form-data")
            .expect().statusCode(201)
                .header("Location", "http://localhost:" + serverPort.getValue() + "/api/issues/1")
                .body(is("")).header("Content-Length", "0")
            .when().post("/api/issues");
    }

}
