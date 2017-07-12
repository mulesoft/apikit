import org.apache.commons.io.IOUtils;
import org.mule.module.metadata.RamlV1Parser;
import org.mule.module.metadata.RamlV2Parser;
import org.mule.module.metadata.interfaces.Parseable;
import org.mule.module.metadata.interfaces.ResourceLoader;
import org.mule.raml.interfaces.model.IRaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class TestResourceLoader implements ResourceLoader
{
    public static final String PARSER_V2_PROPERTY = "apikit.raml.parser.v2";

    @Override
    public IRaml getRamlApi(String uri)
    {
        try
        {
            File f = new File(TestResourceLoader.class.getResource(uri).toURI());
            String ramlContent = getRamlContent(uri);
            Parseable parser = getParser(ramlContent);
            return parser.build(f, ramlContent);

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private Parseable getParser(String ramlContent)
    {
        if (useParserV2(ramlContent)) {
            return new RamlV2Parser();
        } else {
            return new RamlV1Parser();
        }
    }

    private String getRamlContent(String uri) throws URISyntaxException, IOException
    {
        InputStream stream = TestResourceLoader.class.getResourceAsStream(uri);
        return IOUtils.toString(stream);
    }

    private static boolean useParserV2(String content)
    {
        String property = System.getProperty(PARSER_V2_PROPERTY);
        if (property != null && Boolean.valueOf(property))
        {
            return true;
        }
        else
        {
            return content.startsWith("#%RAML 1.0");
        }
    }

}
