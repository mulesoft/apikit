/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.jdom2.CDATA;
import org.jdom2.Element;
import org.mule.tools.apikit.misc.ExampleUtils;
import org.mule.tools.apikit.output.GenerationModel;

import java.util.ArrayList;
import java.util.List;

import static org.mule.tools.apikit.output.MuleConfigGenerator.EE_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XSI_NAMESPACE;

public class APIKitFlowScope implements Scope {

  private final Element flow;

  private static final String LOGGER_ATTRIBUTE_LEVEL = "level";
  private static final String LOGGER_ATTRIBUTE_MESSAGE = "message";
  private static final String LOGGER_ATTRIBUTE_LEVEL_VALUE = "INFO";

  public APIKitFlowScope(GenerationModel flowEntry) {
    this(flowEntry, true);
  }

  public APIKitFlowScope(GenerationModel flowEntry, boolean isMuleEE) {
    flow = new Element("flow", XMLNS_NAMESPACE.getNamespace());
    flow.setAttribute("name", flowEntry.getFlowName());

    for (Element element : generateFlowContent(flowEntry, isMuleEE)) {
      flow.addContent(element);
    }
  }

  private List<Element> generateFlowContent(GenerationModel flowEntry, boolean isMuleEE) {
    if (isMuleEE && flowEntry.getExampleWrapper() != null) {
      try {
        return generateTransform(flowEntry);
      } catch (Exception e) {
        return generateLogger(flowEntry, true);
      }
    } else {
      return generateLogger(flowEntry, isMuleEE);
    }
  }

  private List<Element> generateTransform(GenerationModel flowEntry) {
    ArrayList<Element> result = new ArrayList<>();
    if (!flowEntry.getUriParameters().isEmpty())
      result.add(createEEUriParamsSetVariables(flowEntry));

    Element transform = new Element("transform", EE_NAMESPACE.getNamespace());
    Element setPayload = new Element("set-payload", EE_NAMESPACE.getNamespace());
    Element message = new Element("message", EE_NAMESPACE.getNamespace());
    CDATA cdataSection = new CDATA(generateTransformTextForExample(flowEntry.getExampleWrapper().trim()));
    setPayload.addContent(cdataSection);
    message.setContent(setPayload);
    transform.addNamespaceDeclaration(EE_NAMESPACE.getNamespace());
    transform.setAttribute("schemaLocation", EE_NAMESPACE.getNamespace().getURI() + " " + EE_NAMESPACE.getLocation(),
                           XSI_NAMESPACE.getNamespace());
    transform.addContent(message);
    result.add(transform);
    return result;
  }

  private List<Element> generateLogger(GenerationModel flowEntry, boolean isMuleEE) {
    ArrayList<Element> result = new ArrayList<>();

    if (isMuleEE && !flowEntry.getUriParameters().isEmpty())
      result.add(createEEUriParamsSetVariables(flowEntry));
    else if (!isMuleEE && !flowEntry.getUriParameters().isEmpty())
      result.addAll(createCEUriParamsSetVariables(flowEntry));

    Element logger = new Element("logger", XMLNS_NAMESPACE.getNamespace());
    logger.setAttribute(LOGGER_ATTRIBUTE_LEVEL, LOGGER_ATTRIBUTE_LEVEL_VALUE);
    logger.setAttribute(LOGGER_ATTRIBUTE_MESSAGE, flowEntry.getFlowName());

    result.add(logger);
    return result;
  }

  private Element createEEUriParamsSetVariables(GenerationModel flowEntry) {
    Element transform = new Element("transform", EE_NAMESPACE.getNamespace());
    //    transform.setAttribute("name", "Set Variables"/);
    Element variables = new Element("variables", EE_NAMESPACE.getNamespace());

    for (String uriParameter : flowEntry.getUriParameters()) {
      Element setVariable = new Element("set-variable", EE_NAMESPACE.getNamespace());
      setVariable.setAttribute("variableName", uriParameter);
      setVariable.addContent("attributes.uriParams." + uriParameter);

      variables.addContent(setVariable);
    }

    transform.addContent(variables);

    return transform;
  }

  private List<Element> createCEUriParamsSetVariables(GenerationModel flowEntry) {
    List<Element> result = new ArrayList<>();
    for (String uriParameter : flowEntry.getUriParameters()) {
      Element element = new Element("set-variable");
      element.setAttribute("value", "#[attributes.uriParams." + uriParameter + "]");
      element.setAttribute("variableName", uriParameter);
      result.add(element);
    }
    return result;
  }

  private String generateTransformTextForExample(String example) {
    return ExampleUtils.getDataWeaveExpressionText(example);
  }

  @Override
  public Element generate() {
    return flow;
  }
}
