/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1;

import org.apache.commons.io.IOUtils;
import org.mule.raml.implv1.parser.visitor.RamlDocumentBuilderImpl;
import org.mule.raml.implv1.parser.visitor.RamlValidationServiceImpl;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.mule.raml.interfaces.parser.visitor.IRamlDocumentBuilder;
import org.mule.raml.interfaces.parser.visitor.IRamlValidationService;
import org.raml.parser.loader.ResourceLoader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParserV1Utils
{
    private static final Yaml YAML_PARSER = new Yaml();
    private static final String INCLUDE_KEYWORD = "!include";

    public static List<String> validate(String resourceFolder, String rootFileName, String resourceContent)
    {
        List<String> errorsList = new ArrayList<>();
        IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl();
        ramlDocumentBuilder.addPathLookupFirst(resourceFolder);
        IRamlValidationService validationService = new RamlValidationServiceImpl(ramlDocumentBuilder);
        IRamlValidationService result = validationService.validate(resourceContent, rootFileName);
        for (IValidationResult validationResult : result.getErrors())
        {
            errorsList.add(validationResult.getMessage());
        }
        return  errorsList;
    }

    public static IRaml build(String content, String resourceFolder, String rootFileName)
    {
        IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl();
        ramlDocumentBuilder.addPathLookupFirst(resourceFolder);
        return ramlDocumentBuilder.build(content, rootFileName);
    }

    public static List<String> detectIncludes(String ramlPath, ResourceLoader resourceLoader) throws IOException {
        try {
            //We only need to check includes on raml files
            if(!(ramlPath.endsWith(".raml") || ramlPath.endsWith(".yaml"))){
                return Collections.emptyList();
            }
            final String content = IOUtils.toString(resourceLoader.fetchResource(ramlPath));
            final String rootFilePath = ramlPath.contains("/")?ramlPath.substring(0, ramlPath.lastIndexOf("/")):ramlPath;

            final Node rootNode = YAML_PARSER.compose(new StringReader(content));
            if (rootNode == null) {
                return Collections.emptyList();
            } else {
                return new ArrayList<>(includedFilesIn(rootFilePath, rootNode, resourceLoader));
            }
        } catch (final MarkedYAMLException e) {
            return Collections.emptyList();
        }
    }

    private static Set<String> includedFilesIn(final String rootFileUri, final Node rootNode, ResourceLoader resourceLoader)
            throws IOException {
        final Set<String> includedFiles = new HashSet<>();
        if (rootNode.getNodeId() == NodeId.scalar) {
            final ScalarNode includedNode = (ScalarNode) rootNode;
            final Tag nodeTag = includedNode.getTag();
            if (nodeTag != null && nodeTag.toString().equals(INCLUDE_KEYWORD)) {
                String includedNodeValue = includedNode.getValue();
                String includeUri = rootFileUri + "/" + includedNodeValue;
                String normalized = includeUri.replace("/", File.separator);
                if (resourceLoader.fetchResource(normalized) != null) {
                    includedFiles.add(normalized);
                    includedFiles.addAll(detectIncludes(normalized, resourceLoader));
                }
            }
        } else if (rootNode.getNodeId() == NodeId.mapping) {
            final MappingNode mappingNode = (MappingNode) rootNode;
            final List<NodeTuple> children = mappingNode.getValue();
            for (final NodeTuple childNode : children) {
                final Node valueNode = childNode.getValueNode();
                includedFiles.addAll(includedFilesIn(rootFileUri, valueNode, resourceLoader));
            }
        } else if (rootNode.getNodeId() == NodeId.sequence) {
            final SequenceNode sequenceNode = (SequenceNode) rootNode;
            final List<Node> children = sequenceNode.getValue();
            for (final Node childNode : children) {
                includedFiles.addAll(includedFilesIn(rootFileUri, childNode, resourceLoader));
            }
        }
        return includedFiles;
    }
}
