/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2;

import org.mule.raml.implv2.parser.rule.ValidationResultImpl;
import org.mule.raml.implv2.v08.model.RamlImpl08V2;
import org.mule.raml.implv2.v10.model.RamlImpl10V2;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.system.types.AnnotableSimpleType;
import org.raml.v2.internal.impl.commons.nodes.LibraryNodeProvider;
import org.raml.v2.internal.impl.v10.nodes.LibraryLinkNode;
import org.raml.yagi.framework.nodes.Node;
import org.raml.yagi.framework.nodes.snakeyaml.SYIncludeNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParserV2Utils {

  public static final String PARSER_V2_PROPERTY = "apikit.raml.parser.v2";
  private static final String RAML_PATH_SEPARATOR = "/";

  public static IRaml build(ResourceLoader resourceLoader, String ramlPath) {
    RamlModelResult ramlModelResult = new RamlModelBuilder(resourceLoader).buildApi(ramlPath);
    return wrapApiModel(ramlModelResult, resourceLoader, ramlPath);
  }

  public static IRaml build(ResourceLoader resourceLoader, String ramlPath, String content) {
    RamlModelResult ramlModelResult = new RamlModelBuilder(resourceLoader).buildApi(content, ramlPath);
    return wrapApiModel(ramlModelResult, resourceLoader, ramlPath);
  }

  private static IRaml wrapApiModel(RamlModelResult ramlModelResult, ResourceLoader resourceLoader, String ramlPath) {
    if (ramlModelResult.hasErrors()) {
      throw new RuntimeException("Invalid RAML descriptor.");
    }
    if (ramlModelResult.isVersion08()) {
      return new RamlImpl08V2(ramlModelResult.getApiV08());
    }
    return new RamlImpl10V2(ramlModelResult.getApiV10(), resourceLoader, ramlPath);
  }

  public static List<IValidationResult> validate(ResourceLoader resourceLoader, String ramlPath, String content) {
    List<IValidationResult> result = new ArrayList<>();

    try {
      RamlModelResult ramlApiResult = new RamlModelBuilder(resourceLoader).buildApi(content, ramlPath);
      for (ValidationResult validationResult : ramlApiResult.getValidationResults()) {
        result.add(new ValidationResultImpl(validationResult));
      }
    } catch (Exception e) {
      throw new RuntimeException("Raml parser uncaught exception: " + e.getMessage());
    }
    return result;
  }

  public static List<IValidationResult> validate(ResourceLoader resourceLoader, String ramlPath) {
    return validate(resourceLoader, ramlPath, null);
  }

  public static boolean useParserV2(String content) {
    String property = System.getProperty(PARSER_V2_PROPERTY);
    if (property != null && Boolean.valueOf(property)) {
      return true;
    } else {
      return content.startsWith("#%RAML 1.0");
    }
  }

  public static String nullSafe(AnnotableSimpleType<?> simpleType) {
    return simpleType != null ? String.valueOf(simpleType.value()) : null;
  }

  public static List<String> findIncludeNodes(final Node raml, String ramlPath) {
    final List<String> includePaths = new ArrayList<>();
    findIncludeNodes("", includePaths, Arrays.asList(raml), ramlPath);
    return includePaths;
  }

  private static void findIncludeNodes(final String pathRelativeToRoot, final List<String> includePaths,
                                       final List<Node> currents, String ramlPath) {
    final String rootPath = new File(ramlPath).getParent();

    for (final Node current : currents) {
      // search for include in sources of the current node
      Node possibleInclude = current;
      String pathRelativeToRootCurrent = pathRelativeToRoot;
      while (possibleInclude != null) {
        String includePath = null;
        if (possibleInclude instanceof SYIncludeNode) {
          includePath = ((SYIncludeNode) possibleInclude).getIncludePath();
        } else if (possibleInclude instanceof LibraryLinkNode) {
          includePath = ((LibraryLinkNode) possibleInclude).getRefName();
        }

        if (includePath != null) {
          includePaths.add(computeIncludePath(rootPath, pathRelativeToRoot, includePath));
          pathRelativeToRootCurrent = calculateNextRootRelative(pathRelativeToRootCurrent, includePath);
        }

        possibleInclude = possibleInclude.getSource();
      }

      findIncludeNodes(pathRelativeToRootCurrent, includePaths, getChildren(current), ramlPath);
    }
  }

  private static String calculateNextRootRelative(String pathRelativeToRootCurrent, String includePath) {
    String newRelativeSubPath = new File(includePath).getParent();
    newRelativeSubPath = newRelativeSubPath == null ? "" : newRelativeSubPath;
    return pathRelativeToRootCurrent + newRelativeSubPath;
  }

  private static List<Node> getChildren(Node node) {
    if (node instanceof LibraryLinkNode) {
      node = ((LibraryLinkNode) node).getRefNode();
    }
    List<Node> result = new ArrayList<>();
    if (node != null) {
      if (node instanceof LibraryNodeProvider) {
        LibraryNodeProvider libraryNodeProvider = (LibraryNodeProvider) node;
        Node libraryNode = libraryNodeProvider.getLibraryNode();
        if (libraryNode != null) {
          result.add(libraryNode);
        }
      }
      result.addAll(node.getChildren());
    }

    return result;
  }

  private static String computeIncludePath(final String rootPath, final String pathRelativeToRoot, final String includePath) {
    // according to RAML 1.0 spec: https://github.com/raml-org/raml-spec/blob/master/versions/raml-10/raml-10.md
    String resolvedPath = isAbsolute(includePath) //
        ? rootPath + includePath
        // relative path: A path that neither begins with a single slash ("/") nor constitutes a URL, and is interpreted relative to the location of the included file.
        : rootPath + (pathRelativeToRoot.isEmpty() ? "" : File.separator + pathRelativeToRoot) + File.separator + includePath;

    // uses File class to normalize the resolved path acording with the OS (every slash in the path must be in the same direction)
    return new File(resolvedPath).getPath();
  }

  private static boolean isAbsolute(String includePath) {
    // absolute path: A path that begins with a single slash ("/") and is interpreted relative to the root RAML file location.
    return includePath.startsWith(RAML_PATH_SEPARATOR);
  }

}
