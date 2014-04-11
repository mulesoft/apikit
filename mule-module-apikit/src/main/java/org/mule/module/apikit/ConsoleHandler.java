/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit;

import static org.mule.module.apikit.Configuration.BIND_ALL_HOST;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.module.apikit.exception.NotFoundException;
import org.mule.transformer.types.MimeTypes;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.mule.transport.http.components.ResourceNotFoundException;
import org.mule.transport.http.i18n.HttpMessages;
import org.mule.util.FilenameUtils;
import org.mule.util.IOUtils;
import org.mule.util.StringMessageUtils;
import org.mule.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleHandler
{

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String MIME_TYPE_JAVASCRIPT = "application/x-javascript";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_GIF = "image/gif";
    public static final String MIME_TYPE_CSS = "text/css";

    private static final String CONSOLE_URL_FILE = "consoleurl";

    private static final String RESOURCE_BASE = "/console";

    private Map<String, String> homePage = new ConcurrentHashMap<String, String>();
    private String consolePath;
    private String baseHost;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private String ramlUri;

    public ConsoleHandler(String ramlUri, String consolePath) throws InitialisationException
    {
        this.consolePath = sanitize(consolePath);
        String indexHtml = IOUtils.toString(getClass().getResourceAsStream("/console/index.html"));
        this.ramlUri = ramlUri.endsWith("/") ? ramlUri : ramlUri + "/";
        String baseHomePage = indexHtml.replaceFirst("<raml-console src=\"[^\"]+\"", "<raml-console src=\""
                                                                                     + this.ramlUri + "\"");
        try
        {
            baseHost = new URI(this.ramlUri).getHost();
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        homePage.put(baseHost, baseHomePage);
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

        String path = event.getMessage().getInboundProperty(HttpConnector.HTTP_REQUEST_PATH_PROPERTY);
        String contextPath = event.getMessage().getInboundProperty(HttpConnector.HTTP_CONTEXT_PATH_PROPERTY);

        // Remove the contextPath from the endpoint from the request as this isn't
        // part of the path.
        path = path.substring(contextPath.length());
        if (!path.startsWith("/") && !path.isEmpty())
        {
            path = "/" + path;
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Console request: " + path);
        }
        MuleEvent resultEvent;
        InputStream in = null;
        try
        {
            if (path.equals(consolePath))
            {
                // client redirect
                event.getMessage().setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                    String.valueOf(HttpConstants.SC_MOVED_PERMANENTLY));
                String context = event.getMessage().getInboundProperty("http.context.uri");
                String scheme = context.substring(0, context.indexOf("/"));
                String host = event.getMessage().getInboundProperty("Host");
                String requestPath = event.getMessage().getInboundProperty("http.request.path");
                String redirectLocation = scheme + "//" + host + requestPath + "/";
                String queryString = event.getMessage().getInboundProperty("http.query.string");
                if (StringUtils.isNotEmpty(queryString))
                {
                    redirectLocation += "?" + queryString;
                }
                event.getMessage().setOutboundProperty(HttpConstants.HEADER_LOCATION, redirectLocation);
                return event;
            }
            if (path.equals(consolePath + "/") || path.equals(consolePath + "/index.html"))
            {
                path = RESOURCE_BASE + "/index.html";
                String host = event.getMessage().getInboundProperty("host");
                if (host.contains(":"))
                {
                    host = host.split(":")[0];
                }
                in = new ByteArrayInputStream(getHomePage(host).getBytes());
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
        }
        catch (IOException e)
        {
            throw new ResourceNotFoundException(HttpMessages.fileNotFound(RESOURCE_BASE + path), event);
        }

        return resultEvent;
    }

    private String getHomePage(String host)
    {
        if (!BIND_ALL_HOST.equals(baseHost))
        {
            return homePage.get(baseHost);
        }

        String page = homePage.get(host);
        if (page == null)
        {
            page = homePage.get(baseHost).replace(BIND_ALL_HOST, host);
            homePage.put(host, page);
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
        else if (FilenameUtils.getExtension(path).equals("css"))
        {
            mimeType = MIME_TYPE_CSS;
        }
        return mimeType;
    }

    public void publishConsoleUrl(String parentDirectory)
    {
        String path = "";
        if (consolePath.startsWith("/"))
        {
            path = consolePath.substring(1, consolePath.length());
        }
        String consoleUrl = ramlUri + path;
        if (!consoleUrl.isEmpty())
        {
            File urlFile = new File(parentDirectory, CONSOLE_URL_FILE);
            FileWriter writer = null;
            try
            {
                if (!urlFile.exists())
                {
                    urlFile.createNewFile();
                }
                writer = new FileWriter(urlFile, true);
                writer.write(consoleUrl + "\n");
                writer.flush();
            }
            catch (IOException e)
            {
                logger.error("cannot publish console url for studio", e);
            }
            finally
            {
                IOUtils.closeQuietly(writer);
            }

            if (logger.isInfoEnabled())
            {
                String msg = String.format("APIKit Console URL: %s", consoleUrl);
                logger.info(StringMessageUtils.getBoilerPlate(msg));
            }
        }
    }

}
