/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.MuleDomainParser;
import org.mule.tools.apikit.input.RAMLFilesParser;
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.model.APIFactory;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.GenerationStrategy;
import org.mule.tools.apikit.output.MuleConfigGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.plugin.logging.Log;

public class Scaffolder
{
    private final MuleConfigGenerator muleConfigGenerator;

    public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory, List<String> specFiles, List<String> muleXmlFiles) throws IOException
    {
        return createScaffolder(log, muleXmlOutputDirectory, specFiles, muleXmlFiles, null, null, null);
    }

    public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory, List<String> specFiles, List<String> muleXmlFiles, String domainFile) throws IOException
    {
        return createScaffolder(log, muleXmlOutputDirectory, specFiles, muleXmlFiles, domainFile, null, null);
    }

    public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory, List<String> specFiles, List<String> muleXmlFiles, String domainFile, String muleVersion) throws IOException
    {
        return createScaffolder(log, muleXmlOutputDirectory, specFiles, muleXmlFiles, domainFile, muleVersion, null);
    }

    public static Scaffolder createScaffolder(Log log, File muleXmlOutputDirectory, List<String> specPaths, List<String> muleXmlPaths, String domainPath, String muleVersion, List<String> ramlsWithExtensionEnabledPaths) throws IOException
    {
        FileListUtils fileUtils = new FileListUtils(log);
        Map<File, InputStream> ramlStreams = fileUtils.toStreamsOrFail(specPaths);
        Map<File, InputStream> muleStreams = fileUtils.toStreamsOrFail(muleXmlPaths);
        Set<File> ramlWithExtensionEnabled = new TreeSet<>();
        if (ramlsWithExtensionEnabledPaths != null)
        {
            for (String ramlWithEE : ramlsWithExtensionEnabledPaths)
            {
                ramlWithExtensionEnabled.add(new File(ramlWithEE));
            }
        }
        InputStream domainStream = getDomainStream(log, domainPath);
        return new Scaffolder(log, muleXmlOutputDirectory, ramlStreams, muleStreams, domainStream, muleVersion, ramlWithExtensionEnabled);
    }

    public Scaffolder(Log log, File muleXmlOutputDirectory,  Map<File, InputStream> ramls, Map<File, InputStream> xmls, InputStream domainStream, String muleVersion, Set<File> ramlsWithExtensionEnabled)
    {
        MuleDomainParser muleDomainParser = new MuleDomainParser(log, domainStream);
        APIFactory apiFactory = new APIFactory(muleDomainParser.getHttpListenerConfigs());
        MuleConfigParser muleConfigParser = new MuleConfigParser(log, apiFactory).parse(ramls.keySet(), xmls);
        RAMLFilesParser RAMLFilesParser = new RAMLFilesParser(log, ramls, apiFactory, muleVersion);
        List<GenerationModel> generationModels = new GenerationStrategy(log).generate(RAMLFilesParser, muleConfigParser);
        muleConfigGenerator = new MuleConfigGenerator(log, muleXmlOutputDirectory, generationModels, muleDomainParser.getHttpListenerConfigs(), muleVersion, ramlsWithExtensionEnabled);
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

    public void run()
    {
        muleConfigGenerator.generate();
    }


}
