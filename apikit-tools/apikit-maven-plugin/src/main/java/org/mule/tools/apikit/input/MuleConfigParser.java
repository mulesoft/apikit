package org.mule.tools.apikit.input;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

import org.mule.tools.apikit.input.parsers.APIKitConfigParser;
import org.mule.tools.apikit.input.parsers.APIKitFlowsParser;
import org.mule.tools.apikit.input.parsers.APIKitRoutersParser;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.model.ResourceActionPair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.util.Map.Entry;

public class MuleConfigParser {

    private Set<ResourceActionPair> entries = new HashSet<ResourceActionPair>();
    private Map<String, API> includedApis = new HashMap<String, API>();
    private Map<String, APIKitConfig> apikitConfigs = new HashMap<String, APIKitConfig>();

    public MuleConfigParser(Log log, Set<File> yamlPaths, Map<File, InputStream> streams) {
        for (Entry<File, InputStream> fileStreamEntry : streams.entrySet()) {
            InputStream stream = fileStreamEntry.getValue();
            File file = fileStreamEntry.getKey();
            try {
                parseMuleConfigFile(file, stream, yamlPaths);
                stream.close();
            } catch (Exception e) {
                log.error("Error parsing Mule xml config file: [" + file + "]");
                log.debug(e);
            }
        }
    }

    private void parseMuleConfigFile(File file, InputStream stream, Set<File> yamlPaths) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
        Document document = saxBuilder.build(stream);

        apikitConfigs = new APIKitConfigParser().parse(document);
        includedApis = new APIKitRoutersParser(apikitConfigs, yamlPaths, file).parse(document);
        entries = new APIKitFlowsParser(includedApis).parse(document);
    }

    public Map<String, APIKitConfig> getApikitConfigs() {
        return apikitConfigs;
    }

    public Set<ResourceActionPair> getEntries() {
        return entries;
    }

    public Set<API> getIncludedApis() {
        Set<API> apis = new HashSet<API>();
        apis.addAll(includedApis.values());
        return apis;
    }
}
