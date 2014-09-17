/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.GenerationStrategy;
import org.mule.tools.apikit.output.MuleConfigGenerator;
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class Scaffolder {
    private final MuleConfigGenerator muleConfigGenerator;

    public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory,
                                   List<String> specFiles, List<String> muleXmlFiles)
            throws MojoExecutionException {
        FileListUtils fileUtils = new FileListUtils(log);

        Map<File, InputStream> fileInputStreamMap = fileUtils.toStreamsOrFail(specFiles);
        Map<File, InputStream> streams = fileUtils.toStreamsOrFail(muleXmlFiles);

        return new Scaffolder(log, muleXmlOutputDirectory, fileInputStreamMap, streams);
    }

    public Scaffolder(Log log, File muleXmlOutputDirectory,  Map<File, InputStream> yamls,
                      Map<File, InputStream> xmls)  {
        APIFactory apiFactory = new APIFactory();
        RAMLFilesParser RAMLFilesParser = new RAMLFilesParser(log, yamls, apiFactory);
        MuleConfigParser muleConfigParser = new MuleConfigParser(log, yamls.keySet(), xmls, apiFactory);
        List<GenerationModel> generationModels = new GenerationStrategy(log).generate(RAMLFilesParser, muleConfigParser);
        muleConfigGenerator = new MuleConfigGenerator(log, muleXmlOutputDirectory, generationModels);
    }

    public void run() {
        muleConfigGenerator.generate();
    }


}
