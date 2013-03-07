/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.resource;

import org.mule.module.apikit.rest.OperationHandlerException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.operation.AbstractRestOperation;
import org.mule.module.apikit.rest.operation.RestOperation;
import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.transformer.types.MimeTypes;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.mule.util.FilenameUtils;
import org.mule.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

public class StaticResourceCollection extends AbstractRestResource
{

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String MIME_TYPE_JAVASCRIPT = "application/x-javascript";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_GIF = "image/gif";
    public static final String MIME_TYPE_CSS = "text/css";

    protected String directory;

    public StaticResourceCollection(String name, RestResource parentResource, String directory)
    {
        super(name, parentResource);
        this.directory = directory;
        operations = Collections.<RestOperation> singletonList(new StaticResourceCollectionRetreiveAction());
    }

    @Override
    public String getName()
    {
        return name;
    }

    class StaticResourceCollectionRetreiveAction extends AbstractRestOperation
    {
        @Override
        public void handle(RestRequest restRequest) throws RestException
        {
            String path = restRequest.getNextPathElement();
            while (restRequest.hasMorePathElements())
            {
                path = path + "/" + restRequest.getNextPathElement();
            }

            InputStream in = null;
            try
            {
                in = getClass().getResourceAsStream(directory + "/" + path);
                if (in == null)
                {
                    restRequest.getMuleEvent().getMessage().setOutboundProperty("http.status", 404);
                    throw new ResourceNotFoundException(path);
                }

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

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copyLarge(in, baos);

                byte[] buffer = baos.toByteArray();

                restRequest.getMuleEvent().getMessage().setPayload(buffer);
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                        String.valueOf(HttpConstants.SC_OK));
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, mimeType);
                restRequest.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, buffer.length);
            }
            catch (IOException e)
            {
                throw new ResourceNotFoundException(path);
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        throw new OperationHandlerException(e);
                    }
                }
            }
        }

        @Override
        public RestOperationType getType()
        {
            return RestOperationType.RETRIEVE;
        }

    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE);
    }

    @Override
    public void appendSwaggerJson(JsonGenerator jsonGenerator) throws JsonGenerationException, IOException
    {
        super.appendSwaggerJson(jsonGenerator);
    }

}
