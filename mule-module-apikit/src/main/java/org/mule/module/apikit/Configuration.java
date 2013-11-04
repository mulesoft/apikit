/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
