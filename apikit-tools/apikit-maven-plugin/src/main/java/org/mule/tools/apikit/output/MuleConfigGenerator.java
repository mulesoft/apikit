package org.mule.tools.apikit.output;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import org.mule.tools.apikit.model.APIKitConfig;
import org.mule.tools.apikit.output.scopes.APIKitConfigScope;
import org.mule.tools.apikit.output.scopes.APIKitFlowScope;
import org.mule.tools.apikit.output.scopes.ExceptionStrategyScope;
import org.mule.tools.apikit.output.scopes.FlowScope;
import org.mule.tools.apikit.output.scopes.MuleScope;
import org.mule.tools.apikit.model.API;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    private final Set<GenerationModel> flowEntries;
    private final Log log;
    private final File rootDirectory;

    public MuleConfigGenerator(Log log, File muleConfigOutputDirectory, Set<GenerationModel> flowEntries) {
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
            new APIKitFlowScope(flowEntry, doc.getRootElement()).generate();
        }

        // Write everything to files
        for (Map.Entry<API, Document> yamlFileDescriptorDocumentEntry : docs.entrySet()) {
            XMLOutputter xout = new XMLOutputter();
            xout.getFormat().setLineSeparator(LineSeparator.UNIX);
            xout.getFormat().setEncoding("UTF-8");
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

    }

    Document getOrCreateDocument(Map<API, Document> docs, API api)
            throws IOException, JDOMException {
        Document doc;
        SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
        if (docs.containsKey(api)) {
            doc = docs.get(api);
        } else {
            File xmlFile = api.getXmlFile(rootDirectory);
            if (!xmlFile.exists() || xmlFile.length() == 0) {
                xmlFile.getParentFile().mkdirs();
                doc = new Document();
                Element mule = new MuleScope(doc).generate();
                new APIKitConfigScope(api.getConfig(), mule).generate();
                Element exceptionStrategy = new ExceptionStrategyScope(mule).generate();
                String configRef = api.getConfig() != null? api.getConfig().getName() : null;
                new FlowScope(mule, exceptionStrategy.getAttribute("name").getValue(),
                        api, configRef).generate();
            } else {
                InputStream xmlInputStream = new FileInputStream(xmlFile);
                doc = saxBuilder.build(xmlInputStream);
            }

            docs.put(api, doc);
        }
        return doc;
    }

}
