package org.mule.module.metadata.model;

public class FlowMapping
{
    private String resource;
    private String action;
    private String contentType;
    private String flowRef;
    private String configName;

    public FlowMapping(String configName, String resource, String action, String contentType, String flowRef) {
        this.configName = configName;
        this.resource = resource;
        this.action = action;
        this.contentType = contentType;
        this.flowRef = flowRef;
    }

    public String getResource()
    {
        return resource;
    }

    public String getAction()
    {
        return action;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getFlowRef()
    {
        return flowRef;
    }

    public String getConfigName()
    {
        return configName;
    }
}
