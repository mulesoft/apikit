package org.mule.module.metadata.model;

/**
 * A RAML coordinate. It is composed by a method, a resource, a media type (optional), and a APIkit config name (optional).
 */
public class RamlCoordinate
{
    private String method;
    private String resource;
    private String mediaType;
    private String configName;

    public RamlCoordinate(String method, String resource, String mediaType, String configName) {
        this.resource = resource;
        this.method = method;
        this.mediaType = mediaType;
        this.configName = configName;
    }

    public String getConfigName()
    {
        return configName;
    }

    public String getResource()
    {
        return resource;
    }

    public String getMediaType()
    {
        return mediaType;
    }

    public String getMethod()
    {
        return method;
    }

}
