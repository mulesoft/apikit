/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.module.apikit.helpers.AttributesHelper;
import org.mule.module.apikit.parser.ParserService;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.core.exception.TypedException;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.raml.model.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RamlHandler
{
    public static final String APPLICATION_RAML = "application/raml+yaml";
    private static final String RAML_QUERY_STRING = "raml";

    private boolean keepRamlBaseUri;
    private String apiServer;
    private IRaml api;
    private ParserService parserService;

    private String apiResourcesRelativePath = "";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    //ramlLocation should be the root raml location, relative of the resources folder
    public RamlHandler(String ramlLocation, boolean keepRamlBaseUri) throws IOException
    {
        this.keepRamlBaseUri = keepRamlBaseUri;

        String rootRamlLocation = findRootRaml(ramlLocation);
        if (rootRamlLocation == null)
        {
            throw new IOException("Raml not found at: " + ramlLocation);
        }
        parserService = new ParserService(rootRamlLocation);
        parserService.validateRaml();
        this.api = parserService.build();

        int idx = rootRamlLocation.lastIndexOf("/");
        if (idx > 0)
        {
            this.apiResourcesRelativePath = rootRamlLocation.substring(0, idx + 1);
            this.apiResourcesRelativePath = sanitarizeResourceRelativePath(apiResourcesRelativePath);
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

    public String getRamlV1() {
        if (keepRamlBaseUri)
        {
            return parserService.dumpRaml(api);
        }
        else
        {
            return parserService.dumpRaml(api, apiServer);
        }
    }


    //resourcesRelativePath should not contain the console path
    public String getRamlV2(String resourceRelativePath) throws TypedException
    {
        resourceRelativePath = sanitarizeResourceRelativePath(resourceRelativePath);
        if (resourceRelativePath.contains(".."))
        {
            throw ApikitErrorTypes.throwErrorTypeNew(new NotFoundException("\"..\" is not allowed"));
        }
        if (apiResourcesRelativePath.equals(resourceRelativePath))
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
            //the resource should be in a subfolder, otherwise it could be requesting the properties file
            if (!resourceRelativePath.contains("/"))
            {
                throw ApikitErrorTypes.throwErrorTypeNew(new NotFoundException("Requested resources should be in a subfolder"));
            }
            //resource
            InputStream apiResource = null;
            ByteArrayOutputStream baos = null;
            try
            {
                apiResource = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceRelativePath);

                if (apiResource == null)
                {
                    throw ApikitErrorTypes.throwErrorTypeNew(new NotFoundException(resourceRelativePath));
                }

                baos = new ByteArrayOutputStream();
                StreamUtils.copyLarge(apiResource, baos);
            }
            catch (IOException e)
            {
                logger.debug(e.getMessage());
                throw ApikitErrorTypes.throwErrorTypeNew(new NotFoundException(resourceRelativePath));
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
        String listenerPath = UrlUtils.getListenerPath(messageAttributes);// messageAttributes.getRequestPath();
        return (!isParserV2() &&
                listenerPath.equals(messageAttributes.getRequestPath()) &&
                ActionType.GET.toString().equals(messageAttributes.getMethod().toUpperCase()) &&
                APPLICATION_RAML.equals(AttributesHelper.getHeaderIgnoreCase(messageAttributes,"Accept")));
    }

    public boolean isRequestingRamlV1ForConsole(HttpRequestAttributes messageAttributes)
    {
        String listenerPath = UrlUtils.getListenerPath(messageAttributes);
        return (!isParserV2() &&
                (listenerPath.equals(messageAttributes.getRequestPath()) ||
                        (listenerPath + "/").equals(messageAttributes.getRequestPath())) &&
                ActionType.GET.toString().equals(messageAttributes.getMethod().toUpperCase()) &&
                (APPLICATION_RAML.equals(AttributesHelper.getHeaderIgnoreCase(messageAttributes,"Accept"))
                        || messageAttributes.getQueryString().equals(RAML_QUERY_STRING)));
    }

    public boolean isRequestingRamlV2(HttpRequestAttributes messageAttributes)
    {
        String consolePath = UrlUtils.getListenerPath(messageAttributes);
        String resourcesFullPath = consolePath;
        if (!consolePath.endsWith("/"))
        {
            if (!apiResourcesRelativePath.startsWith("/"))
            {
                resourcesFullPath += "/";
            }
            resourcesFullPath += apiResourcesRelativePath;
        }
        else
        {
            if (apiResourcesRelativePath.startsWith("/") && apiResourcesRelativePath.length() >1)
            {
                resourcesFullPath += apiResourcesRelativePath.substring(1);
            }
        }
        return isParserV2() && messageAttributes.getQueryString().equals(RAML_QUERY_STRING)
               && ActionType.GET.toString().equals(messageAttributes.getMethod().toUpperCase())
                && messageAttributes.getRequestPath().startsWith(resourcesFullPath);
    }

    private String sanitarizeResourceRelativePath(String resourceRelativePath)
    {
        //delete first slash
        if (resourceRelativePath.startsWith("/") && resourceRelativePath.length() > 1)
        {
            resourceRelativePath = resourceRelativePath.substring(1, resourceRelativePath.length());
        }
        //delete querystring
        if (resourceRelativePath.contains("?raml"))
        {
            resourceRelativePath = resourceRelativePath.substring(0,resourceRelativePath.indexOf('?'));
        }
        //delete last slash
        if (resourceRelativePath.endsWith("/") && resourceRelativePath.length() > 1)
        {
            resourceRelativePath = resourceRelativePath.substring(0, resourceRelativePath.length()-1);
        }
        return resourceRelativePath;
    }

    private String findRootRaml(String ramlLocation)
    {
        String[] startingLocations = new String[]{"", "api/", "api"};
        for (String start : startingLocations)
        {
            URL ramlLocationUrl = Thread.currentThread().getContextClassLoader().getResource(start + ramlLocation);
            if (ramlLocationUrl != null)
            {
                return start + ramlLocation;
            }
        }
        return null;
    }

    public String getRootRamlLocationForV2() {
        return "this.location.href" + " + '" +  apiResourcesRelativePath + "/?" + RAML_QUERY_STRING + "'";
    }

    public String getRootRamlLocationForV1() {
        return "this.location.href" + " + '" +  "?" + RAML_QUERY_STRING + "'";
    }

    public String getSuccessStatusCode(IAction action)
    {

        for (String status : action.getResponses().keySet())
        {
            int code = Integer.parseInt(status);
            if (code >= 200 && code < 300)
            {
                return status;
            }
        }
        //default success status
        return "200";
    }

    public void setApiServer(String apiServer)
    {
        this.apiServer = apiServer;
    }
}