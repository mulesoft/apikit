/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;

import org.mule.module.apikit.helpers.FlowName;
import org.mule.tools.apikit.input.APIKitFlow;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class APIKitFlowsParser implements MuleConfigFileParser {

  private final Log log;
  private final Map<String, API> includedApis;

  public APIKitFlowsParser(Log log, final Map<String, API> includedApis) {
    this.log = log;
    this.includedApis = includedApis;
  }

  @Override
  public Set<ResourceActionMimeTypeTriplet> parse(Document document) {
    Set<ResourceActionMimeTypeTriplet> entries = new HashSet<>();
    XPathExpression<Element> xp = XPathFactory.instance().compile("//*/*[local-name()='flow']",
                                                                  Filters.element(XMLNS_NAMESPACE.getNamespace()));
    List<Element> elements = xp.evaluate(document);
    for (Element element : elements) {
      String name = FlowName.decode(element.getAttributeValue("name"));
      APIKitFlow flow;
      try {
        flow = APIKitFlow.buildFromName(name, includedApis.keySet());
      } catch (IllegalArgumentException iae) {
        log.info("Flow named '" + name + "' is not an APIKit Flow because it does not follow APIKit naming convention.");
        continue;
      }

      API api = includedApis.get(flow.getConfigRef());

      String resource = flow.getResource();
      if (api != null) {
        if (!resource.startsWith("/")) {
          resource = "/" + resource;
        }

        if (api.getPath() == null) {
          throw new IllegalStateException("Api path is invalid");
        }

        String completePath = APIKitTools
            .getCompletePathFromBasePathAndPath(api.getHttpListenerConfig().getBasePath(), api.getPath());

        entries.add(new ResourceActionMimeTypeTriplet(api, completePath + resource, flow.getAction(), flow.getMimeType()));
      } else {
        throw new IllegalStateException("No APIKit entries found in Mule config");
      }
    }
    return entries;
  }
}
