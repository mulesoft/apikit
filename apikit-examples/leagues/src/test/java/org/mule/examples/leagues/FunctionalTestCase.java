package org.mule.examples.leagues;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class FunctionalTestCase extends org.mule.tck.junit4.FunctionalTestCase
{

    private static final String BARCELONA_ID = "BAR";
    private static final String BARCELONA_NAME = "Barcelona";
    private static final String BARCELONA_CITY = "Barcelona";
    private static final String BARCELONA_STADIUM = "Camp Nou";

    private static final String NEW_TEAM_ID = "NEW";
    private static final String NEW_TEAM_NAME = "New Team";
    private static final String NEW_TEAM_CITY = "Barcelona";
    private static final String NEW_TEAM_STADIUM = "New Stadium";

    private static final String UPDATED_TEAM_NAME = "Updated Team";


    @ClassRule
    public static DynamicPort httpPort = new DynamicPort("http.port");

    protected String getConfigResources()
    {
        return "leagues-test-config.xml";
    }

    @Before
    public void doSetUp()
    {
        RestAssured.port = httpPort.getNumber();
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api";
    }

    @Test
    public void initializedTeams() throws Exception
    {
        given().log().all().
                header("Accept", "application/json").
                expect().
                response().
                statusCode(HttpStatus.SC_OK).
                body("teams", hasSize(20)).
                when().
                get("/teams");

        given().log().all().
                header("Accept", "application/json").
                expect().
                response().
                statusCode(HttpStatus.SC_OK).
                body("id", is(BARCELONA_ID)).
                body("name", is(BARCELONA_NAME)).
                body("homeCity", is(BARCELONA_CITY)).
                body("stadium", is(BARCELONA_STADIUM)).
                when().
                get("/teams/" + BARCELONA_ID);
    }

    @Test
    public void teamNotFound() throws Exception
    {
        given().log().all().
                header("Accept", "application/json").
                expect().
                response().
                statusCode(HttpStatus.SC_NOT_FOUND).
                when().
                get("/teams/" + NEW_TEAM_ID);
    }

    @Test
    public void newTeam() throws Exception
    {
        given().log().all().body("{\"name\": \"Elche\",\"id\": \"ELC\",\"homeCity\": \"Elche\",\"stadium\": \"Martinez Valero\"}")
                .contentType("application/json")
                .expect().statusCode(201)
                .header("Content-Length", "0")
                .when().post("/teams");
    }

    @Test
    public void positions() throws Exception
    {
        given().log().all().
                header("Accept", "application/json").
                expect().
                response().
                statusCode(HttpStatus.SC_OK).
                body("positions.size", is(20)).
                when().
                get("/positions");
    }

    @Test
    public void fixture() throws Exception
    {
        given().log().all().
                header("Accept", "application/json").
                expect().
                response().
                statusCode(HttpStatus.SC_OK).
                body("fixture.size", is(19 * 20)).
                when().
                get("/fixture");
    }

}
