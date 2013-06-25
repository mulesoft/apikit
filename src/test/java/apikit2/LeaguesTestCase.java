package apikit2;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class LeaguesTestCase extends FunctionalTestCase
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
        return "apikit2/leagues-flow-config.xml";
    }

    @Test
    public void resourceNotFound() throws Exception
    {
        given().header("Accept", "application/json")
        .expect().response().statusCode(404)
                .body(is("resource not found"))
                .when().get("/api/matches");
    }

    @Test
    public void methodNotAllowed() throws Exception
    {
        expect().response().statusCode(405)
                .body(is("method not allowed"))
                .when().delete("/api/leagues");
    }

    @Test
    public void unsupportedMediaType() throws Exception
    {
        given().body("Liga Criolla").contentType("text/plain")
        .expect().response().statusCode(415)
                .body(is("unsupported media type"))
                .when().post("/api/leagues");
    }

    @Test
    public void notAcceptable() throws Exception
    {
        given().header("Accept", "text/plain")
        .expect().response().statusCode(406)
                .body(is("not acceptable"))
                .when().get("/api/leagues");
    }

    @Test
    public void badRequestJson() throws Exception
    {
        given().body("{\"liga\": \"Criolla\"}").contentType("application/json")
                .expect().response().statusCode(400)
                .body(is("bad request"))
                .when().post("/api/leagues");
    }

    @Test
    public void badRequestXml() throws Exception
    {
        given().body("<leaguee xmlns=\"http://mulesoft.com/schemas/soccer\"><name>MLS</name></leaguee>")
                .contentType("text/xml")
                .expect().response().statusCode(400)
                .body(is("bad request"))
                .when().post("/api/leagues");
    }

    @Test
    public void getOnLeaguesJson() throws Exception
    {
        given().header("Accept", "application/json")
            .expect().log().everything()
                .response().body("leagues.name", hasItems("Liga BBVA", "Premier League"))
                .header("Content-type", "application/json").statusCode(200)
            .when().get("/api/leagues");
    }

    @Test
    public void getOnLeaguesXml() throws Exception
    {
        given().header("Accept", "text/xml")
            .expect().log().everything()
                .response().body("leagues.league.name", hasItems("Liga BBVA", "Premier League"))
                .header("Content-type", "text/xml").statusCode(200)
            .when().get("/api/leagues");
    }

    @Test
    public void postOnLeaguesJson() throws Exception
    {
        given().body("{ \"name\": \"Major League Soccer\" }")
                .contentType("application/json")
            .expect().statusCode(201)
                .header("Location", "http://localhost:" + serverPort.getValue() + "/api/leagues/3")
            .when().post("/api/leagues");
    }

    @Test
    public void postOnLeaguesXml() throws Exception
    {
        given().body("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><name>MLS</name></league>")
                .contentType("text/xml")
            .expect().statusCode(201)
                .header("Location", "http://localhost:" + serverPort.getValue() +"/api/leagues/3")
            .when().post("/api/leagues");
    }

    @Test
    public void postCustomStatus() throws Exception
    {
        given().log().everything().body("{ \"name\": \"(invlid name)\" }")
                .contentType("application/json")
            .expect()
                .statusCode(400)
                .body(is("Invalid League Name"))
            .when().post("/api/leagues");
    }

    @Test
    public void getOnSingleLeagueJson() throws Exception
    {
        given().header("Accept", "application/json")
            .expect().log().everything()
                .response().body("name", is("Liga BBVA"))
                .header("Content-type", "application/json").statusCode(200)
            .when().get("/api/leagues/liga-bbva");
    }

    @Test
    public void getOnSingleLeagueXml() throws Exception
    {
        given().header("Accept", "text/xml")
            .expect()
                .response().body("league.name", is("Liga BBVA"))
                .header("Content-type", "text/xml").statusCode(200)
            .when().get("/api/leagues/liga-bbva");
    }

    @Test
    public void putOnSingleLeagueJson() throws Exception
    {
        given().body("{ \"name\": \"Liga Hispanica\" }")
                .contentType("application/json")
            .expect()
                .statusCode(204)
                .body(is(""))
            .when().put("/api/leagues/liga-bbva");

        expect().response()
                .body("leagues.league.name", hasItems("Liga Hispanica", "Premier League"))
            .when().get("/api/leagues");
    }

    @Test
    public void putOnSingleLeagueXml() throws Exception
    {
        given().body("<league xmlns=\"http://mulesoft.com/schemas/soccer\"><name>Hispanic League</name></league>")
                .contentType("text/xml")
            .expect()
                .statusCode(204)
                .body(is(""))
            .when().put("/api/leagues/liga-bbva");

        expect().response()
                .body("leagues.league.name", hasItems("Hispanic League", "Premier League"))
            .when().get("/api/leagues");
    }

    @Test
    public void deleteOnSingleLeague() throws Exception
    {
        expect().response()
            .statusCode(204).body(is(""))
            .when().delete("/api/leagues/liga-bbva");
    }
}
