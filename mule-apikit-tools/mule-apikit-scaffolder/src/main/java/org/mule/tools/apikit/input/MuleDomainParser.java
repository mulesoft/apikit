/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.mule.tools.apikit.input.parsers.HttpListener4xConfigParser;
import org.mule.tools.apikit.model.HttpListener4xConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

public class MuleDomainParser {

  private Map<String, HttpListener4xConfig> httpListenerConfigs = new HashMap<>();

  public MuleDomainParser(Log log, InputStream domainStream) {

    if (domainStream != null) {
      try {
        parseMuleDomainFile(domainStream);
      } catch (Exception e) {
        log.error("Error parsing Mule domain file. Reason: " + e.getMessage());
        log.debug(e);
      }
    }
  }

  private void parseMuleDomainFile(InputStream stream) throws JDOMException, IOException {
    SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
    Document document = saxBuilder.build(stream);
    httpListenerConfigs.putAll(new HttpListener4xConfigParser().parse(document));
  }

  public Map<String, HttpListener4xConfig> getHttpListenerConfigs() {
    return httpListenerConfigs;
  }

}
