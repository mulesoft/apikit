/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import org.mule.tools.apikit.input.APIKitFlow;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.APIKitConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class APIKitConfigParser implements MuleConfigFileParser {

  @Override
  public Map<String, APIKitConfig> parse(Document document) {
    Map<String, APIKitConfig> apikitConfigs = new HashMap<String, APIKitConfig>();
    XPathExpression<Element> xp = XPathFactory.instance().compile("//*/*[local-name()='" + APIKitConfig.ELEMENT_NAME + "']",
                                                                  Filters.element(APIKitTools.API_KIT_NAMESPACE.getNamespace()));
    List<Element> elements = xp.evaluate(document);
    for (Element element : elements) {
      Attribute name = element.getAttribute(APIKitConfig.NAME_ATTRIBUTE);
      Attribute api = element.getAttribute(APIKitConfig.API_ATTRIBUTE);
      Attribute raml = element.getAttribute(APIKitConfig.RAML_ATTRIBUTE);
      Attribute extensionEnabled = element.getAttribute(APIKitConfig.EXTENSION_ENABLED_ATTRIBUTE);
      Attribute outboundHeadersMapName = element.getAttribute(APIKitConfig.OUTBOUND_HEADERS_MAP_ATTRIBUTE);
      Attribute httpStatusVarName = element.getAttribute(APIKitConfig.HTTP_STATUS_VAR_ATTRIBUTE);

      final APIKitConfig apiKitConfig = new APIKitConfig();
      if (api != null) {
        apiKitConfig.setApi(api.getValue());
      } else if (raml != null) {
        apiKitConfig.setRaml(raml.getValue());
      } else {
        throw new IllegalArgumentException(APIKitConfig.API_ATTRIBUTE + " attribute is required");
      }
      if (name != null) {
        apiKitConfig.setName(name.getValue());
      }
      if (outboundHeadersMapName != null) {
        apiKitConfig.setOutboundHeadersMapName(outboundHeadersMapName.getValue());
      }
      if (extensionEnabled != null) {
        apiKitConfig.setExtensionEnabled(Boolean.valueOf(extensionEnabled.getValue()));
      }
      if (httpStatusVarName != null) {
        apiKitConfig.setHttpStatusVarName(httpStatusVarName.getValue());
      }

      String configId = apiKitConfig.getName() != null ? apiKitConfig.getName() : APIKitFlow.UNNAMED_CONFIG_NAME;
      apikitConfigs.put(configId, apiKitConfig);
    }

    return apikitConfigs;
  }
}
