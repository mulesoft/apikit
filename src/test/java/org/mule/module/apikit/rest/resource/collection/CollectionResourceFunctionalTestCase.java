
package org.mule.module.apikit.rest.resource.collection;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;

import org.mule.tck.functional.FlowAssert;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.hamcrest.Matchers;
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
        return "org/mule/module/apikit/rest/resource/collection/collection-functional-config.xml, org/mule/module/apikit/rest/resource/collection/collection-functional-flows.xml";
    }

    @Test
    public void exists() throws Exception
    {
        expect().response().statusCode(200).when().head("/api/leagues");
    }

    @Test
    public void retrieve() throws Exception
    {
        expect().response()
            .statusCode(200)
            .body(
                Matchers.equalTo("{leagues:[{'id':1,'name'='one'},{'id':2,'name'='two'},{'id':3,'name'='three'}]}"))
            .when()
            .get("/api/leagues");
    }

    @Test
    public void createMember() throws Exception
    {
        given().body("{'id':1,'name'='one'}")
            .expect()

            .response()
            .statusCode(201)
            .header("Content-Length", "0")
            .header("location", "http://localhost:" + serverPort.getNumber() + "/api/leagues/1")
            .when()
            .post("/api/leagues");

        FlowAssert.verify("createLeague");
    }

    @Test
    public void retrieveMember1() throws Exception
    {
        expect().response()
            .statusCode(200)
            .body(Matchers.equalTo("{'id':1,'name'='one'}"))
            .when()
            .get("/api/leagues/1");

        FlowAssert.verify("retrieveLeague");
    }

    @Test
    public void retrieveMember2() throws Exception
    {
        expect().response()
            .statusCode(200)
            .body(Matchers.equalTo("{'id':2,'name'='two'}"))
            .when()
            .get("/api/leagues/2");

        FlowAssert.verify("retrieveLeague");
    }

    @Test
    public void updateMember() throws Exception
    {
        given().body("{'id':1,'name'='ONE'}")
            .expect()
            .response()
            .statusCode(200)
            .header("Content-Length", "0")
            .when()
            .put("/api/leagues/1");

        FlowAssert.verify("updateLeague");
    }

    @Test
    public void deleteMember() throws Exception
    {
        expect().response().statusCode(200).header("Content-Length", "0").when().delete("/api/leagues/1");

        FlowAssert.verify("deleteLeague");
    }

}
