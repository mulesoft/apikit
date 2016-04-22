/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.parser;

import org.mule.module.apikit.AbstractConfiguration;
import org.mule.module.apikit.UrlUtils;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.injector.RamlUpdater;
import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.interfaces.model.IRaml;

import java.io.InputStream;
import java.util.List;

import org.raml.v2.RamlBuilder;
import org.raml.v2.loader.CompositeResourceLoader;
import org.raml.v2.loader.DefaultResourceLoader;
import org.raml.v2.loader.FileResourceLoader;
import org.raml.v2.loader.ResourceLoader;
import org.raml.v2.nodes.ErrorNode;
import org.raml.v2.nodes.Node;
import org.raml.v2.nodes.Position;
import org.raml.v2.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserWrapperV2 implements ParserWrapper
{

    private static final Logger logger = LoggerFactory.getLogger(ParserWrapperV2.class);

    private final String ramlPath;
    private final ResourceLoader resourceLoader;

    public ParserWrapperV2(String ramlPath, String appHome)
    {
        this.ramlPath = ramlPath;
        if (appHome != null)
        {
            this.resourceLoader = new CompositeResourceLoader(new DefaultResourceLoader(), new FileResourceLoader(appHome));
        }
        else
        {
            this.resourceLoader = new DefaultResourceLoader();
        }
    }

    @Override
    public void validate()
    {
        String errorMessage = null;
        InputStream content = resourceLoader.fetchResource(ramlPath);
        if (content != null)
        {
            RamlBuilder builder = new RamlBuilder();
            Node raml = builder.build(StreamUtils.reader(content), resourceLoader, ramlPath);
            List<ErrorNode> errors = raml.findDescendantsWith(ErrorNode.class);
            if (!errors.isEmpty())
            {
                errorMessage = errorSummary(errors);
            }
        }
        else
        {
            errorMessage = "Raml resource not found ";
        }
        if (errorMessage != null)
        {
            throw new ApikitRuntimeException(errorMessage);
        }
    }

    private String errorSummary(List<ErrorNode> errors)
    {
        StringBuilder message = new StringBuilder("Invalid API descriptor -- errors found: ");
        message.append(errors.size()).append("\n\n");
        for (ErrorNode error : errors)
        {
            message.append(error.getErrorMessage()).append(" -- file: ");
            Position startPosition = error.getStartPosition();
            if (startPosition.getResource() != null)
            {
                message.append(startPosition.getResource());
            }
            else
            {
                message.append(ramlPath);
            }
            if (startPosition.getLine() >= 0)
            {
                message.append(" -- line ");
                message.append(startPosition.getLine());
            }
            message.append("\n");
        }
        return message.toString();
    }

    @Override
    public IRaml build()
    {
        return ParserV2Utils.build(resourceLoader, ramlPath);
    }

    @Override
    public String dump(IRaml api, String oldSchemeHostPort, String newSchemeHostPort)
    {
        String dump = StreamUtils.toString(resourceLoader.fetchResource(ramlPath));
        dump = UrlUtils.rewriteBaseUri(dump, newSchemeHostPort);
        return dump;
    }

    @Override
    public RamlUpdater getRamlUpdater(IRaml api, AbstractConfiguration configuration)
    {
        throw new UnsupportedOperationException("RAML 1.0 is read only");
    }

    @Override
    public void updateBaseUri(IRaml api, String baseUri)
    {
        // do nothing, as updates are not supported
        logger.debug("RAML 1.0 parser does not support base uri updates");
    }
}
