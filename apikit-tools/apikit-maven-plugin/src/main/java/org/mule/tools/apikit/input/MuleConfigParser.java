package org.mule.tools.apikit.input;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.*;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.ResourceActionPair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.util.Map.Entry;

public class MuleConfigParser {

    private final Set<ResourceActionPair> entries = new HashSet<ResourceActionPair>();
    private final Set<API> includedApis = new HashSet<API>();
    private final Set<File> yamlPaths;

    public MuleConfigParser(Log log, Set<File> yamlPaths, Map<File, InputStream> streams) {

        this.yamlPaths = yamlPaths;

        for (Entry<File, InputStream> fileStreamEntry : streams.entrySet()) {
            InputStream stream = fileStreamEntry.getValue();
            File file = fileStreamEntry.getKey();
            try {
                parseMuleConfigFile(file, stream);
                stream.close();
            } catch (Exception e) {
                log.error("Error parsing Mule xml config file: [" + file + "]");
                log.debug(e);
            }
        }
    }

    private void parseMuleConfigFile(File file, InputStream stream) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
        Document document = saxBuilder.build(stream);

        final Filter<Element> apikitElementFilter = Filters.element(
                APIKitTools.API_KIT_NAMESPACE.getNamespace());

        XPathFactory xPathFactory = XPathFactory.instance();
        XPathExpression<Element> xp = xPathFactory.compile("//*/*[local-name()='rest-processor']",
                apikitElementFilter);
        List<Element> elements = xp.evaluate(document);
        for (Element element : elements) {
            Attribute config = element.getAttribute("config");
            if (config != null) {
                for (File yamlPath : yamlPaths) {
                    if (yamlPath.getName().equals(config.getValue())) {
                        Element inboundEndpoint = element.getParentElement().getChildren().get(0);

                        // TODO Unhack, it is assuming that the rest-processor will always be in a flow
                        // where the first element is going to be an http inbound-endpoint
                        if (!"inbound-endpoint".equals(inboundEndpoint.getName())) {
                            throw new IllegalStateException("The first element of the main flow must be an " +
                                    "inbound-endpoint");
                        }

                        String path = inboundEndpoint.getAttributeValue("path");

                        // Case the user is specifying baseURI using address attribute
                        if (path == null) {
                            String address = inboundEndpoint.getAttributeValue("address");

                            if (address == null) {
                                throw new IllegalStateException("Neither 'path' nor 'address' attribute was used. " +
                                        "Cannot retrieve base URI.");
                            }

                            path = address;
                        } else  if (!path.startsWith("/")) {
                            path = "/" + path;
                        }

                        includedApis.add(
                                API.createAPIBinding(yamlPath, file, path));
                    }
                }

            }
        }

        xp = xPathFactory.compile("//*/*[local-name()='flow']",
                apikitElementFilter);
        elements = xp.evaluate(document);
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

    }

    public Set<ResourceActionPair> getEntries() {
        return entries;
    }

    public Set<API> getIncludedApis() {
        return includedApis;
    }
}
