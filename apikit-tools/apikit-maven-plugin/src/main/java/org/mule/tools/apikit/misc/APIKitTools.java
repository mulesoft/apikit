package org.mule.tools.apikit.misc;

import org.jdom2.Namespace;
import org.mule.tools.apikit.output.NamespaceWithLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class APIKitTools {
    public static final NamespaceWithLocation API_KIT_NAMESPACE = new NamespaceWithLocation(
            Namespace.getNamespace("apikit", "http://www.mulesoft.org/schema/mule/apikit"),
            "http://www.mulesoft.org/schema/mule/apikit/current/mule-apikit.xsd"
    );

    public static String getPathFromUri(String baseUri) {
        List<String> split = new ArrayList<String>(Arrays.asList(baseUri.split("/")));

        Collections.reverse(split);

        String path = null;
        for (String s : split) {
            if (!"".equals(s)) {
                path = s;
                break;
            }
        }

        if (path != null && !path.startsWith("/")) {
            path = "/" + path;
        }

        return path;

    }
}
