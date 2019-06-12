/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mule.util.StringUtils.isNotEmpty;

import static org.mule.module.apikit.UrlUtils.getBasePath;
import static org.mule.module.apikit.UrlUtils.getQueryString;
import static org.mule.module.apikit.UrlUtils.getResourceRelativePath;
import static org.mule.module.apikit.uri.URICoder.decode;

public class ConsoleHandler implements MessageProcessor
{

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String MIME_TYPE_JAVASCRIPT = "application/x-javascript";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_GIF = "image/gif";
    public static final String MIME_TYPE_SVG = "image/svg+xml";
    public static final String MIME_TYPE_CSS = "text/css";
    private static final String RESOURCE_BASE = System.getProperty("apikit.console.old") != null ? "/console" : "/console2";
    private static final String CONSOLE_ELEMENT = "<raml-console-loader";
    private static final String CONSOLE_ELEMENT_OLD = "<raml-console";
    private static final String CONSOLE_ATTRIBUTES = "options=\"{disableRamlClientGenerator: true, disableThemeSwitcher: true}\"";
    private static final String CONSOLE_ATTRIBUTES_OLD = "disable-raml-client-generator=\"\" disable-theme-switcher=\"\"";
    private static final String CONSOLE_ATTRIBUTES_PLACEHOLDER = "console-attributes-placeholder";
    private static final String DEFAULT_API_RESOURCES_PATH = "api/";
    private static final String RAML_QUERY_STRING = "raml";
    private static final String EXCHANGE_MODULES = "exchange_modules";

    private String cachedIndexHtml;
    private String embeddedConsolePath;
    private String apiResourcesRelativePath = DEFAULT_API_RESOURCES_PATH;
    private boolean standalone;
    private AbstractConfiguration configuration;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private String consoleBaseUri;

    public ConsoleHandler(String consoleBaseUri, AbstractConfiguration configuration)
    {
        this(consoleBaseUri, "", configuration);
        standalone = true;
    }

    public ConsoleHandler(String consoleBaseUri, String embeddedConsolePath, AbstractConfiguration configuration)
    {
        this.configuration = configuration;
        this.embeddedConsolePath = sanitize(embeddedConsolePath);
        this.consoleBaseUri = consoleBaseUri;
    }

    public void updateRamlUri()
    {
        String relativeRamlUri = getRelativeRamlUri();
        if (relativeRamlUri != null)
        {
            String consoleElement = CONSOLE_ELEMENT;
            String consoleAttributes = CONSOLE_ATTRIBUTES;
            if (isOldConsole())
            {
                consoleElement = CONSOLE_ELEMENT_OLD;
                consoleAttributes = CONSOLE_ATTRIBUTES_OLD;
            }
            InputStream indexInputStream = getClass().getResourceAsStream(RESOURCE_BASE + "/index.html");
            String indexHtml = IOUtils.toString(indexInputStream);
            IOUtils.closeQuietly(indexInputStream);
            indexHtml = indexHtml.replaceFirst(consoleElement + " src=\"[^\"]+\"",
                                               consoleElement + " src=\"" + relativeRamlUri + "\"");
            cachedIndexHtml = indexHtml.replaceFirst(CONSOLE_ATTRIBUTES_PLACEHOLDER, consoleAttributes);
        }
        else
        {
            cachedIndexHtml = "RAML Console is DISABLED.";
        }
    }

    private boolean isOldConsole()
    {
        return RESOURCE_BASE.equals("/console");
    }

    private String getRelativeRamlUri()
    {
        if (configuration.isParserV2())
        {
            //check if raml is in /api dir
            String ramlLocation = configuration.getRaml();
            if (ramlLocation.startsWith(DEFAULT_API_RESOURCES_PATH))
            {
                ramlLocation = ramlLocation.substring(DEFAULT_API_RESOURCES_PATH.length());
            }
            File apiResource = new File(configuration.getAppHome(), "/" + DEFAULT_API_RESOURCES_PATH + ramlLocation);
            if (apiResource.isFile())
            {
                if (ramlLocation.contains("/"))
                {
                    apiResourcesRelativePath += ramlLocation.substring(0, ramlLocation.lastIndexOf("/") + 1);
                }
                return apiResourcesRelativePath + "?" + RAML_QUERY_STRING;
            }

            // check if raml is in a classpath subdir
            ramlLocation = configuration.getRaml();
            int idx = ramlLocation.lastIndexOf("/");
            if (idx > 0)
            {
                this.apiResourcesRelativePath = ramlLocation.substring(0, idx + 1);
                return apiResourcesRelativePath + "?" + RAML_QUERY_STRING;
            }

            logger.error("RAML Console is DISABLED. RAML resources cannot be hosted in the classpath root");
            return null;
        }
        return "./?";
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
        String listenerPath = event.getMessage().getInboundProperty("http.listener.path");
        if (listenerPath != null && !listenerPath.endsWith("/*"))
        {
            throw new IllegalStateException("Console path in listener must end with /*");
        }
        String path = decode(getResourceRelativePath(event.getMessage()));
        String contextPath = getBasePath(event.getMessage());
        String queryString = getQueryString(event.getMessage());

        if (logger.isDebugEnabled())
        {
            logger.debug("Console request: " + path);
        }
        MuleEvent resultEvent;
        InputStream in = null;
        ByteArrayOutputStream baos = null;
        try
        {
            boolean addContentEncodingHeader = false;
            if (path.equals(embeddedConsolePath) && !(contextPath.endsWith("/") && standalone))
            {
                // client redirect
                event.getMessage().setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                                                       String.valueOf(HttpConstants.SC_MOVED_PERMANENTLY));
                String scheme = UrlUtils.getScheme(event.getMessage());
                String host = event.getMessage().getInboundProperty("Host");
                String requestPath = event.getMessage().getInboundProperty("http.request.path");
                String redirectLocation = scheme + "://" + host + requestPath + "/";
                if (isNotEmpty(queryString))
                {
                    redirectLocation += "?" + queryString;
                }
                event.getMessage().setOutboundProperty(HttpConstants.HEADER_LOCATION, redirectLocation);
                return event;
            }
            if (path.equals(embeddedConsolePath) || path.equals(embeddedConsolePath + "/") || path.equals(embeddedConsolePath + "/index.html"))
            {
                path = RESOURCE_BASE + "/index.html";
                in = new ByteArrayInputStream(cachedIndexHtml.getBytes());
            }
            else
            {
                String apiResourcesFullPath = embeddedConsolePath + "/" + apiResourcesRelativePath;
                if (path.startsWith(apiResourcesFullPath))
                {
                    // check for root raml
                    if (path.equals(apiResourcesFullPath) && queryString.equals(RAML_QUERY_STRING))
                    {
                        path += ".raml"; // to set raml mime type
                        in = new ByteArrayInputStream(configuration.getApikitRamlConsole(event).getBytes());
                    }
                    else
                    {
                        String trimResourcesPath = path.substring(apiResourcesFullPath.length());
                        String resultRelativePath = trimResourcesPath.contains(EXCHANGE_MODULES) ?
                          trimResourcesPath.substring(trimResourcesPath.lastIndexOf(EXCHANGE_MODULES)) :
                          trimResourcesPath;
                        final String resourcePath = "/" + apiResourcesRelativePath + resultRelativePath;
                        File apiResource = new File(configuration.getAppHome(), resourcePath);

                        if (apiResource.exists()) {
                            in = new FileInputStream(apiResource);
                        } else if (isNotEmpty(apiResourcesRelativePath) && !"/".equals(apiResourcesRelativePath)){
                            // check if exists in /classes/${apiResourcesRelativePath} dir
                            apiResource = new File(configuration.getAppHome(), "classes/" + resourcePath);
                            in = new FileInputStream(apiResource);
                        }
                    }
                }
                else if (path.startsWith(embeddedConsolePath + "/scripts"))
                {
                    String acceptEncoding = event.getMessage().getInboundProperty("accept-encoding");
                    if (acceptEncoding != null && acceptEncoding.contains("gzip"))
                    {
                        in = getClass().getResourceAsStream(RESOURCE_BASE + path.substring(embeddedConsolePath.length()) + ".gz");
                        addContentEncodingHeader = true;
                    }
                    else
                    {
                        in = getClass().getResourceAsStream(RESOURCE_BASE + path.substring(embeddedConsolePath.length()));
                    }
                }
                else if (path.startsWith(embeddedConsolePath))
                {
                    in = getClass().getResourceAsStream(RESOURCE_BASE + path.substring(embeddedConsolePath.length()));
                }
            }
            if (in == null)
            {
                throw new NotFoundException(path);
            }

            baos = new ByteArrayOutputStream();
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
            resultEvent.getMessage().setOutboundProperty("Access-Control-Allow-Origin", "*");

            if (addContentEncodingHeader)
            {
                resultEvent.getMessage().setOutboundProperty("Content-Encoding", "gzip");
            }
            if (mimetype.equals(MimeTypes.HTML))
            {
                resultEvent.getMessage().setOutboundProperty(HttpConstants.HEADER_EXPIRES, -1); //avoid IE ajax response caching
            }
        }
        catch (IOException e)
        {
            throw new ResourceNotFoundException(HttpMessages.fileNotFound(RESOURCE_BASE + path), event, this);
        }
        finally
        {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(baos);
        }

        return resultEvent;
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
        String url = consoleBaseUri.endsWith("/") ? consoleBaseUri.substring(0, consoleBaseUri.length() - 1) : consoleBaseUri;
        return url + embeddedConsolePath;
    }

}
