package org.mule.module.apikit.rest.integration;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.mule.tck.functional.FlowAssert;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import org.junit.Rule;
import org.junit.Test;

public class RestIntegrationTestCase extends FunctionalTestCase
{

    @Rule
    public DynamicPort serverPort = new DynamicPort("serverPort");

    @Override
    protected void doSetUp() throws Exception {
        RestAssured.port = serverPort.getNumber();
        super.doSetUp();
    }

    @Override
    protected String getConfigResources() {
        return "org/mule/module/apikit/rest/integration/functional-config.xml, org/mule/module/apikit/rest/integration/functional-flows.xml";
    }

    @Test
    public void testHeadOnCollectionArchetype() throws Exception {
        expect().response().statusCode(200).header("Content-Length", "0").when().head("/api/leagues");
    }

    @Test
    public void testNoDeleteOnCollectionArchetype() throws Exception {
        expect().response().statusCode(405).when().delete("/api/leagues");
    }

    @Test
    public void testRetrieveStatusCodeOnCollectionArchetype() throws Exception {
        expect().log().all().response().statusCode(200).when().get("/api/leagues");
    }

    @Test
    public void testRetrieveOnCollectionArchetype() throws Exception {
        expect().log().everything().response().body("leagues.name", hasItems("Liga BBVA", "Premier League")).when().get("/api/leagues");
    }

/*
    @Test
    public void testRetrieveAsXmlOnCollectionArchetype() throws Exception {
        given().header("Accept", "text/xml").expect().response().body("leagues.league.name", hasItems("Liga BBVA", "Premier League")).when().get("/api/leagues");
        FlowAssert.verify("apiImplementation");
    }
*/

    @Test
    public void testRetrieveOnCollectionArchetypeWithWrongContentType() throws Exception {
        given().header("Accept", "application/xml").expect().response().statusCode(406).when().get("/api/leagues");
    }

    @Test
    public void testRetrieveContentTypeOnCollectionArchetype() throws Exception {
        expect().log().body().response().contentType(ContentType.JSON).when().get("/api/leagues");
    }

    @Test
    public void testCreateOnCollectionArchetype() throws Exception {
        given().body("{ \"name\": \"MLS\" }").contentType("application/json").expect().statusCode(201).post("/api/leagues");
        expect().log().everything().response().body("leagues.name", hasItems("Liga BBVA", "Premier League", "MLS")).when().get("/api/leagues");
    }

    /*

    @Test
    public void testCreateWithInvalidContentTypeOnCollectionArchetype() throws Exception {
        given().body("{ \"name\": \"MLS\" }").contentType("application/x-www-form-urlencoded").expect().statusCode(415).post("/api/leagues");
        FlowAssert.verify("apiImplementation");
    }

    @Test
    public void testCreateUsingXmlOnCollectionArchetype() throws Exception {
        given().body("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><name>MLS</name></league>").contentType("text/xml").expect().statusCode(201).post("/api/leagues");
        FlowAssert.verify("apiImplementation");
    }

    @Test
    public void testCreateInvalidInputUsingXmlOnCollectionArchetype() throws Exception {
        given().log().everything().body("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><xxx>MLS</xxx></league>").contentType("text/xml").expect().statusCode(400).post("/api/leagues");
        FlowAssert.verify("apiImplementation");
    }

    @Test
    public void testCreateInvalidInputOnCollectionArchetype() throws Exception {
        given().log().everything().body("{ \"xxx\": \"MLS\" }").contentType("application/json").expect().statusCode(400).post("/api/leagues");
        FlowAssert.verify("apiImplementation");
    }

    @Test
    public void testLocationOnCreateOnCollectionArchetype() throws Exception {
        given().log().everything().body("{ \"name\": \"MLS\" }").contentType("application/json").expect().header("Location", is("http://localhost:8080/api/leagues/mls")).post("/api/leagues");
        FlowAssert.verify("apiImplementation");
    }

    //<!-- GET {parentUri}/{leagueName} - Retrieve a single resource -> 200 -->
    //<!-- HEAD {parentUri}/{leagueName} - Does the resource exists? 200 : 404 -->
    //<!-- DELETE {parentUri}/{leagueName} - Delete a resource -> 204 -->

    @Test
    public void testNoPutOnDocumentArchetype() throws Exception {
        expect().response().statusCode(405).when().put("/api/leagues/liga-bbva");
        FlowAssert.verify("apiImplementation");
    }

    @Test
    public void testNoPostOnDocumentArchetype() throws Exception {
        expect().response().statusCode(405).when().post("/api/leagues/liga-bbva");
        FlowAssert.verify("apiImplementation");
    }

    @Test
    public void testRetrieveUsingXmlOnDocumentArchetype() throws Exception {
        given().log().all().header("Accept", "text/xml").expect().response().log().all().statusCode(200).contentType("text/xml").body("league.id", is("liga-bbva")).when().get("/api/leagues/liga-bbva");
        FlowAssert.verify("apiImplementation");
    }

    @Test
    public void testRetrieveUsinXmlOnDocumentArchetype2() throws Exception {
        given().log().all().header("Accept", "text/xml").expect().response().log().all().statusCode(200).contentType("text/xml").body("league.id", is("premier-league")).when().get("/api/leagues/premier-league");
        FlowAssert.verify("apiImplementation");
    }
*/

    @Test
    public void testRetrieveOnMemberArchetype() throws Exception {
        given().log().all().header("Accept", "application/json").expect().response().log().all().statusCode(200).contentType("application/json").body("id", is("liga-bbva")).when().get("/api/leagues/liga-bbva");
    }

    @Test
    public void testDeleteOnMemberArchetype() throws Exception {
        expect().response().statusCode(204).when().delete("/api/leagues/liga-bbva");
    }


/*

    @Test
    public void testRetrieveUsinJsonOnDocumentArchetype2() throws Exception {
        given().log().all().header("Accept", "application/json").expect().response().log().all().statusCode(200).contentType("application/json").body("id", is("premier-league")).when().get("/api/leagues/premier-league");
        FlowAssert.verify("apiImplementation");
    }

    @Test
    public void testRetrieveDoesntExistsOnDocumentArchetype() throws Exception {
        given().log().all().header("Accept", "text/xml").expect().response().log().all().statusCode(404).when().get("/api/leagues/mls");
        FlowAssert.verify("apiImplementation");
    }

*/
}
