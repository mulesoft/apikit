/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit;

import static org.mule.module.apikit.UrlUtils.getBaseSchemeHostPort;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.transformer.types.MimeTypes;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.mule.transport.http.components.ResourceNotFoundException;
import org.mule.transport.http.i18n.HttpMessages;
import org.mule.util.FilenameUtils;
import org.mule.util.IOUtils;
import org.mule.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleHandler implements MessageProcessor
{

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String MIME_TYPE_JAVASCRIPT = "application/x-javascript";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_GIF = "image/gif";
    public static final String MIME_TYPE_SVG = "image/svg+xml";
    public static final String MIME_TYPE_CSS = "text/css";
    private static final String RESOURCE_BASE = System.getProperty("apikit.console.old") != null ? "/console" : "/console2";
    private static final String API_RESOURCES_PATH = "/api/";
    private static final String RAML_QUERY_STRING = "raml";

    private Map<String, String> homePage = new ConcurrentHashMap<String, String>();
    private String consolePath;
    private String baseSchemeHostPort;
    private boolean standalone;
    private AbstractConfiguration configuration;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private String consoleBaseUri;
    private String ramlUri;

    public ConsoleHandler(String consoleBaseUri, AbstractConfiguration configuration)
    {
        this(consoleBaseUri, "", configuration);
        standalone = true;
    }

    public ConsoleHandler(String consoleBaseUri, String consolePath, AbstractConfiguration configuration)
    {
        this.configuration = configuration;
        this.consolePath = sanitize(consolePath);
        this.consoleBaseUri = consoleBaseUri;
    }

    public void updateRamlUri()
    {
        String indexHtml = IOUtils.toString(getClass().getResourceAsStream(RESOURCE_BASE + "/index.html"));
        this.ramlUri = calculateRamlUri(consoleBaseUri);
        String baseHomePage = indexHtml.replaceFirst("<raml-console src=\"[^\"]+\"",
                                                     "<raml-console src=\"" + this.ramlUri + "\"");
        baseSchemeHostPort = getBaseSchemeHostPort(this.ramlUri);
        homePage.put(baseSchemeHostPort, baseHomePage);
    }

    private String calculateRamlUri(String consoleBaseUri)
    {
        if (configuration.isParserV2())
        {
            if (consoleBaseUri.endsWith("/"))
            {
                consoleBaseUri = consoleBaseUri.substring(0, consoleBaseUri.length() - 1);
            }
            return consoleBaseUri + consolePath + API_RESOURCES_PATH;
        }
        return consoleBaseUri.endsWith("/") ? consoleBaseUri : consoleBaseUri + "/";
    }

    private String sanitize(String consolePath)
    {
        if (consolePath.endsWith("/"))
        {
            consolePath = consolePath.substring(0, consolePath.length() - 1);
        }
        if (!consolePath.isEmpty() && !consolePath.startsWith("/"))
        {
            consolePath = "/" + consolePath;
        }
        return consolePath;
    }

    public MuleEvent process(MuleEvent event) throws MuleException
    {

        String path = UrlUtils.getResourceRelativePath(event.getMessage());
        String contextPath = UrlUtils.getBasePath(event.getMessage());
        String queryString = UrlUtils.getQueryString(event.getMessage());

        if (logger.isDebugEnabled())
        {
            logger.debug("Console request: " + path);
        }
        MuleEvent resultEvent;
        InputStream in = null;
        try
        {
            if (path.equals(consolePath) && !(contextPath.endsWith("/") && standalone))
            {
                // client redirect
                event.getMessage().setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                                                       String.valueOf(HttpConstants.SC_MOVED_PERMANENTLY));
                String scheme = UrlUtils.getScheme(event.getMessage());
                String host = event.getMessage().getInboundProperty("Host");
                String requestPath = event.getMessage().getInboundProperty("http.request.path");
                String redirectLocation = scheme + "://" + host + requestPath + "/";
                if (StringUtils.isNotEmpty(queryString))
                {
                    redirectLocation += "?" + queryString;
                }
                event.getMessage().setOutboundProperty(HttpConstants.HEADER_LOCATION, redirectLocation);
                return event;
            }
            if (path.equals(consolePath) || path.equals(consolePath + "/") || path.equals(consolePath + "/index.html"))
            {
                path = RESOURCE_BASE + "/index.html";
                in = new ByteArrayInputStream(getHomePage(getBaseSchemeHostPort(event)).getBytes());
            }
            else if (path.startsWith(consolePath + API_RESOURCES_PATH))
            {
                // check for root raml
                if (path.equals(consolePath + API_RESOURCES_PATH) && queryString.equals(RAML_QUERY_STRING))
                {
                    path += ".raml"; // to set raml mime type
                    in = new ByteArrayInputStream(configuration.getApikitRaml(event).getBytes());
                }
                else
                {
                    String resourcePath = API_RESOURCES_PATH + path.substring((consolePath + API_RESOURCES_PATH).length());
                    File apiResource = new File(configuration.getAppHome(), resourcePath);
                    in = new FileInputStream(apiResource);
                }
            }
            else if (path.startsWith(consolePath))
            {
                in = getClass().getResourceAsStream(RESOURCE_BASE + path.substring(consolePath.length()));
            }
            if (in == null)
            {
                throw new NotFoundException(path);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copyLarge(in, baos);

            byte[] buffer = baos.toByteArray();

            String mimetype = getMimeType(path);
            if (mimetype == null)
            {
                mimetype = DEFAULT_MIME_TYPE;
            }

            resultEvent = new DefaultMuleEvent(new DefaultMuleMessage(buffer, event.getMuleContext()), event);
            resultEvent.getMessage().setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                                                         String.valueOf(HttpConstants.SC_OK));
            resultEvent.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, mimetype);
            resultEvent.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, buffer.length);
            if (mimetype.equals(MimeTypes.HTML))
            {
                resultEvent.getMessage().setOutboundProperty(HttpConstants.HEADER_EXPIRES, -1); //avoid IE ajax response caching
            }
        }
        catch (IOException e)
        {
            throw new ResourceNotFoundException(HttpMessages.fileNotFound(RESOURCE_BASE + path), event, this);
        }

        return resultEvent;
    }

    private String getHomePage(String schemeHostPort)
    {
        if (schemeHostPort == null)
        {
            return homePage.get(baseSchemeHostPort);
        }

        String page = homePage.get(schemeHostPort);
        if (page == null)
        {
            page = homePage.get(baseSchemeHostPort).replace(baseSchemeHostPort, schemeHostPort);
            homePage.put(schemeHostPort, page);
        }
        return page;
    }

    private String getMimeType(String path)
    {
        String mimeType = DEFAULT_MIME_TYPE;
        if (FilenameUtils.getExtension(path).equals("html"))
        {
            mimeType = MimeTypes.HTML;
        }
        else if (FilenameUtils.getExtension(path).equals("js"))
        {
            mimeType = MIME_TYPE_JAVASCRIPT;
        }
        else if (FilenameUtils.getExtension(path).equals("png"))
        {
            mimeType = MIME_TYPE_PNG;
        }
        else if (FilenameUtils.getExtension(path).equals("gif"))
        {
            mimeType = MIME_TYPE_GIF;
        }
        else if (FilenameUtils.getExtension(path).equals("svg"))
        {
            mimeType = MIME_TYPE_SVG;
        }
        else if (FilenameUtils.getExtension(path).equals("css"))
        {
            mimeType = MIME_TYPE_CSS;
        }
        else if (FilenameUtils.getExtension(path).equals("raml"))
        {
            mimeType = AbstractConfiguration.APPLICATION_RAML;
        }
        return mimeType;
    }

    public String getConsoleUrl()
    {
        String path = "";
        if (consolePath.startsWith("/"))
        {
            path = consolePath.substring(1, consolePath.length());
        }
        return consoleBaseUri + path;
    }

}
