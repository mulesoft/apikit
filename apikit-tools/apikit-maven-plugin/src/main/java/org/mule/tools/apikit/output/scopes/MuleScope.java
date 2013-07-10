package org.mule.tools.apikit.output.scopes;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.output.NamespaceWithLocation;

import java.util.Arrays;
import java.util.List;

import static org.mule.tools.apikit.output.MuleConfigGenerator.XMLNS_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.HTTP_NAMESPACE;
import static org.mule.tools.apikit.output.MuleConfigGenerator.SPRING_NAMESPACE;
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

    public MuleScope(Document doc)  {
        mule = new Element("mule");
        StringBuilder stringBuilder = new StringBuilder();

        mule.setNamespace(XMLNS_NAMESPACE.getNamespace());
        mule.addNamespaceDeclaration(XMLNS_NAMESPACE.getNamespace());
        addLocationEntry(stringBuilder, XMLNS_NAMESPACE);

        List<NamespaceWithLocation> namespaces = Arrays.asList(HTTP_NAMESPACE,
                APIKitTools.API_KIT_NAMESPACE, SPRING_NAMESPACE);

        mule.addNamespaceDeclaration(XSI_NAMESPACE.getNamespace());

        for (NamespaceWithLocation namespace : namespaces) {
            mule.addNamespaceDeclaration(namespace.getNamespace());
            addLocationEntry(stringBuilder, namespace);
        }

        mule.setAttribute("schemaLocation", stringBuilder.toString(),
                XSI_NAMESPACE.getNamespace());

        doc.setRootElement(mule);
    }

    @Override
    public Element generate() {
        return mule;
    }
}
