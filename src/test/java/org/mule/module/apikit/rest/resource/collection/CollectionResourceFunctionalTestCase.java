
package org.mule.module.apikit.rest.resource.collection;

import static com.jayway.restassured.RestAssured.expect;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class CollectionResourceFunctionalTestCase extends FunctionalTestCase
{

    @Override
    public int getTestTimeoutSecs()
    {
        return 6000;
    }

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
        return "org/mule/module/apikit/rest/resource/collection/collection-functional-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    public void exists() throws Exception
    {
        expect().response().statusCode(200).when().head("/api/leagues");
    }

    @Test
    public void retrieve() throws Exception
    {
        expect().log()
            .everything()
            .response()
            .statusCode(200)
            .body(containsString("Liga BBVA"))
            .when()
            .get("/api/leagues");
    }

    @Test
    public void createMember() throws Exception
    {
        expect().log()
            .everything()
            .response()
            .statusCode(201)
            .header("Content-Length", "0")
            .header("location", "http://localhost:" + serverPort.getNumber() + "/api/leagues/1")
            .when()
            .post("/api/leagues");
    }

    @Test
    public void retrieveMember() throws Exception
    {

    }

    @Test
    public void updateMember() throws Exception
    {

    }

    @Test
    public void deleteMember() throws Exception
    {

    }

}
