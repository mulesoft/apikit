
package org.mule.module.apikit.rest.resource;

import static com.jayway.restassured.RestAssured.expect;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;

import com.jayway.restassured.RestAssured;

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
        return "org/mule/module/apikit/rest/resource/collection-resource-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    public void noHeadOnCollection() throws Exception
    {
        expect().response().statusCode(405).when().head("/api/leagues");
    }

    @Test
    public void retrieveOnCollection() throws Exception
    {
        expect().log()
            .everything()
            .response()
            .statusCode(200)
            .body(containsString("Liga BBVA"))
            .when()
            .get("/api/leagues");
    }

}
