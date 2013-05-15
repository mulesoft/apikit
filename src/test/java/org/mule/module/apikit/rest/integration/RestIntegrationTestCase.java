/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.module.apikit.rest.integration;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.mule.module.apikit.rest.protocol.http.HttpStatusCode;
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
        return "org/mule/module/apikit/rest/integration/functional-config.xml, org/mule/module/apikit/rest/integration/functional-flows.xml";
    }

    @Test
    public void testHeadOnCollectionArchetype() throws Exception
    {
        expect().response().statusCode(200).header("Content-Length", "0").when().head("/api/leagues");
    }

    @Test
    public void testNoDeleteOnCollectionArchetype() throws Exception
    {
        expect().response().statusCode(405)
                .body("title", is("METHOD NOT ALLOWED"))
                .when().delete("/api/leagues");
    }

    @Test
    public void testRetrieveStatusCodeOnCollectionArchetype() throws Exception
    {
        expect().log().all().response().statusCode(200).when().get("/api/leagues");
    }

    @Test
    public void testRetrieveOnCollectionArchetype() throws Exception
    {
        expect().log()
                .everything()
                .response()
                .body("leagues.name", hasItems("Liga BBVA", "Premier League"))
                .when()
                .get("/api/leagues");
    }


    @Test
    public void testRetrieveAsXmlOnCollectionArchetype() throws Exception
    {
        given().header("Accept", "text/xml").expect().response()
                .body("leagues.league.name", hasItems("Liga BBVA", "Premier League")).when().get("/api/leagues");
    }


    @Test
    public void testRetrieveOnCollectionArchetypeWithWrongContentType() throws Exception
    {
        given().header("Accept", "application/xml")
                .expect()
                .response()
                .statusCode(406)
                .when()
                .get("/api/leagues");
    }

    @Test
    public void testRetrieveContentTypeOnCollectionArchetype() throws Exception
    {
        expect().log().body().response().contentType(ContentType.JSON).when().get("/api/leagues");
    }

    @Test
    public void testCreateOnCollectionArchetype() throws Exception
    {
        given().body("{ \"name\": \"MLS\" }")
                .contentType("application/json")
                .expect()
                .statusCode(201)
                .post("/api/leagues");
        expect().log()
                .everything()
                .response()
                .body("leagues.name", hasItems("Liga BBVA", "Premier League", "MLS"))
                .when()
                .get("/api/leagues");
    }

    @Test
    public void testCreateCustomStatusOnCollectionArchetype() throws Exception
    {
        given().log().everything().body("{ \"name\": \"(invlid name)\" }")
                .contentType("application/json")
                .expect().log().everything()
                .statusCode(400)
                .body(containsString("Invalid League Name"))
                .post("/api/leagues");
    }

    @Test
    public void testUpdateCustomStatusOnCollectionArchetype() throws Exception
    {
        given().log().everything().body("{ \"name\": \"(invlid name)\" }")
                .contentType("application/json")
                .expect().log().everything()
                .statusCode(400)
                .body(containsString("Invalid League Name"))
                .put("/api/leagues/liga-bbva");
    }

    @Test
    public void testCreateInvalidInputTypeUsingJsonOnCollectionArchetype() throws Exception
    {
        given().log()
                .everything().body("{ \"name\": 4 }"
        ).contentType("application/json").expect().statusCode(HttpStatusCode.CLIENT_ERROR_BAD_REQUEST.getCode()).post("/api/leagues");
    }

    @Test
    public void testCreateInvalidInputPropertyUsingJsonOnCollectionArchetype() throws Exception
    {
        given().log()
                .everything().body("{ \"surname\": \"hola\" }"
        ).contentType("application/json").expect().statusCode(HttpStatusCode.CLIENT_ERROR_BAD_REQUEST.getCode()).post("/api/leagues");
    }

    @Test
    public void testCreateInvalidInputUsingXmlOnCollectionArchetype() throws Exception
    {
        given().log()
                .everything().body("<leaguee xmlns=\"http://mulesoft.com/schemas/soccer\"><name>MLS</name></leaguee>"
        ).contentType("text/xml").expect().statusCode(HttpStatusCode.CLIENT_ERROR_BAD_REQUEST.getCode()).post("/api/leagues");
    }

    @Test
    public void testCreateUsingXmlOnCollectionArchetype() throws Exception
    {
        given().body("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><name>MLS</name></league>"
        ).contentType("text/xml").expect().statusCode(201).post("/api/leagues");
    }


    /*
    @Test
    public void testCreateWithInvalidContentTypeOnCollectionArchetype() throws Exception
    {
        given().body
                ("{ \"name\": \"MLS\" }").contentType("application/x-www-form-urlencoded").expect().statusCode
                (415).post("/api/leagues");
        FlowAssert.verify("apiImplementation");
    }

    @Test
    public void testCreateInvalidInputOnCollectionArchetype() throws Exception
    {
        given().log().everything
                ().body("{ \"xxx\": \"MLS\" }").contentType("application/json").expect().statusCode
                (400).post("/api/leagues");
        FlowAssert.verify("apiImplementation");
    }

    @Test
    public void testLocationOnCreateOnCollectionArchetype() throws Exception
    {
        given().log().everything
                ().body("{ \"name\": \"MLS\" }").contentType("application/json").expect().header("Location",
                                                                                                 is("http://localhost:8080/api/leagues/mls")).post("/api/leagues");
        FlowAssert.verify("apiImplementation");
    } //<!-- GET {parentUri}/{leagueName} - Retrieve a single

    resource->200--> //<!-- HEAD {parentUri}/{leagueName} - Does the resource exists? 200 : 404 -->

    //<!-- DELETE {parentUri}/{leagueName} - Delete a resource -> 204 -->
    */

    @Test
    public void testNoUpdateOnCollection() throws Exception
    {
        expect().response().statusCode(405).when().put("/api/leagues");
    }

    @Test
    public void testUpdateOnMember() throws Exception
    {
        given().body("{ \"name\": \"Liga Hispanica\" }")
                .contentType("application/json")
                .expect()
                .statusCode(204)
                .header("Content-Length", "0")
                .put("/api/leagues/liga-bbva");
        expect().log()
                .everything()
                .response()
                .body("leagues.name", hasItems("Liga Hispanica", "Premier League"))
                .when()
                .get("/api/leagues");
    }

    @Test
    public void testRetrieveOnMemberArchetype() throws Exception
    {
        given().log()
                .all()
                .header("Accept", "application/json")
                .expect()
                .response()
                .log()
                .all()
                .statusCode(200)
                .contentType("application/json")
                .body("id", is("liga-bbva"))
                .when()
                .get("/api/leagues/liga-bbva");
    }

    @Test
    public void testDeleteOnMemberArchetype() throws Exception
    {
        expect().response()
            .statusCode(204)
            .header("Content-Length", "0")
            .when()
            .delete("/api/leagues/liga-bbva");
    }

    @Test
    public void testRetrieveOnSubCollectionArchetype() throws Exception
    {
        given().header("Accept", "application/json")
        .expect().log()
                .everything()
                .response().statusCode(200)
                .body("name", hasItems("Real Madrid", "Barcelona"))
                .when()
                .get("/api/leagues/liga-bbva/teams");
    }

    @Test
    public void testRetrieveQueryParamOnSubCollectionArchetype() throws Exception
    {
        given().header("Accept", "application/json")
        .expect().log()
                .everything()
                .response().statusCode(200)
                .body("name", not(hasItems("Valencia")))
                .when()
                .get("/api/leagues/liga-bbva/teams?limit=2");
    }

    @Test
    public void testRetrieveOnSubCollectionArchetypeForce500Json() throws Exception
    {
        expect().log()
                .everything()
                .response()
                .statusCode(500)
                .contentType("application/json")
                .body("httpStatus", is("500"))
                .when()
                .get("/api/leagues/liga-bbvaa/teams");
    }

    @Test
    public void testRetrieveOnSubCollectionArchetypeForce500Xml() throws Exception
    {
        given().header("Accept", "text/xml")
        .expect().log()
                .everything()
                .response()
                .statusCode(500)
                .contentType("text/xml")
                .body("httpStatus", hasItem("500"))
                .when()
                .get("/api/leagues/liga-bbvaa/teams");
    }

    @Test
    public void testRetrieveOnSubMemberArchetype() throws Exception
    {
        given().log()
                .all()
                .header("Accept", "application/json")
                .expect()
                .response()
                .log()
                .all()
                .statusCode(200)
                .contentType("application/json")
                .body("id", is("barcelona"))
                .when()
                .get("/api/leagues/liga-bbva/teams/barcelona");
    }

    @Test
    public void testRetrieveInvalidPathOnSubMemberArchetype() throws Exception
    {
        given().log()
                .all()
                .header("Accept", "application/json")
                .expect()
                .response()
                .log()
                .all()
                .statusCode(404)
                .when()
                .get("/api/leagues/liga-bbvaa/teams/barcelona");
    }

    @Test
    public void testRetrieveInvalidPathOnMemberArchetype() throws Exception
    {
        given().log()
                .all()
                .header("Accept", "application/json")
                .expect()
                .response()
                .log()
                .all()
                .statusCode(404)
                .when()
                .get("/api/leagues/liga-bbvaa");
    }

    ///////////
    // DOCUMENT
    ///////////

    @Test
    public void testRetrieveOnDocumentArchetype() throws Exception
    {
        given().log().all().header("Accept", "application/json")
                .expect().response().log().all()
                .statusCode(200)
                .contentType("application/json")
                .body("year", is(1909))
                .when().get("/api/leagues/liga-bbva/federation");
    }

    @Test
    public void testRetrieveUnacceptedTypeOnDocumentArchetype() throws Exception
    {
        given().log().all().header("Accept", "text/xml")
                .expect().response().log().all()
                .statusCode(406).contentType("text/xml")
                .body("title", hasItem("NOT ACCEPTABLE"))
                .when().get("/api/leagues/liga-bbva/federation");
    }

    @Test
    public void testUpdateOnDocumentArchetype() throws Exception
    {
        given().body("{ \"name\": \"Federacion Espanola\", \"year\":1991 }")
                .contentType("application/json")
                .expect().statusCode(204)
                .header("Content-Length", "0")
                .put("/api/leagues/liga-bbva/federation");

        //verify updated data
        given().log().all().header("Accept", "application/json")
                .expect().response().log().all()
                .statusCode(200)
                .contentType("application/json")
                .body("year", is(1991))
                .when().get("/api/leagues/liga-bbva/federation");
    }

    @Test
    public void testUpdateInvalidDataOnDocumentArchetype() throws Exception
    {
        given().body("{ \"name\": \"Federacion Espanola\", \"year\":\"nineteen eighty-four\" }")
                .contentType("application/json")
                .expect().log().all().statusCode(400)
                .body("title", is("BAD REQUEST"))
                .put("/api/leagues/liga-bbva/federation");
    }

    @Test
    public void testUpdateInvalidRepresentationOnDocumentArchetype() throws Exception
    {
        given().body("Spanish League")
                .contentType("text/plain")
                .expect().log().all().statusCode(415)
                .body("title", is("UNSUPPORTED MEDIA TYPE"))
                .put("/api/leagues/liga-bbva/federation");
    }

    @Test
    public void testNoDeleteOnDocumentArchetype() throws Exception
    {
        expect().response().statusCode(405)
                .body("title", is("METHOD NOT ALLOWED"))
                .when().delete("/api/leagues/liga-bbva/federation");
    }

    @Test
    public void testNoCreateOnDocumentArchetype() throws Exception
    {
        expect().response().statusCode(405)
                .body("title", is("METHOD NOT ALLOWED"))
                .when().post("/api/leagues/liga-bbva/federation");
    }

}
