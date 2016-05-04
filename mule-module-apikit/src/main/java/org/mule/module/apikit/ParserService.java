/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.module.apikit.injector.RamlUpdater;
import org.mule.module.apikit.parser.ParserWrapper;
import org.mule.module.apikit.parser.ParserWrapperV1;
import org.mule.module.apikit.parser.ParserWrapperV2;
import org.mule.raml.interfaces.model.IRaml;

import java.io.InputStream;

import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.internal.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserService
{

    private static final String PARSER_V2_PROPERTY = "apikit.raml.parser.v2";
    private static final Logger logger = LoggerFactory.getLogger(ParserService.class);

    private final String ramlPath;
    private ResourceLoader resourceLoaderV2;
    private ParserWrapper parserWrapper;
    private boolean parserV2;

    public ParserService(String ramlPath, String appHome)
    {
        this.ramlPath = ramlPath;
        setupV2ResourceLoader(appHome);
        checkParserVersion();
        setupParserWrapper(ramlPath, appHome);
    }

    private void setupV2ResourceLoader(String appHome)
    {
        if (appHome != null)
        {
            resourceLoaderV2 = new CompositeResourceLoader(new DefaultResourceLoader(), new FileResourceLoader(appHome));
        }
        else
        {
            resourceLoaderV2 = new DefaultResourceLoader();
        }
    }

    public boolean isParserV2()
    {
        return parserV2;
    }

    private void checkParserVersion()
    {
        String property = System.getProperty(PARSER_V2_PROPERTY);
        if (property != null && Boolean.valueOf(property))
        {
            parserV2 = true;
        }
        else
        {
            InputStream content = resourceLoaderV2.fetchResource(ramlPath);
            if (content != null)
            {
                String dump = StreamUtils.toString(content);
                parserV2 = dump.startsWith("#%RAML 1.0");
            }
        }
        logger.debug("Using parser " + (parserV2 ? "V2" : "V1"));
    }

    private void setupParserWrapper(String ramlPath, String appHome)
    {
        if (parserV2)
        {
            parserWrapper = new ParserWrapperV2(ramlPath, appHome);
        }
        else
        {
            parserWrapper = new ParserWrapperV1(ramlPath, appHome);
        }
    }

    public void validateRaml()
    {
        parserWrapper.validate();
    }

    public IRaml build()
    {
        return parserWrapper.build();
    }

    public RamlUpdater getRamlUpdater(IRaml api, AbstractConfiguration configuration)
    {
        return parserWrapper.getRamlUpdater(api, configuration);
    }

    public String dumpRaml(IRaml api, String oldSchemeHostPort, String newSchemeHostPort)
    {
        return parserWrapper.dump(api, oldSchemeHostPort, newSchemeHostPort);
    }

    public void updateBaseUri(IRaml api, String baseUri)
    {
        parserWrapper.updateBaseUri(api, baseUri);
    }
}
