/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.mule.tools.apikit.input.parsers.APIKitConfigParser;
import org.mule.tools.apikit.input.parsers.APIKitFlowsParser;
import org.mule.tools.apikit.input.parsers.APIKitRoutersParser;
import org.mule.tools.apikit.input.parsers.HttpListener4xConfigParser;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.HttpListener4xConfig;
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Map.Entry;

public class MuleConfigParser {

  private Set<ResourceActionMimeTypeTriplet> entries = new HashSet<>();
  private Map<String, API> includedApis = new HashMap<>();
  private Map<String, HttpListener4xConfig> httpListenerConfigs = new HashMap<>();
  private Map<String, APIKitConfig> apikitConfigs = new HashMap<>();
  private final APIFactory apiFactory;
  private final Log log;

  public MuleConfigParser(Log log, APIFactory apiFactory) {
    this.apiFactory = apiFactory;
    this.httpListenerConfigs.putAll(apiFactory.getDomainHttpListenerConfigs());
    this.log = log;
  }

  public MuleConfigParser parse(Set<File> ramlPaths, Map<File, InputStream> streams) {
    Map<File, Document> configurations = createDocuments(streams);

    for (Entry<File, Document> fileStreamEntry : configurations.entrySet()) {
      Document document = fileStreamEntry.getValue();
      File file = fileStreamEntry.getKey();
      parseConfigsAndApis(file, document, ramlPaths);
    }

    parseFlows(configurations.values());
    return this;
  }

  private Map<File, Document> createDocuments(Map<File, InputStream> streams) {
    Map<File, Document> result = new HashMap<>();

    SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
    for (Entry<File, InputStream> fileStreamEntry : streams.entrySet()) {
      InputStream stream = fileStreamEntry.getValue();
      try {
        Document document = saxBuilder.build(stream);
        stream.close();
        result.put(fileStreamEntry.getKey(), document);
      } catch (Exception e) {
        log.error("Error parsing Mule xml config file: [" + fileStreamEntry.getKey() + "]. Reason: " + e.getMessage());
        log.debug(e);
      }
    }
    return result;
  }

  protected void parseConfigsAndApis(File file, Document document, Set<File> ramlPaths) {
    apikitConfigs.putAll(new APIKitConfigParser().parse(document));
    httpListenerConfigs.putAll(new HttpListener4xConfigParser().parse(document));
    includedApis.putAll(new APIKitRoutersParser(apikitConfigs, httpListenerConfigs, ramlPaths, file, apiFactory).parse(document));
  }

  protected void parseFlows(Collection<Document> documents) {
    for (Document document : documents) {
      entries.addAll(new APIKitFlowsParser(log, includedApis).parse(document));
    }
  }

  public Map<String, APIKitConfig> getApikitConfigs() {
    return apikitConfigs;
  }

  public Set<ResourceActionMimeTypeTriplet> getEntries() {
    return entries;
  }

  public Set<API> getIncludedApis() {
    Set<API> apis = new HashSet<API>();
    apis.addAll(includedApis.values());
    return apis;
  }
}
