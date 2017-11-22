/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.loader;

import org.raml.v2.api.loader.ClassPathResourceLoader;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.FileResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ExchangeDependencyResourceLoader implements ResourceLoader {

    private static final String BASE_PATH = ".";
    private final File workingDir;
    private final ResourceLoader resourceLoader;

    private static final Pattern DEPENDENCY_PATH_PATTERN = Pattern.compile("^exchange_modules/|/exchange_modules/");

    public ExchangeDependencyResourceLoader() {
        this(BASE_PATH);
    }

    public ExchangeDependencyResourceLoader(String path) {
        this.workingDir = new File(path);
        resourceLoader = new CompositeResourceLoader(new FileResourceLoader(path), new ClassPathResourceLoader());
    }

    @Nullable
    @Override
    public InputStream fetchResource(String path) {
        if (isNullOrEmpty(path)) {
            return null;
        }

        final String resourceName;

        final Matcher matcher = DEPENDENCY_PATH_PATTERN.matcher(path);
        if (matcher.find()) {
            final int dependencyIndex = path.lastIndexOf(matcher.group(0));
            resourceName = dependencyIndex <= 0 ? path : path.substring(dependencyIndex);
        } else {
            resourceName = path;
        }

        return resourceLoader.fetchResource(new File(workingDir, resourceName).getPath());
    }
}