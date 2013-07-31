package org.mule.module.apikit;

import java.util.ArrayList;
import java.util.List;

public class Configuration
{

    private String name;
    private String raml;
    private boolean consoleEnabled;
    private String consolePath;
    private List<FlowMapping> flowMappings = new ArrayList<FlowMapping>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getRaml()
    {
        return raml;
    }

    public void setRaml(String raml)
    {
        this.raml = raml;
    }

    public boolean isConsoleEnabled()
    {
        return consoleEnabled;
    }

    public void setConsoleEnabled(boolean consoleEnabled)
    {
        this.consoleEnabled = consoleEnabled;
    }

    public String getConsolePath()
    {
        return consolePath;
    }

    public void setConsolePath(String consolePath)
    {
        this.consolePath = consolePath;
    }

    public List<FlowMapping> getFlowMappings()
    {
        return flowMappings;
    }

    public void setFlowMappings(List<FlowMapping> flowMappings)
    {
        this.flowMappings = flowMappings;
    }
}
