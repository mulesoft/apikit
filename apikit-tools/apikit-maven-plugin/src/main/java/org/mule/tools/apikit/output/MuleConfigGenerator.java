/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import org.mule.tools.apikit.output.deployer.MuleDeployWriter;
import org.mule.tools.apikit.output.scopes.*;
import org.mule.tools.apikit.output.scopes.FlowScope;
import org.mule.tools.apikit.model.API;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MuleConfigGenerator {
    public static final NamespaceWithLocation XMLNS_NAMESPACE = new NamespaceWithLocation(
            Namespace.getNamespace("http://www.mulesoft.org/schema/mule/core"),
            "http://www.mulesoft.org/schema/mule/core/current/mule.xsd"
    );
    public static final NamespaceWithLocation XSI_NAMESPACE = new NamespaceWithLocation(
            Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"),
            null
    );
    public static final NamespaceWithLocation HTTP_NAMESPACE = new NamespaceWithLocation(
            Namespace.getNamespace("http", "http://www.mulesoft.org/schema/mule/http"),
            "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd"
    );
    public static final NamespaceWithLocation SPRING_NAMESPACE = new NamespaceWithLocation(
            Namespace.getNamespace("spring", "http://www.springframework.org/schema/beans"),
            "http://www.springframework.org/schema/beans/spring-beans-3.1.xsd"
    );

    private static final String INDENTATION = "    ";

    private final List<GenerationModel> flowEntries;
    private final Log log;
    private final File rootDirectory;

    public MuleConfigGenerator(Log log, File muleConfigOutputDirectory, List<GenerationModel> flowEntries) {
        this.log = log;
        this.flowEntries = flowEntries;
        this.rootDirectory = muleConfigOutputDirectory;
    }

    public void generate() {
        Map<API, Document> docs = new HashMap<API, Document>();

        for (GenerationModel flowEntry : flowEntries) {
            Document doc;

            API api = flowEntry.getApi();
            try {
                doc = getOrCreateDocument(docs, api);
            } catch (Exception e) {
                log.error("Error generating xml for file: [" + api.getYamlFile() + "]", e);
                continue;
            }

            // Generate each of the APIKit flows
            doc.getRootElement().addContent(new APIKitFlowScope(flowEntry).generate());
        }

        // Write everything to files
        for (Map.Entry<API, Document> yamlFileDescriptorDocumentEntry : docs.entrySet()) {
            Format prettyFormat = Format.getPrettyFormat();
            prettyFormat.setIndent(INDENTATION);
            prettyFormat.setLineSeparator(System.getProperty("line.separator"));
            prettyFormat.setEncoding("UTF-8");
            XMLOutputter xout = new XMLOutputter(prettyFormat);
            Document doc = yamlFileDescriptorDocumentEntry.getValue();
            File xmlFile = yamlFileDescriptorDocumentEntry.getKey().getXmlFile(rootDirectory);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(xmlFile);
                xout.output(doc, fileOutputStream);
                fileOutputStream.close();
                log.info("Updating file: [" + xmlFile + "]");
            } catch (IOException e) {
                log.error("Error writing to file: [" + xmlFile + "]", e);
            }
        }

        // Generate mule deploy properties file
        new MuleDeployWriter(rootDirectory).generate();
    }

    Document getOrCreateDocument(Map<API, Document> docs, API api)
            throws IOException, JDOMException {
        Document doc;
        if (docs.containsKey(api)) {
            doc = docs.get(api);
        } else {
            doc = getDocument(api);
            if(api.getConfig() == null) {
                api.setDefaultConfig();
                generateAPIKitConfig(api, doc);
            }
            docs.put(api, doc);
        }
        return doc;
    }

    private Document getDocument(API api) throws IOException, JDOMException {
        SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
        Document doc;
        File xmlFile = api.getXmlFile(rootDirectory);
        if (!xmlFile.exists() || xmlFile.length() == 0) {
            xmlFile.getParentFile().mkdirs();
            doc = new Document();
            doc.setRootElement(new MuleScope().generate());
        } else {
            InputStream xmlInputStream = new FileInputStream(xmlFile);
            doc = saxBuilder.build(xmlInputStream);
        }
        return doc;
    }

    private void generateAPIKitConfig(API api, Document doc) {
        XPathExpression muleExp = XPathFactory.instance().compile("//*[local-name()='mule']");
        List<Element> mules = muleExp.evaluate(doc);
        Element mule = mules.get(0);
        new APIKitConfigScope(api.getConfig(), mule).generate();
        Element exceptionStrategy = new ExceptionStrategyScope(mule, api.getId()).generate();
        String configRef = api.getConfig() != null? api.getConfig().getName() : null;
        new FlowScope(mule, exceptionStrategy.getAttribute("name").getValue(),
                      api, configRef).generate();
    }

}
