package org.mule.module.metadata;

import org.mule.module.metadata.interfaces.Parseable;
import org.mule.raml.implv1.parser.visitor.RamlDocumentBuilderImpl;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.visitor.IRamlDocumentBuilder;

import java.io.File;

public class RamlV1Parser implements Parseable
{
    @Override
    public IRaml build(File ramlFile, String ramlContent)
    {
        IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl();
        ramlDocumentBuilder.addPathLookupFirst(ramlFile.getParentFile().getPath());
        return ramlDocumentBuilder.build(ramlContent, ramlFile.getName());
    }
}
