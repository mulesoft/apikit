package org.mule.module.apikit;

public class Configuration
{

    private String name;
    private String raml;
    private boolean consoleEnabled;
    private String consolePath;

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
}
