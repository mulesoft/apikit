package heaven;

import static org.junit.Assert.assertEquals;

import heaven.model.Heaven;
import heaven.model.ParamType;
import heaven.model.parameter.UriParameter;
import org.junit.Test;

public class BasicInfoTestCase extends AbstractHeavenTestCase
{

    @Test
    public void rootElements()
    {
        Heaven heaven = validateFile("heaven/root-elements.yaml");
        UriParameter hostParam = heaven.getUriParameters().get("host");
        assertEquals("Host", hostParam.getName());
        assertEquals(ParamType.STRING, hostParam.getType());
        UriParameter pathParam = heaven.getUriParameters().get("path");
        assertEquals("Path", pathParam.getName());
        assertEquals(ParamType.STRING, pathParam.getType());
    }

    @Test
    public void titleNotDefined()
    {
        expectParseException("heaven/title-not-defined.yaml", "title not defined");
    }

    @Test
    public void titleTooLong()
    {
        expectParseException("heaven/title-too-long.yaml", "Title too long");
    }

    @Test
    public void baseUriNotDefined()
    {
        expectParseException("heaven/baseuri-not-defined.yaml", "baseUri not defined");
    }

    @Test
    public void baseUriMalformed()
    {
        expectParseException("heaven/baseuri-malformed.yaml", "Malformed baseUri");
    }

}
