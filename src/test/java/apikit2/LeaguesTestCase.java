package apikit2;

import static com.jayway.restassured.RestAssured.given;
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
    public void testGetOnLeaguesJSON() throws Exception
    {
        given().header("Accept", "application/json")
            .expect().log().everything()
                .response().body("leagues.name", hasItems("Liga BBVA", "Premier League"))
                .header("Content-type", "application/json")
            .when().get("/api/leagues");
    }

    @Test
    public void testGetOnLeaguesXML() throws Exception
    {
        given().header("Accept", "text/xml")
            .expect().log().everything()
                .response().body("leagues.league.name", hasItems("Liga BBVA", "Premier League"))
                .header("Content-type", "text/xml")
            .when().get("/api/leagues");
    }

}
