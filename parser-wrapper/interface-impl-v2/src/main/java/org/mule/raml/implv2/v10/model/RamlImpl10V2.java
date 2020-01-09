/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10.model;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mule.raml.implv2.ParserV2Utils.nullSafe;

import com.google.common.base.Optional;
import org.apache.commons.io.IOUtils;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.ISecurityScheme;
import org.mule.raml.interfaces.model.ITemplate;
import org.mule.raml.interfaces.model.parameter.IParameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.raml.v2.api.loader.ResourceLoader;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.datamodel.AnyTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ExternalTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.resources.Resource;
import org.raml.v2.internal.impl.RamlBuilder;
import org.raml.v2.internal.impl.commons.nodes.LibraryNodeProvider;
import org.raml.v2.internal.impl.v10.nodes.LibraryLinkNode;
import org.raml.yagi.framework.nodes.Node;
import org.raml.yagi.framework.nodes.snakeyaml.SYIncludeNode;

public class RamlImpl10V2 implements IRaml
{
    public static final String PARSER_V2_PROPERTY = "apikit.raml.parser.v2";
    private static final String RAML_PATH_SEPARATOR = "/";
    private final String ramlPath;
    private final ResourceLoader resourceLoader;
    private Api api;
    private Optional<String> version;

    public RamlImpl10V2(Api api, ResourceLoader resourceLoader, String ramlPath)
    {
        this.api = api;
        this.ramlPath = ramlPath;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Map<String, IResource> getResources()
    {
        Map<String, IResource> map = new LinkedHashMap<>();
        List<Resource> resources = api.resources();
        for (Resource resource : resources)
        {
            map.put(resource.relativeUri().value(), new ResourceImpl(resource));
        }
        return map;
    }

    @Override
    public String getBaseUri()
    {
        return nullSafe(api.baseUri());
    }

    @Override
    public String getVersion()
    {
        if (version == null) {
            version = Optional.fromNullable(nullSafe(api.version()));
        }
        return version.orNull();
    }

    @Override
    public List<Map<String, String>> getSchemas()
    {
        Map<String, String> map = new LinkedHashMap<>();
        List<TypeDeclaration> types = api.types();
        if (types.isEmpty())
        {
            types = api.schemas();
        }
        for (TypeDeclaration typeDeclaration : types)
        {
            map.put(typeDeclaration.name(), getTypeAsString(typeDeclaration));
        }
        List<Map<String, String>> result = new ArrayList<>();
        result.add(map);
        return result;
    }

    static String getTypeAsString(TypeDeclaration typeDeclaration)
    {
        if (typeDeclaration instanceof ExternalTypeDeclaration)
        {
            return ((ExternalTypeDeclaration) typeDeclaration).schemaContent();
        }
        if (typeDeclaration instanceof AnyTypeDeclaration)
        {
            return null;
        }
        //return non-null value in order to detect that a schema was defined
        return "[yaml-type-flag]";
    }

    @Override
    public IResource getResource(String path)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getConsolidatedSchemas()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getCompiledSchemas()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, IParameter> getBaseUriParameters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, ISecurityScheme>> getSecuritySchemes()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, ITemplate>> getTraits()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUri()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getInstance()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanBaseUriParameters()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void injectTrait(String name)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void injectSecurityScheme(Map<String, ISecurityScheme> securityScheme)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getAllReferences() {
        try {
            return findIncludeNodes(getPathAsUri(ramlPath), resourceLoader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emptyList();
    }

    private URI getPathAsUri(String path) {
        final String normalizedPath = path.replace(File.separator, "/");
        return URI.create(normalizedPath);
    }

    public static List<String> findIncludeNodes(URI ramlURI, ResourceLoader resourceLoader) throws IOException {
        String rootPath = getParent(ramlURI);
        return findIncludeNodes(rootPath, ramlURI, resourceLoader);
    }

    public static List<String> findIncludeNodes(String rootPath, URI ramlURI, ResourceLoader resourceLoader) throws IOException {
        final InputStream is = resourceLoader.fetchResource(URLDecoder.decode(ramlURI.toString()));

        if (is == null) {
            return emptyList();
        }
        Node raml;
        try {
            raml = new RamlBuilder().build(IOUtils.toString(is), resourceLoader, rootPath);
        } catch (Exception e) {
            return emptyList();
        }
        return findIncludeNodes(rootPath, raml, resourceLoader);
    }

    public static List<String> findIncludeNodes(String rootPath, final Node raml, ResourceLoader resourceLoader)
            throws IOException {
        final Set<String> includePaths = new HashSet<>();
        findIncludeNodes(rootPath, "", includePaths, singletonList(raml), resourceLoader);
        return new ArrayList<>(includePaths);
    }

    private static void findIncludeNodes(String rootPath, final String pathRelativeToRoot, final Set<String> includePaths,
                                         final List<Node> currents, ResourceLoader resourceLoader)
            throws IOException {

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
                    final String absolutIncludePath = computeIncludePath(rootPath, pathRelativeToRoot, includePath);
                    final URI includedFileAsUri = URI.create(URLEncoder.encode(absolutIncludePath)).normalize();
                    includePaths.add(URLDecoder.decode(includedFileAsUri.toString()));
                    includePaths.addAll(findIncludeNodes(rootPath, includedFileAsUri, resourceLoader));
                    pathRelativeToRootCurrent = calculateNextRootRelative(pathRelativeToRootCurrent,
                            includePath);
                }

                possibleInclude = possibleInclude.getSource();
            }

            findIncludeNodes(rootPath, pathRelativeToRootCurrent, includePaths, getChildren(current), resourceLoader);
        }
    }
    private static String computeIncludePath(final String rootPath, final String pathRelativeToRoot, final String includePath) {
        // according to RAML 1.0 spec: https://github.com/raml-org/raml-spec/blob/master/versions/raml-10/raml-10.md

        // uses File class to normalize the resolved path acording with the OS (every slash in the path must be in the same direction)
        final String absolutePath = isAbsolute(includePath) //
                ? rootPath + includePath
                // relative path: A path that neither begins with a single slash ("/") nor constitutes a URL, and is interpreted relative to the location of the included file.
                : rootPath + (pathRelativeToRoot.isEmpty() ? "" : "/" + pathRelativeToRoot) + "/" + includePath;
        return fixExchangeModulePath(absolutePath);
    }

    private static boolean isAbsolute(String includePath) {
        // absolute path: A path that begins with a single slash ("/") and is interpreted relative to the root RAML file location.
        return includePath.startsWith(RAML_PATH_SEPARATOR);
    }
    private static String getParent(URI uri) {
        final URI parentUri = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
        final String parentUriAsString = parentUri.toString();
        return parentUriAsString.endsWith("/") ? parentUriAsString.substring(0, parentUriAsString.length() - 1) : parentUriAsString;
    }

    private static String fixExchangeModulePath(String path) {
        return getExchangeModulePath(path);
    }

    private static String calculateNextRootRelative(String pathRelativeToRootCurrent, String includePath) {
        String newRelativeSubPath = getParent(URI.create(URLEncoder.encode(includePath)));
        newRelativeSubPath = newRelativeSubPath == null ? "" : URLDecoder.decode(newRelativeSubPath);
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


    private static final Pattern DEPENDENCY_PATH_PATTERN = Pattern.compile("^exchange_modules/|/exchange_modules/");


    public static String getExchangeModulePath(String path) {
        final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(path);
        if (matcher.find()) {
            final String matching = matcher.group(0);
            final int dependencyIndex = path.lastIndexOf(matching);

            if (dependencyIndex <= 0)
                return path;
            else {
                final String rootPath = path.substring(0, path.indexOf(matching));
                final String exchangeModulePath = path.substring(dependencyIndex);
                return Paths.get(rootPath + "/" + exchangeModulePath).normalize().toString();
            }
        } else {
            return path;
        }
    }

}
