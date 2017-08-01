package org.mule.module.metadata;

import org.apache.commons.io.IOUtils;
import org.mule.module.metadata.interfaces.Parseable;
import org.mule.module.metadata.interfaces.ResourceLoader;
import org.mule.raml.interfaces.model.IRaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RamlHandler
{
    public static final String PARSER_V2_PROPERTY = "apikit.raml.parser.v2";

    private ResourceLoader resourceLoader;

    public RamlHandler(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public IRaml getRamlApi(String uri) {

        try
        {
            File ramlFile = resourceLoader.getRamlResource(uri);
            String ramlContent = getRamlContent(ramlFile);
            Parseable parser = getParser(ramlContent);
            return parser.build(ramlFile, ramlContent);

        } catch (FileNotFoundException e)
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

    private String getRamlContent(File uri) throws FileNotFoundException
    {
        try
        {
            return IOUtils.toString(new FileInputStream(uri));

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
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
