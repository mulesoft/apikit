/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.jdom2.Element;
import org.mule.tools.apikit.output.NamespaceWithLocation;

import java.util.Arrays;
import java.util.List;

import static org.mule.tools.apikit.misc.APIKitTools.API_KIT_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.EE_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTP_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.XSI_NAMESPACE;

public class MuleScope implements Scope {

  private final Element mule;

  private void addLocationEntry(StringBuilder stringBuilder, NamespaceWithLocation namespaceWithLocation) {
    stringBuilder
        .append(namespaceWithLocation.getNamespace().getURI())
        .append(" ")
        .append(namespaceWithLocation.getLocation())
        .append(" ");
  }

  public MuleScope(boolean addEENamespace) {
    mule = new Element("mule");
    StringBuilder stringBuilder = new StringBuilder();

    mule.setNamespace(XMLNS_NAMESPACE.getNamespace());
    mule.addNamespaceDeclaration(XMLNS_NAMESPACE.getNamespace());
    addLocationEntry(stringBuilder, XMLNS_NAMESPACE);

    List<NamespaceWithLocation> namespaces = Arrays.asList(HTTP_NAMESPACE, API_KIT_NAMESPACE);

    mule.addNamespaceDeclaration(XSI_NAMESPACE.getNamespace());

    for (NamespaceWithLocation namespace : namespaces) {
      mule.addNamespaceDeclaration(namespace.getNamespace());
      addLocationEntry(stringBuilder, namespace);
    }
    if (addEENamespace) {
      mule.addNamespaceDeclaration(EE_NAMESPACE.getNamespace());
      addLocationEntry(stringBuilder, EE_NAMESPACE);
    }
    mule.setAttribute("schemaLocation", stringBuilder.toString(),
                      XSI_NAMESPACE.getNamespace());

  }

  @Override
  public Element generate() {
    return mule;
  }
}
