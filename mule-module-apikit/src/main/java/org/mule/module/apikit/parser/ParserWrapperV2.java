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

import com.google.common.collect.Lists;

import java.io.InputStream;
import java.util.List;

import org.raml.v2.api.loader.ClassPathResourceLoader;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.internal.utils.StreamUtils;
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
        List<ResourceLoader> resourceLoaders = Lists.newArrayList((ResourceLoader) new DefaultResourceLoader());
        if (appHome != null)
        {
            resourceLoaders.add(new FileResourceLoader(appHome));
        }
        if (ramlPath.matches(".+/.+"))
        {
            resourceLoaders.add(0, new ClassPathResourceLoader(ramlPath.substring(0, ramlPath.lastIndexOf("/"))));
        }
        if (resourceLoaders.size() > 1)
        {
            this.resourceLoader = new CompositeResourceLoader(resourceLoaders.toArray(new ResourceLoader[]{}));
        }
        else
        {
            this.resourceLoader = resourceLoaders.get(0);
        }
    }

    @Override
    public void validate()
    {
        List<String> errors = ParserV2Utils.validate(resourceLoader, ramlPath);
        if (!errors.isEmpty())
        {
            StringBuilder message = new StringBuilder("Invalid API descriptor -- errors found: ");
            message.append(errors.size()).append("\n\n");
            for (String error : errors)
            {
                message.append(error).append("\n");
            }
            throw new ApikitRuntimeException(message.toString());
        }
    }

    @Override
    public IRaml build()
    {
        return ParserV2Utils.build(resourceLoader, ramlPath);
    }

    @Override
    public String dump(String ramlContent, IRaml api, String oldSchemeHostPort, String newSchemeHostPort)
    {
        return UrlUtils.rewriteBaseUri(ramlContent, newSchemeHostPort);
    }

    @Override
    public String dump(IRaml api, String newBaseUri)
    {
        String dump = dumpRaml(api);
        if (newBaseUri != null)
        {
            dump = UrlUtils.replaceBaseUri(dump, newBaseUri);
        }
        return dump;
    }

    private String dumpRaml(IRaml api)
    {
        InputStream stream = resourceLoader.fetchResource(ramlPath);
        if (stream == null)
        {
            throw new ApikitRuntimeException("Invalid RAML descriptor");
        }
        return StreamUtils.toString(stream);
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
