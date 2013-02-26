
package org.mule.module.apikit.rest.resource.pojo;

import static com.jayway.restassured.RestAssured.expect;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class PojoResourceFunctionalTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/rest/resource/pojo/pojo-functional-config.xml, org/mule/module/apikit/rest/resource/pojo/pojo-functional-flows.xml";
    }

    @Test
    public void retrieve() throws Exception
    {
        expect().response().statusCode(200).body(containsString("Liga BBVA")).when().get("/api/sponsor");
    }

    // @Test
    // public void update() throws Exception
    // {
    // given().body("Premier League").expect().response().statusCode(200).when().put("/api/league");
    // }
    //
    //
    // @Test
    // public void deleteNotAllowed() throws Exception
    // {
    // expect().response().statusCode(405).header("Content-Length", "0").when().delete("/api/league");
    // }
    //
    // @Test
    // public void exists() throws Exception
    // {
    // expect().response().statusCode(200).header("Content-Length", "0").when().head("/api/league");
    // }
    //
}
