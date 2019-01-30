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

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParserV1Utils {

  private static final Yaml YAML_PARSER = new Yaml();
  private static final String INCLUDE_KEYWORD = "!include";

  public static List<String> validate(ResourceLoader resourceLoader, String rootFileName, String resourceContent) {
    return validate(null, resourceLoader, rootFileName, resourceContent);
  }

  public static List<String> validate(String resourceFolder, String rootFileName, String resourceContent) {
    return validate(resourceFolder, null, rootFileName, resourceContent);
  }

  public static IRaml build(String content, String resourceFolder, String rootFileName) {
    return build(content, resourceFolder, null, rootFileName);
  }

  public static IRaml build(String content, ResourceLoader resourceLoader, String rootFileName) {
    return build(content, null, resourceLoader, rootFileName);
  }

  private static List<String> validate(String resourceFolder, ResourceLoader resourceLoader, String rootFileName,
                                       String resourceContent) {
    IRamlDocumentBuilder ramlDocumentBuilder = getIRamlDocumentBuilder(resourceFolder, resourceLoader);

    List<String> errorsList = new ArrayList<>();
    IRamlValidationService validationService = new RamlValidationServiceImpl(ramlDocumentBuilder);
    IRamlValidationService result = validationService.validate(resourceContent, rootFileName);
    for (IValidationResult validationResult : result.getErrors()) {
      errorsList.add(validationResult.getMessage());
    }
    return errorsList;
  }

  public static IRaml build(String content, String resourceFolder, ResourceLoader resourceLoader, String rootFileName) {
    IRamlDocumentBuilder ramlDocumentBuilder = getIRamlDocumentBuilder(resourceFolder, resourceLoader);

    return ramlDocumentBuilder.build(content, rootFileName);
  }

  private static IRamlDocumentBuilder getIRamlDocumentBuilder(String resourceFolder, ResourceLoader resourceLoader) {
    IRamlDocumentBuilder ramlDocumentBuilder;
    if (resourceLoader == null) {
      ramlDocumentBuilder = new RamlDocumentBuilderImpl();
    } else {
      ramlDocumentBuilder = new RamlDocumentBuilderImpl(resourceLoader);
    }

    if (resourceFolder != null) {
      ramlDocumentBuilder.addPathLookupFirst(resourceFolder);
    }
    return ramlDocumentBuilder;
  }

  public static List<String> detectIncludes(URI ramlUri, ResourceLoader resourceLoader) throws IOException {
    try {
      final String ramlUriAsString = ramlUri.toString();
      final String content = IOUtils.toString(resourceLoader.fetchResource(ramlUriAsString));
      final String rootFilePath = ramlUriAsString.substring(0, ramlUriAsString.lastIndexOf("/"));

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
        final String includedNodeValue = includedNode.getValue();
        final String includeUriAsString = rootFileUri + "/" + includedNodeValue;

        final URI includeUri = URI.create(includeUriAsString);
        if (resourceLoader.fetchResource(includeUriAsString) != null) {
          includedFiles.add(includeUriAsString);
          includedFiles.addAll(detectIncludes(includeUri, resourceLoader));
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
