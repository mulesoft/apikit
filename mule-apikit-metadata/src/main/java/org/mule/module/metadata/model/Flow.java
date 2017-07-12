package org.mule.module.metadata.model;

public class Flow
{
    private static final String PARAMETER_NAME = "name";

    private String name;

    public Flow(String name) {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
