package org.mule.tools.apikit.input.parsers;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.ResourceActionPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class APIKitFlowsParser implements MuleConfigFileParser {

    private final Set<API> includedApis;

    public APIKitFlowsParser(final Set<API> includedApis) {
        this.includedApis = includedApis;
    }

    @Override
    public Set<ResourceActionPair> parse(Document document)  {
        Set<ResourceActionPair> entries = new HashSet<ResourceActionPair>();
        XPathExpression<Element> xp = XPathFactory.instance().compile("//*/*[local-name()='flow']",
                                                                      Filters.element(APIKitTools.API_KIT_NAMESPACE.getNamespace()));
        List<Element> elements = xp.evaluate(document);
        for (Element element : elements) {
            String resource = element.getAttribute("resource").getValue();
            String action = element.getAttribute("action").getValue();
            String ref = element.getAttributeValue("ref");
            API api = null;

            if (ref != null) {
                // TODO Add here the search for the config element
            } else if (includedApis.size() > 0) {
                api = includedApis.toArray(new API[0])[0];
            }

            if (api != null) {
                if (!resource.startsWith("/")) {
                    resource = "/" + resource;
                }

                String baseUri = api.getBaseUri();
                List<String> split = new ArrayList<String>(Arrays.asList(baseUri.split("/")));

                Collections.reverse(split);

                String path = null;
                for (String s : split) {
                    if (!"".equals(s)) {
                        path = s;
                        break;
                    }
                }

                if (path == null) {
                    throw new IllegalStateException("Inbound-endpoint Address URI is invalid");
                }

                if (!path.startsWith("/")) {
                    path = "/" + path;
                }

                entries.add(new ResourceActionPair(api, path + resource, action));
            } else {
                throw new IllegalStateException("No APIKit entries found in Mule config");
            }
        }
        return entries;
    }
}
