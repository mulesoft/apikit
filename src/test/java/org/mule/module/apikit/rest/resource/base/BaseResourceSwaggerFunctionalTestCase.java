
package org.mule.module.apikit.rest.resource.base;

import static com.jayway.restassured.RestAssured.given;

import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.util.IOUtils;

import com.jayway.restassured.RestAssured;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class BaseResourceSwaggerFunctionalTestCase extends FunctionalTestCase
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
        return "org/mule/module/apikit/rest/resource/base/base-functional-config.xml, org/mule/module/apikit/test-flows-config.xml";
    }

    @Test
    @Ignore
    public void getHtml() throws Exception
    {
        given().header("Accept", "text/html")
            .expect()
            .response()
            .statusCode(200)
            .contentType("text/html")
            .body(Matchers.hasXPath("//html/body/div[@id='header']/div[@class='swagger-ui-wrap']/a"))
            .when()
            .get("/api");
        given().header("Accept", "text/html")
            .expect()
            .response()
            .statusCode(200)
            .contentType("text/html")
            .body(Matchers.hasXPath("//html/body/div[@id='header']/div[@class='swagger-ui-wrap']/a"))
            .when()
            .get("/api/");
    }

    @Test
    public void getResources() throws Exception
    {
        given().header("Accept", "application/x-javascript")
            .expect()
            .response()
            .statusCode(200)
            .contentType("application/x-javascript")
            .body(
                Matchers.equalTo(IOUtils.getResourceAsString(
                    "org/mule/module/apikit/rest/swagger/lib/swagger.js", this.getClass())))
            .when()
            .get("/api/_swagger/lib/swagger.js");
    }

    /*
     * Default behavior is to provide swagger meta-data (json) for all resources/operation including those
     * that are protected with access controls
     */
    @Test
    public void getSwaggerJson() throws Exception
    {
        given().header("Accept", "application/swagger+json")
            .expect()
            .response()
            .statusCode(200)
            .contentType("application/swagger+json")
            .body(
                Matchers.equalTo("{\"apiVersion\":\"1.0\",\"swaggerVersion\":\"1.0\",\"apis\":[{\"path\":\"/leagues\",\"description\":\"\"},{\"path\":\"/teams\",\"description\":\"\"}]}"))
            .when()
            .get("/api");
        given().header("Accept", "application/swagger+json")
            .expect()
            .response()
            .statusCode(200)
            .contentType("application/swagger+json")
            .body(
                Matchers.equalTo("{\"apiVersion\":\"1.0\",\"swaggerVersion\":\"1.0\",\"apis\":[{\"path\":\"/leagues\",\"description\":\"\"},{\"path\":\"/teams\",\"description\":\"\"}]}"))
            .when()
            .get("/api/");
    }

    @Test
    public void getSwaggerJsonProtectedInterface() throws Exception
    {
        given().header("Accept", "application/swagger+json")
            .expect()
            .response()
            .statusCode(401)
            .when()
            .get("/protectedapi");
        given().header("Accept", "application/swagger+json")
            .expect()
            .response()
            .statusCode(401)
            .when()
            .get("/protectedapi/");
    }

}
