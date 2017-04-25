/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.input;

import static java.util.Map.Entry;

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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

public class MuleConfigParser {

    private Set<ResourceActionMimeTypeTriplet> entries = new HashSet<>();
    private Map<String, API> includedApis = new HashMap<>();
    private Map<String, HttpListener4xConfig> httpListenerConfigs = new HashMap<>();
    private Map<String, APIKitConfig> apikitConfigs = new HashMap<>();
    private final APIFactory apiFactory;
    private final Log log;
    private final boolean compatibilityMode;

    public MuleConfigParser(Log log, APIFactory apiFactory, boolean compatibilityMode) {
        this.apiFactory = apiFactory;
        this.httpListenerConfigs.putAll(apiFactory.getDomainHttpListenerConfigs());
        this.log = log;
        this.compatibilityMode = compatibilityMode;
    }

    public MuleConfigParser parse(Set<File> ramlPaths, Map<File, InputStream> streams) {
        for (Entry<File, InputStream> fileStreamEntry : streams.entrySet()) {
            InputStream stream = fileStreamEntry.getValue();
            File file = fileStreamEntry.getKey();
            try {
                parseMuleConfigFile(file, stream, ramlPaths);
                stream.close();
            } catch (Exception e) {
                log.error("Error parsing Mule xml config file: [" + file + "]. Reason: " + e.getMessage());
                log.debug(e);
            }
        }
        return this;
    }

    protected void parseMuleConfigFile(File file, InputStream stream, Set<File> ramlPaths) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
        Document document = saxBuilder.build(stream);

        apikitConfigs.putAll(new APIKitConfigParser().parse(document));
        httpListenerConfigs.putAll(new HttpListener4xConfigParser().parse(document));

        includedApis.putAll(new APIKitRoutersParser(apikitConfigs,httpListenerConfigs, ramlPaths, file, apiFactory).parse(document));

        entries.addAll(new APIKitFlowsParser(log, includedApis, compatibilityMode).parse(document));
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
