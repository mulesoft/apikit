package org.mule.tools.apikit.input;

public class APIKitFlow {

    private final String action;
    private final String resource;
    private final String configRef;

    public APIKitFlow(final String action, final String resource) {
        this(action, resource, null);
    }

    public APIKitFlow(final String action, final String resource, String configRef) {
        this.action = action;
        this.resource = resource;
        this.configRef = configRef;
    }

    public String getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }

    public String configRef() {
        return configRef;
    }
}
