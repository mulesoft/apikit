
package org.mule.module.apikit.rest.resource;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class DocumentResourceFunctionalTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/rest/resource/document-resource-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    public void documentNotFound() throws Exception
    {
        expect().response().statusCode(404).header("Content-Length", "0").when().head("/api/league1");
    }

    @Test
    public void noCreateOnDocument() throws Exception
    {
        expect().response().statusCode(405).header("Content-Length", "0").when().post("/api/league");
    }

    @Test
    public void retrieveOnDocument() throws Exception
    {
        expect().response().statusCode(200).body(containsString("Liga BBVA")).when().get("/api/league");
    }

    @Test
    public void retrieveOnNestedDocument() throws Exception
    {
        expect().response().statusCode(200).body(containsString("Royal")).when().get("/api/league/association");
    }

    @Test
    public void updateOnDocument() throws Exception
    {
        given().body("Premier League").expect().response().statusCode(200).when().put("/api/league");
    }

    @Test
    public void updateOnNestedDocument() throws Exception
    {
        given().body("AFA").expect().response().statusCode(200).when().put("/api/league/association");
    }

    @Test
    public void noDeleteOnDocument() throws Exception
    {
        expect().response().statusCode(405).header("Content-Length", "0").when().delete("/api/league");
    }

    @Test
    public void existsOnDocument() throws Exception
    {
        expect().response().statusCode(200).header("Content-Length", "0").when().head("/api/league");
    }

}
