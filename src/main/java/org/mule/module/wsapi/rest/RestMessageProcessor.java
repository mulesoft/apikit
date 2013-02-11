
package org.mule.module.wsapi.rest;

import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.module.wsapi.rest.action.ActionType;
import org.mule.module.wsapi.rest.action.RestAction;
import org.mule.module.wsapi.rest.resource.AbstractRestResource;
import org.mule.transformer.types.MimeTypes;
import org.mule.transport.http.HttpConnector;
import org.mule.transport.http.HttpConstants;
import org.mule.transport.http.components.ResourceNotFoundException;
import org.mule.transport.http.i18n.HttpMessages;
import org.mule.util.FilenameUtils;
import org.mule.util.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestMessageProcessor extends AbstractRestResource
{
    public static final String RESOURCE_BASE_PATH = "/org/mule/modules/rest/swagger/";
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    public static final String MIME_TYPE_JAVASCRIPT = "application/x-javascript";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_GIF = "image/gif";
    public static final String MIME_TYPE_CSS = "text/css";

    public RestMessageProcessor(RestWebServiceInterface restInterface)
    {
        this.restInterface = restInterface;
        setRoutes(restInterface.getRoutes());
        logger.debug("Creating REST resource hierarchy and updating routing table...");
        buildRoutingTable();
        actions = Collections.<RestAction> unmodifiableList(Collections.singletonList(new NestedRetreiveAction()));
    }

    @Override
    public Set<ActionType> getSupportedActions()
    {
        return Collections.singleton(ActionType.RETRIEVE);
    }

    class NestedRetreiveAction implements RestAction
    {
        @Override
        public MessageProcessor getHandler()
        {
            return null;
        }

        @Override
        public MuleEvent process(MuleEvent event) throws MuleException
        {
            String path = event.getMessage().getInboundProperty(HttpConnector.HTTP_REQUEST_PATH_PROPERTY);
            if (getProtocolAdapter(event).getAcceptedContentTypes().equalsIgnoreCase(
                "application/swagger+json")
                || getProtocolAdapter(event).getAcceptedContentTypes().equalsIgnoreCase("*/*"))
            {
                try
                {
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(restInterface);
                    json = json.replace("{baseSwaggerUri}", event.getMessageSourceURI().toString());

                    event.getMessage().setPayload(json);
                    event.getMessage().setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                        String.valueOf(HttpConstants.SC_OK));
                    event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, MimeTypes.JSON);
                    event.getMessage()
                        .setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, json.length());
                    return event;

                }
                catch (JsonProcessingException e)
                {
                    throw new MessagingException(event, e);
                }
            }
            else if (getProtocolAdapter(event).getAcceptedContentTypes().contains("text/html"))
            {
                InputStream in = null;
                try
                {
                    in = getClass().getResourceAsStream(RESOURCE_BASE_PATH + "index.html");
                    if (in == null)
                    {
                        throw new ResourceNotFoundException(HttpMessages.fileNotFound("index.html"), event);
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.copyLarge(in, baos);

                    String buffer = new String(baos.toByteArray());

                    buffer = buffer.replace("${swaggerUrl}", event.getMessageSourceURI().toString());

                    // urlBuilder.append("/resources.json");
                    //
                    // buffer = buffer.replace("${resourcesJson}", urlBuilder.toString());

                    buffer = buffer.replace("${pageTitle}", restInterface.getName() + " UI");

                    event.getMessage().setPayload(buffer);
                    event.getMessage().setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                        String.valueOf(HttpConstants.SC_OK));
                    event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, MimeTypes.HTML);
                    event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH,
                        buffer.length());
                    return event;
                }
                catch (JsonProcessingException e)
                {
                    throw new MessagingException(event, e);
                }
                catch (IOException e)
                {
                    throw new MessagingException(event, e);
                }
            }
            else if (path.endsWith(".png") || path.endsWith(".js") || path.endsWith(".css")
                     || path.endsWith(".html") || path.endsWith(".gif"))
            {
                InputStream in = null;
                try
                {
                    in = getClass().getResourceAsStream(RESOURCE_BASE_PATH + path);
                    if (in == null)
                    {
                        event.getMessage().setOutboundProperty("http.status", 404);
                        throw new ResourceNotFoundException(HttpMessages.fileNotFound(path), event);
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

                    event.getMessage().setPayload(buffer);
                    event.getMessage().setOutboundProperty(HttpConnector.HTTP_STATUS_PROPERTY,
                        String.valueOf(HttpConstants.SC_OK));
                    event.getMessage().setOutboundProperty(HttpConstants.HEADER_CONTENT_TYPE, mimeType);
                    event.getMessage()
                        .setOutboundProperty(HttpConstants.HEADER_CONTENT_LENGTH, buffer.length);
                }
                catch (IOException e)
                {
                    throw new ResourceNotFoundException(HttpMessages.fileNotFound(path), event);
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
                            throw new MessagingException(event, e);
                        }
                    }
                }

                return event;

            }
            else
            {
                getProtocolAdapter(event).statusNotAcceptable(event);
                return event;
            }
        }

        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public ActionType getType()
        {
            return ActionType.RETRIEVE;
        }
    }
}
