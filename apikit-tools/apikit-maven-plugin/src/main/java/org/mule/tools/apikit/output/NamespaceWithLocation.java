package org.mule.tools.apikit.output;

import org.jdom2.Namespace;

public class NamespaceWithLocation {
    private final Namespace namespace;
    private final String location;

    public NamespaceWithLocation(Namespace namespace, String location) {
        this.namespace = namespace;
        this.location = location;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public String getLocation() {
        return location;
    }
}
