/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.model.HttpListener4xConfig;
import org.mule.tools.apikit.model.RuntimeEdition;
import org.mule.tools.apikit.output.scopes.APIKitConfigScope;
import org.mule.tools.apikit.output.scopes.APIKitFlowScope;
import org.mule.tools.apikit.output.scopes.ConsoleFlowScope;
import org.mule.tools.apikit.output.scopes.FlowScope;
import org.mule.tools.apikit.output.scopes.HttpListenerConfigMule4Scope;
import org.mule.tools.apikit.output.scopes.MuleScope;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.plugin.logging.Log;
import org.jdom2.Content;
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

import static org.mule.tools.apikit.model.RuntimeEdition.EE;

public class MuleConfigGenerator {

  public static final NamespaceWithLocation XMLNS_NAMESPACE = new NamespaceWithLocation(
                                                                                        Namespace
                                                                                            .getNamespace("http://www.mulesoft.org/schema/mule/core"),
                                                                                        "http://www.mulesoft.org/schema/mule/core/current/mule.xsd");
  public static final NamespaceWithLocation XSI_NAMESPACE = new NamespaceWithLocation(
                                                                                      Namespace
                                                                                          .getNamespace("xsi",
                                                                                                        "http://www.w3.org/2001/XMLSchema-instance"),
                                                                                      null);
  public static final NamespaceWithLocation HTTP_NAMESPACE = new NamespaceWithLocation(
                                                                                       Namespace
                                                                                           .getNamespace("http",
                                                                                                         "http://www.mulesoft.org/schema/mule/http"),
                                                                                       "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");

  public static final NamespaceWithLocation EE_NAMESPACE = new NamespaceWithLocation(
                                                                                     Namespace
                                                                                         .getNamespace("ee",
                                                                                                       "http://www.mulesoft.org/schema/mule/ee/core"),
                                                                                     "http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd");

  private static final String INDENTATION = "    ";

  private final List<GenerationModel> flowEntries;
  private final Log log;
  private final File rootDirectory;
  private final Map<String, HttpListener4xConfig> domainHttpListenerConfigs;
  private final Set<File> ramlsWithExtensionEnabled;
  private final RuntimeEdition runtimeEdition;

  public MuleConfigGenerator(Log log, File muleConfigOutputDirectory, List<GenerationModel> flowEntries,
                             Map<String, HttpListener4xConfig> domainHttpListenerConfigs, Set<File> ramlsWithExtensionEnabled,
                             String minMuleVersion, RuntimeEdition runtimeEdition) {
    this.log = log;
    this.flowEntries = flowEntries;
    this.rootDirectory = muleConfigOutputDirectory;
    this.domainHttpListenerConfigs = domainHttpListenerConfigs;
    this.runtimeEdition = runtimeEdition;
    if (ramlsWithExtensionEnabled == null) {
      this.ramlsWithExtensionEnabled = new TreeSet<>();
    } else {
      this.ramlsWithExtensionEnabled = ramlsWithExtensionEnabled;
    }
  }

  public void generate() {
    Map<API, Document> docs = new HashMap<API, Document>();

    for (GenerationModel flowEntry : flowEntries) {
      Document doc;

      API api = flowEntry.getApi();
      try {
        doc = getOrCreateDocument(docs, api);
      } catch (Exception e) {
        log.error("Error generating xml for file: [" + api.getRamlFile() + "]", e);
        continue;
      }

      // Generate each of the APIKit flows and insert them after the last flow
      int index = getLastFlowIndex(doc) + 1;
      doc.getRootElement().addContent(index, new APIKitFlowScope(flowEntry, isMuleEE()).generate());
    }

    // Write everything to files
    for (Map.Entry<API, Document> ramlFileDescriptorDocumentEntry : docs.entrySet()) {
      Format prettyFormat = Format.getPrettyFormat();
      prettyFormat.setIndent(INDENTATION);
      prettyFormat.setLineSeparator(System.getProperty("line.separator"));
      prettyFormat.setEncoding("UTF-8");
      XMLOutputter xout = new XMLOutputter(prettyFormat);
      Document doc = ramlFileDescriptorDocumentEntry.getValue();
      File xmlFile = ramlFileDescriptorDocumentEntry.getKey().getXmlFile(rootDirectory);
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

  private int getLastFlowIndex(Document doc) {
    int lastFlowIndex = 0;
    for (int i = 0; i < doc.getRootElement().getContentSize(); i++) {
      Content content = doc.getRootElement().getContent(i);
      if (content instanceof Element && "flow".equals(((Element) content).getName())) {
        lastFlowIndex = i;
      }
    }
    return lastFlowIndex;
  }

  Document getOrCreateDocument(Map<API, Document> docs, API api)
      throws IOException, JDOMException {
    Document doc;
    if (docs.containsKey(api)) {
      doc = docs.get(api);
    } else {
      doc = getDocument(api);
      if (api.getConfig() == null || api.getHttpListenerConfig() == null) {
        if (api.getConfig() == null) {
          api.setDefaultAPIKitConfig();
        }
        if (ramlsWithExtensionEnabled.contains(api.getRamlFile())) {
          api.getConfig().setExtensionEnabled(true);
        }
        generateAPIKitAndListenerConfig(api, doc);
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
      doc.setRootElement(new MuleScope(false).generate());
    } else {
      InputStream xmlInputStream = new FileInputStream(xmlFile);
      doc = saxBuilder.build(xmlInputStream);
    }
    return doc;
  }

  private void generateAPIKitAndListenerConfig(API api, Document doc) {
    XPathExpression muleExp = XPathFactory.instance().compile("//*[local-name()='mule']");
    List<Element> mules = muleExp.evaluate(doc);
    Element mule = mules.get(0);
    String listenerConfigRef = null;
    if (!domainHttpListenerConfigs.containsKey(api.getHttpListenerConfig().getName())) {
      new HttpListenerConfigMule4Scope(api, mule).generate();
    }
    listenerConfigRef = api.getHttpListenerConfig().getName();
    api.setPath(APIKitTools.addAsteriskToPath(api.getPath()));
    //TODO GLOBAL EXCEPTION STRATEGY REFERENCE
    //        if (!api.useListenerMule3() && !api.useInboundEndpoint())
    //        {
    //            addGlobalExceptionStrategy(mule, api.getId());
    //        }
    new APIKitConfigScope(api.getConfig(), mule).generate();
    //Element exceptionStrategy = new ExceptionStrategyScope(api.getId()).generate();
    String configRef = api.getConfig() != null ? api.getConfig().getName() : null;

    String exceptionStrategyRef = null;//exceptionStrategy.getAttribute("name").getValue()
    new FlowScope(mule, exceptionStrategyRef,
                  api, configRef, listenerConfigRef, isMuleEE()).generate();

    new ConsoleFlowScope(mule, api, configRef, listenerConfigRef, isMuleEE()).generate();

    //mule.addContent(exceptionStrategy);
  }

  private void addGlobalExceptionStrategy(Element mule, String apiId) {
    Element globalExceptionStrategy = new Element("error-handler",
                                                  XMLNS_NAMESPACE.getNamespace());
    globalExceptionStrategy.setAttribute("name", apiId + "-" + "apiKitGlobalExceptionMapping");
    mule.addContent(globalExceptionStrategy);

  }

  private boolean isMuleEE() {
    return runtimeEdition == EE;
  }

}
