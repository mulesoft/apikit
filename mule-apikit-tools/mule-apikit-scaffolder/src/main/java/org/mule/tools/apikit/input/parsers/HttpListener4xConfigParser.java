/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input.parsers;

import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTP_NAMESPACE;

import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.HttpListener4xConfig;
import org.mule.tools.apikit.model.HttpListenerConnection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class HttpListener4xConfigParser implements MuleConfigFileParser {

  public static final String ELEMENT_NAME = "listener-config";

  public Map<String, HttpListener4xConfig> parse(Document document) {
    Map<String, HttpListener4xConfig> httpListenerConfigMap = new HashMap<String, HttpListener4xConfig>();
    XPathExpression<Element> xp = XPathFactory.instance().compile("//*/*[local-name()='" + ELEMENT_NAME + "']",
                                                                  Filters.element(HTTP_NAMESPACE.getNamespace()));
    List<Element> elements = xp.evaluate(document);
    for (Element element : elements) {
      String name = element.getAttributeValue("name");
      if (name == null) {
        throw new IllegalStateException("Cannot retrieve name.");
      }
      String basePath = element.getAttributeValue("basePath");
      if (basePath == null) {
        basePath = "/";
      } else if (!basePath.startsWith("/")) {
        basePath = "/" + basePath;
      }
      for (Element child : element.getChildren()) {
        if (child.getName().equals("listener-connection")) {
          String host = child.getAttributeValue("host");
          if (host == null) {
            throw new IllegalStateException("Cannot retrieve host.");
          }
          String port = child.getAttributeValue("port");
          if (port == null) {
            port = Integer.toString(API.DEFAULT_PORT);
          }
          String protocol = child.getAttributeValue("protocol");
          if (protocol == null) {
            protocol = API.DEFAULT_PROTOCOL;
          }
          httpListenerConfigMap.put(name,
                                    new HttpListener4xConfig(name, basePath, new HttpListenerConnection(host, port, protocol)));
        }
      }
    }
    return httpListenerConfigMap;
  }

}
