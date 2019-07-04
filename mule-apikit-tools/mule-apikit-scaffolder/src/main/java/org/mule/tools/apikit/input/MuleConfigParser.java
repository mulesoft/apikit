/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.mule.tools.apikit.model.ResourceActionMimeTypeTriplet;

public class MuleConfigParser {

  private Set<ResourceActionMimeTypeTriplet> entries = new HashSet<>();
  private Map<String, API> includedApis = new HashMap<>();
  private Map<String, APIKitConfig> apikitConfigs = new HashMap<>();
  private final APIFactory apiFactory;
  private final Log log;

  public MuleConfigParser(Log log, APIFactory apiFactory) {
    this.apiFactory = apiFactory;
    this.log = log;
  }

  public MuleConfigParser parse(List<String> apiFilePaths, Map<File, InputStream> streams) {
    Map<File, Document> configurations = createDocuments(streams);

    for (Entry<File, Document> fileStreamEntry : configurations.entrySet()) {
      Document document = fileStreamEntry.getValue();
      File file = fileStreamEntry.getKey();
      parseConfigs(file, document);
    }

    for (Entry<File, Document> fileStreamEntry : configurations.entrySet()) {
      Document document = fileStreamEntry.getValue();
      File file = fileStreamEntry.getKey();
      parseApis(file, document, apiFilePaths);
    }

    parseFlows(configurations.values());
    return this;
  }

  private Map<File, Document> createDocuments(Map<File, InputStream> streams) {
    Map<File, Document> result = new HashMap<>();

    SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
    for (Entry<File, InputStream> fileStreamEntry : streams.entrySet()) {
      try (InputStream stream = fileStreamEntry.getValue()) {
        Document document = saxBuilder.build(stream);
        result.put(fileStreamEntry.getKey(), document);
      } catch (Exception e) {
        log.error("Error parsing Mule xml config file: [" + fileStreamEntry.getKey() + "]. Reason: " + e.getMessage());
        log.debug(e);
      }
    }
    return result;
  }

  protected void parseConfigs(File file, Document document) {
    apikitConfigs.putAll(new APIKitConfigParser().parse(document));
    apiFactory.getHttpListenerConfigs().putAll(new HttpListener4xConfigParser().parse(document));
  }

  protected void parseApis(File file, Document document, List<String> apiFilePaths) {
    includedApis
        .putAll(new APIKitRoutersParser(apikitConfigs, apiFactory.getHttpListenerConfigs(), apiFilePaths, file, apiFactory)
            .parse(document));
  }

  protected void parseFlows(Collection<Document> documents) {
    for (Document document : documents) {
      try {
        entries.addAll(new APIKitFlowsParser(log, includedApis).parse(document));
      } catch (Exception e) {
        log.error("Error parsing Mule xml config file. Reason: " + e.getMessage());
        log.debug(e);
      }
    }
  }

  public Map<String, APIKitConfig> getApikitConfigs() {
    return apikitConfigs;
  }

  public Set<ResourceActionMimeTypeTriplet> getEntries() {
    return entries;
  }

  public Set<API> getIncludedApis() {
    return new HashSet<>(includedApis.values());
  }
}
