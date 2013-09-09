package org.mule.tools.apikit;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.mule.tools.apikit.output.GenerationModel;
import org.mule.tools.apikit.output.GenerationModelProvider;
import org.mule.tools.apikit.output.GenerationStrategy;
import org.mule.tools.apikit.output.MuleConfigGenerator;
import org.mule.tools.apikit.misc.FileListUtils;
import org.mule.tools.apikit.model.ResourceActionPair;
import org.mule.tools.apikit.input.MuleConfigParser;
import org.mule.tools.apikit.input.RAMLFilesParser;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class Scaffolder {
    private final Log log;
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
        this.log = log;

        GenerationModelProvider generationModelProvider = new GenerationModelProvider();
        RAMLFilesParser RAMLFilesParser = new RAMLFilesParser(log, generationModelProvider, yamls);
        MuleConfigParser muleConfigParser = new MuleConfigParser(log, yamls.keySet(),
                xmls);

        Set<ResourceActionPair> generate = new GenerationStrategy(log)
                .generate(RAMLFilesParser, muleConfigParser);
        Set<GenerationModel> generationModels = generationModelProvider.generate(generate);
        muleConfigGenerator = new MuleConfigGenerator(log, muleXmlOutputDirectory, generationModels);
    }

    public void run() {
        muleConfigGenerator.generate();
    }


}
