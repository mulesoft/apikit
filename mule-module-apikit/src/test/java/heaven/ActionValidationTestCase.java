package heaven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import heaven.model.Action;
import heaven.model.Body;
import heaven.model.Resource;
import heaven.model.parameter.FormParameter;
import heaven.model.parameter.Header;
import heaven.model.Heaven;
import heaven.model.MimeType;
import heaven.model.ParamType;
import heaven.model.parameter.QueryParameter;
import heaven.model.validation.MaximumNumberValidation;
import heaven.model.validation.MinimumNumberValidation;
import heaven.model.validation.Validation;
import heaven.model.parameter.UriParameter;
import heaven.model.validation.EnumerationValidation;
import heaven.model.validation.MaxLengthValidation;
import heaven.model.validation.MaximumIntegerValidation;
import heaven.model.validation.MinLengthValidation;
import heaven.model.validation.MinimumIntegerValidation;
import heaven.model.validation.PatternValidation;
import org.junit.Test;

public class ActionValidationTestCase extends AbstractHeavenTestCase
{

    @Test
    public void actionNotDefined()
    {
        expectParseException("heaven/action-not-defined.yaml", "at least one action must be defined");
    }

    @Test
    public void actionFullyLoaded()
    {
        Heaven heaven = validateFile("heaven/action-fully-loaded.yaml");

        Action put = heaven.getResources().get("/media").getAction("put");
        assertNotNull(put);
        assertEquals("nice summary", put.getSummary());
        assertEquals("nice description", put.getDescription());
        assertEquals(1, put.getHeaders().size());
        assertEquals(1, put.getQueryParameters().size());
        //TODO uri params

        //headers
        Header h1 = put.getHeaders().get("header-1");
        assertNotNull(h1);
        assertEquals("header 1", h1.getName());
        assertEquals("header 1 description", h1.getDescription());
        assertEquals(ParamType.STRING, h1.getType());
        assertEquals(false, h1.isRequired());
        assertEquals(3, h1.getValidations().size());
        Validation h1Pattern = h1.getValidations().get(0);
        assertTrue(h1Pattern instanceof PatternValidation);
        Validation h1MinL = h1.getValidations().get(1);
        assertTrue(h1MinL instanceof MinLengthValidation);
        Validation h1MaxL = h1.getValidations().get(2);
        assertTrue(h1MaxL instanceof MaxLengthValidation);
        assertEquals("san francisco", h1.getDefaultValue());
        assertEquals("new york", h1.getExample());

        //query parameters
        QueryParameter q1 = put.getQueryParameters().get("query-1");
        assertNotNull(q1);
        assertEquals("query 1", q1.getName());
        assertEquals("query 1 description", q1.getDescription());
        assertEquals(ParamType.INTEGER, q1.getType());
        assertTrue(q1.isRequired());
        assertEquals(2, q1.getValidations().size());
        Validation q1Min = q1.getValidations().get(0);
        assertTrue(q1Min instanceof MinimumIntegerValidation);
        Validation q1Max = q1.getValidations().get(1);
        assertTrue(q1Max instanceof MaximumIntegerValidation);
        assertEquals("17", q1.getExample());

        //body
        Body body = put.getBody();
        MimeType json = body.getMimeTypes().get("application/json");
        assertTrue(json.getSchema().contains("input"));
        assertEquals("{ \"input\": \"hola\" }", json.getExample());

        //form params
        MimeType formData = body.getMimeTypes().get("multipart/form-data");
        FormParameter form1 = formData.getFormParameters().get("form-1");
        assertEquals("form 1", form1.getName());
        assertEquals("form 1 description", form1.getDescription());
        assertEquals(ParamType.NUMBER, form1.getType());
        assertTrue(form1.isRequired());
        Validation fMin = form1.getValidations().get(0);
        assertTrue(fMin instanceof MinimumNumberValidation);
        Validation fMax = form1.getValidations().get(1);
        assertTrue(fMax instanceof MaximumNumberValidation);

        //responses
        assertEquals(3, put.getResponses().size());
        Body r200 = put.getResponses().get(200);
        MimeType r200Mime = r200.getMimeTypes().get("application/json");
        assertEquals("{ \"key\": \"value\" }", r200Mime.getExample());
        Body r400 = put.getResponses().get(400);
        MimeType r400Mime = r400.getMimeTypes().get("text/xml");
        assertEquals("<root>none</root>", r400Mime.getExample());
        assertEquals(r400, put.getResponses().get(404));

        //resource uri parameters
        Resource mediaId = heaven.getResources().get("/media").getResources().get("/{mediaId}");
        UriParameter param =  mediaId.getUriParameters().get("mediaId");
        assertEquals("mediaId description", param.getDescription());
        assertEquals(ParamType.STRING, param.getType());
        Validation uriEnum = param.getValidations().get(0);
        assertTrue(uriEnum instanceof EnumerationValidation);
    }
}
