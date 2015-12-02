/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.apache.maven.plugin.logging.Log;

import org.mule.tools.apikit.input.MuleDomainParser;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.GenerationStrategy;
import org.mule.tools.apikit.output.MuleConfigGenerator;
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Scaffolder {
    private final MuleConfigGenerator muleConfigGenerator;

    public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory,
                                              List<String> specFiles, List<String> muleXmlFiles)
            throws IOException{
        return createScaffolder(log, muleXmlOutputDirectory, specFiles, muleXmlFiles, null, null);
    }


    public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory,
                                              List<String> specFiles, List<String> muleXmlFiles, String domainFile)
            throws IOException{
        return createScaffolder(log, muleXmlOutputDirectory, specFiles, muleXmlFiles, domainFile, null);
    }

    public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory,
                                              List<String> specFiles, List<String> muleXmlFiles, String domainFile, String muleVersion) throws IOException
    {
        FileListUtils fileUtils = new FileListUtils(log);
        Map<File, InputStream> fileInputStreamMap = fileUtils.toStreamsOrFail(specFiles);
        Map<File, InputStream> streams = fileUtils.toStreamsOrFail(muleXmlFiles);
        InputStream domainStream = getDomainStream(log, domainFile);
        return new Scaffolder(log, muleXmlOutputDirectory, fileInputStreamMap, streams, domainStream, muleVersion);
    }

    public Scaffolder(Log log, File muleXmlOutputDirectory,  Map<File, InputStream> ramls,
                      Map<File, InputStream> xmls, InputStream domainStream, String muleVersion)  {
        MuleDomainParser muleDomainParser = new MuleDomainParser(log, domainStream);
        APIFactory apiFactory = new APIFactory(muleDomainParser.getHttpListenerConfigs());
        MuleConfigParser muleConfigParser = new MuleConfigParser(log, ramls.keySet(), xmls, apiFactory);
        RAMLFilesParser RAMLFilesParser = new RAMLFilesParser(log, ramls, apiFactory, muleVersion);
        List<GenerationModel> generationModels = new GenerationStrategy(log).generate(RAMLFilesParser, muleConfigParser);
        muleConfigGenerator = new MuleConfigGenerator(log, muleXmlOutputDirectory, generationModels, muleDomainParser.getHttpListenerConfigs(), muleVersion);
    }

    private static InputStream getDomainStream(Log log, String domainPath)
    {
        InputStream domainStream = null;
        if (domainPath != null)
        {
            File domain = null;
            try
            {
                domain = new File(domainPath);
                domainStream = new FileInputStream(domain);
            } catch (FileNotFoundException e)
            {
                if (log != null)
                {
                    log.error("Error opening file [" + domain + "] file", e);
                }
                else
                {
                    throw new RuntimeException(e);
                }
            }
        }
        return domainStream;
    }

    public void run() {
        muleConfigGenerator.generate();
    }


}
