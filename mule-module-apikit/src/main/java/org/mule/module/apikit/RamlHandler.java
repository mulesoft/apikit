/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.parser.ParserService;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.i18n.I18nMessage;
import org.mule.runtime.api.message.Message;

import com.google.common.net.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.raml.model.ActionType;

public class RamlHandler
{
    public static final String APPLICATION_RAML = "application/raml+yaml";
    private static final String RAML_QUERY_STRING = "raml";
    private static final String DEFAULT_API_RESOURCES_PATH = "api/";

    // private String ramlLocation;
    private boolean keepRamlBaseUri;
//    private Map<String, String> apikitRaml;
    private String apiServer;
    private IRaml api;
    private ParserService parserService;

    private String apiResourcesRelativePath = DEFAULT_API_RESOURCES_PATH;

    //ramlLocation should be the root raml location, relative of the resources folder
    public RamlHandler(String ramlLocation, String apiServer, boolean keepRamlBaseUri) throws IOException
    {
        this.keepRamlBaseUri = keepRamlBaseUri;
        this.apiServer = apiServer;
        URL ramlLocationUrl = this.getClass().getResource(ramlLocation);
        if (ramlLocationUrl == null)
        {
            throw new IOException("Raml resource not found at: " + ramlLocation);
        }
        parserService = new ParserService(ramlLocationUrl.toString());
        parserService.validateRaml();
        this.api = parserService.build();
        if (!keepRamlBaseUri && apiServer != null)
        {
            parserService.updateBaseUri(api, apiServer);
        }

        int idx = ramlLocation.lastIndexOf("/");
        if (idx > 0)
        {
            this.apiResourcesRelativePath = ramlLocation.substring(0, idx + 1);
            if (!apiResourcesRelativePath.startsWith("/"))
            {
                apiResourcesRelativePath = "/" + apiResourcesRelativePath;
            }
            //apiResourcesRelativePath = apiResourcesRelativePath + "?" + RAML_QUERY_STRING;
        }
    }

    public boolean isParserV2() {
        return parserService.isParserV2();
    }

    public IRaml getApi()
    {
        return api;
    }

    public void setApi(IRaml api)
    {
        this.api = api;
    }

    //resourcesRelativePath should not contain the console path
    public String getRamlParserV2(String resourceRelativePath) throws NotFoundException, IOException
    {
        if (resourceRelativePath.equals(apiResourcesRelativePath))
        {
            //root raml
            String rootRaml = parserService.dumpRaml(api);
            if (keepRamlBaseUri)
            {
                return rootRaml;
            }
            return UrlUtils.replaceBaseUri(rootRaml, apiServer);
        }
        else
        {
            //resource
            InputStream apiResource = null;
            ByteArrayOutputStream baos = null;
            try
            {
                resourceRelativePath = sanitarizeResourceRelativePath(resourceRelativePath);
                apiResource = this.getClass().getResourceAsStream(resourceRelativePath);

                if (apiResource == null)
                {
                    throw new NotFoundException(resourceRelativePath);
                }

                baos = new ByteArrayOutputStream();
                StreamUtils.copyLarge(apiResource, baos);
            }
            catch (IOException e)
            {
                throw new NotFoundException(resourceRelativePath);//ResourceNotFoundException(null, null);// fileNotFound(RESOURCE_BASE + path)
            }
            finally
            {
                IOUtils.closeQuietly(apiResource);
                IOUtils.closeQuietly(baos);
            }
            if (baos != null)
            {
                return baos.toString();
            }
            return null;
        }
    }

    public boolean isRequestingRamlV1(HttpRequestAttributes messageAttributes)
    {
        String path = messageAttributes.getRequestPath();
        return (!isParserV2() &&
                path.equals(getApi().getUri()) &&
                ActionType.GET.toString().equals(messageAttributes.getMethod().toUpperCase()) &&
                AttributesHelper.isAnAcceptedResponseMediaType(messageAttributes, APPLICATION_RAML));
    }

    public boolean isRequestingRamlV2(HttpRequestAttributes messageAttributes)
    {
        String consolePath = UrlUtils.getListenerPath(messageAttributes);
        return messageAttributes.getQueryString().equals(RAML_QUERY_STRING)
                && messageAttributes.getRequestPath().startsWith(consolePath + apiResourcesRelativePath);
    }

    private String sanitarizeResourceRelativePath(String resourceRelativePath)
    {
        //delete first slash
        if (resourceRelativePath.startsWith("/") && resourceRelativePath.length() > 1)
        {
            resourceRelativePath = resourceRelativePath.substring(1, resourceRelativePath.length());
        }
        //delete last slash
        if (resourceRelativePath.endsWith("/") && resourceRelativePath.length() > 1)
        {
            resourceRelativePath = resourceRelativePath.substring(0, resourceRelativePath.length()-1);
        }
        return resourceRelativePath;
    }
}