/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

import org.mule.tools.apikit.input.APIKitFlow;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.ResourceActionPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class APIKitFlowsParser implements MuleConfigFileParser {

    private static final Logger LOGGER = Logger.getLogger(APIKitFlowsParser.class);
    private final Map<String, API> includedApis;

    public APIKitFlowsParser(final Map<String, API> includedApis) {
        this.includedApis = includedApis;
    }

    @Override
    public Set<ResourceActionPair> parse(Document document)  {
        Set<ResourceActionPair> entries = new HashSet<ResourceActionPair>();
        XPathExpression<Element> xp = XPathFactory.instance().compile("//*/*[local-name()='flow']",
                                                                      Filters.element(XMLNS_NAMESPACE.getNamespace()));
        List<Element> elements = xp.evaluate(document);
        for (Element element : elements) {
            String name = element.getAttributeValue("name");
            APIKitFlow flow;
            try {
                flow = APIKitFlow.buildFromName(name);
            } catch(IllegalArgumentException iae) {
                LOGGER.info("Flow named '" + name + "' is not an APIKit Flow because it does not follow APIKit naming convention." );
                continue;
            }

            API api = includedApis.get(flow.getConfigRef());

            String resource = flow.getResource();
            if (api != null) {
                if (!resource.startsWith("/")) {
                    resource = "/" + resource;
                }
                String path = APIKitTools.getPathFromUri(api.getBaseUri());
                if (path == null) {
                    throw new IllegalStateException("Inbound-endpoint Address URI is invalid");
                }

                entries.add(new ResourceActionPair(api, path + resource, flow.getAction()));
            } else {
                throw new IllegalStateException("No APIKit entries found in Mule config");
            }
        }
        return entries;
    }
}
