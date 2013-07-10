package org.mule.tools.apikit.misc;

import org.jdom2.Namespace;
import org.mule.tools.apikit.output.NamespaceWithLocation;

public class APIKitTools {
    public static final NamespaceWithLocation API_KIT_NAMESPACE = new NamespaceWithLocation(
            Namespace.getNamespace("apikit", "http://www.mulesoft.org/schema/mule/apikit"),
            "http://www.mulesoft.org/schema/mule/apikit/current/mule-apikit.xsd"
    );
}
