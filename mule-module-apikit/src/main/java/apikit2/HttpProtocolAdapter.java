package apikit2;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.util.StringUtils;

import com.google.common.net.MediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HttpProtocolAdapter
{

    private URI baseURI;
    private URI resourceURI;
    private String method;
    private String acceptableResponseMediaTypes;
    private String requestMediaType;
    private Map<String, Object> queryParams;

    public HttpProtocolAdapter(MuleEvent event)
    {
        MuleMessage message = event.getMessage();
        this.baseURI = event.getMessageSourceURI();
        if (message.getInboundProperty("host") != null)
        {
            String hostHeader = message.getInboundProperty("host");
            if (hostHeader.indexOf(':') != -1)
            {
                String host = hostHeader.substring(0, hostHeader.indexOf(':'));
                int port = Integer.parseInt(hostHeader.substring(hostHeader.indexOf(':') + 1));
                try
                {
                    String requestPath;
                    requestPath = message.getInboundProperty("http.request.path");
                    this.resourceURI = new URI("http", null, host, port, requestPath, null, null);
                }
                catch (URISyntaxException e)
                {
                    throw new IllegalArgumentException("Cannot parse URI", e);
                }
            }
            else
            {
                try
                {
                    String requestPath;
                    requestPath = message.getInboundProperty("http.request.path");
                    this.resourceURI = new URI("http", null, (String) message.getInboundProperty("host"), 80,
                                               requestPath, null, null);
                }
                catch (URISyntaxException e)
                {
                    throw new IllegalArgumentException("Cannot parse URI", e);
                }
            }
        }
        else
        {
            try
            {
                this.resourceURI = new URI("http", null, baseURI.getHost(), baseURI.getPort(),
                                           (String) message.getInboundProperty("http.request.path"), null, null);
            }
            catch (URISyntaxException e)
            {
                throw new IllegalArgumentException("Cannot parse URI", e);
            }
        }
        method = message.getInboundProperty("http.method");

        if (!StringUtils.isBlank((String) message.getInboundProperty("accept")))
        {
            this.acceptableResponseMediaTypes = message.getInboundProperty("accept");
        }

        if (!StringUtils.isBlank((String) message.getInboundProperty("content-type")))
        {
            this.requestMediaType = message.getInboundProperty("content-type");
        }
        if (this.requestMediaType == null
            && !StringUtils.isBlank((String) message.getOutboundProperty("content-type")))
        {
            this.requestMediaType = message.getOutboundProperty("content-type");
        }

        this.queryParams = message.getInboundProperty("http.query.params");
    }

    private List<MediaType> parseAcceptHeader(String acceptHeader)
    {
        List<MediaType> mediaTypes = new LinkedList<MediaType>();
        String[] types = StringUtils.split(acceptHeader, ',');
        if (types != null)
        {
            for (String type : types)
            {
                MediaType mediaType = MediaType.parse(type.trim());
                if (!mediaType.parameters().containsKey("q"))
                {
                    mediaType = mediaType.withParameter("q", "1");
                }
                mediaTypes.add(mediaType);
            }
        }
        return mediaTypes;
    }

    public URI getBaseURI()
    {
        return baseURI;
    }

    public URI getResourceURI()
    {
        return resourceURI;
    }

    public String getMethod()
    {
        return method;
    }

    public String getAcceptableResponseMediaTypes()
    {
        return acceptableResponseMediaTypes;
    }

    public String getRequestMediaType()
    {
        return requestMediaType != null ? requestMediaType.split(";")[0] : null;
    }

    public Map<String, Object> getQueryParams()
    {
        return queryParams;
    }
}
