
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

        FlowAssert.verify("retrieveLeagues");
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
        expect().response().statusCode(204).header("Content-Length", "0").when().delete("/api/leagues/1");

        FlowAssert.verify("deleteLeague");
    }

    // Nested Collection

    @Test
    public void existsNestedCollection() throws Exception
    {
        expect().response().statusCode(200).when().head("/api/leagues/1/teams");
    }

    @Test
    public void retrieveNestedCollection() throws Exception
    {
        expect().response()
            .statusCode(200)
            .body(Matchers.equalTo("{teams:[{'id':3,'name'='three'},{'id':4,'name'='four'}]}"))
            .when()
            .get("/api/leagues/1/teams");

        FlowAssert.verify("retrieveTeams");

    }

    @Test
    public void createMemberNestedCollection() throws Exception
    {
        given().body("{'id':3,'name'='three'}")
            .expect()
            .response()
            .statusCode(201)
            .header("Content-Length", "0")
            .header("location", "http://localhost:" + serverPort.getNumber() + "/api/leagues/1/teams/3")
            .when()
            .post("/api/leagues/1/teams");

        FlowAssert.verify("createTeam");
    }

    @Test
    public void retrieveMember1NestedCollection() throws Exception
    {
        expect().response()
            .statusCode(200)
            .body(Matchers.equalTo("{'id':3,'name'='three'}"))
            .when()
            .get("/api/leagues/1/teams/3");

        FlowAssert.verify("retrieveTeam");
    }

    @Test
    public void retrieveMember2NestedCollection() throws Exception
    {
        expect().response()
            .statusCode(200)
            .body(Matchers.equalTo("{'id':4,'name'='four'}"))
            .when()
            .get("/api/leagues/1/teams/4");

        FlowAssert.verify("retrieveTeam");
    }

    @Test
    public void updateMemberNestedCollection() throws Exception
    {
        given().body("{'id':3,'name'='THREE'}")
            .expect()
            .response()
            .statusCode(200)
            .header("Content-Length", "0")
            .when()
            .put("/api/leagues/1/teams/3");

        FlowAssert.verify("updateTeam");
    }

    @Test
    public void deleteMemberNesedCollection() throws Exception
    {
        expect().response()
            .statusCode(204)
            .header("Content-Length", "0")
            .when()
            .delete("/api/leagues/1/teams/3");

        FlowAssert.verify("deleteTeam");
    }

    // Nested Dcoument

    @Test
    public void retrieveNestedDocument() throws Exception
    {
        expect().response()
            .statusCode(200)
            .body(Matchers.equalTo("{'Barclays'}"))
            .when()
            .get("/api/leagues/1/sponsor");

        FlowAssert.verify("retrieveSponsor");

    }

}
