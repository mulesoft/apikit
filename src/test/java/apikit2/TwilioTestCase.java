package apikit2;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

import org.junit.Rule;
import org.junit.Test;

public class TwilioTestCase extends FunctionalTestCase
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
        return "twilio/twilio-flow-config.xml";
    }

    @Test
    public void resourceNotFound() throws Exception
    {
        given().header("Accept", "application/json")
        .expect().response().statusCode(404)
                .body(is("resource not found"))
                .when().get("/2010-04-01/matches");
    }

    @Test
    public void getOnAccountsJson() throws Exception
    {
        given().header("Accept", "application/json")
            .expect().log().everything()
                .response().body("accounts.friendly_name", hasItems("account 1", "account 2"))
                .header("Content-type", "application/json").statusCode(200)
            .when().get("/2010-04-01/Accounts");
    }


}
