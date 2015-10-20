/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.DirectoryScanner;

public class Main
{
    /**
     * Pattern of where to find the spec .raml, .yaml or .yml files.
     */
    private static String[] specIncludes = new String[]{"src/main/api/**/*.yaml", "src/main/api/**/*.yml", "src/main/api/**/*.raml"};

    /**
     * Pattern of what to exclude searching for .yaml files.
     */
    private static String[] specExcludes = new String[]{};

    /**
     * Spec source directory to use as root of specInclude and specExclude patterns.
     */
    //    @Parameter(defaultValue = "${basedir}")
    private static File specDirectory = new File("");

    /**
     * Pattern of where to find the Mule XMLs.
     */
    private static String[] muleXmlIncludes = new String[]{"src/main/app/**/*.xml", "src/main/resources/**/*.xml"};

    /**
     * Pattern of what to exclude searching for Mule XML files.
     */
    private static String[] muleXmlExcludes = new String[]{};

    /**
     * Spec source directory to use as root of muleInclude and muleExclude patterns.
     */
    //    @Parameter(defaultValue = "${basedir}")
    private static File muleXmlDirectory = new File("");

    /**
     * Where to output the generated mule config files.
     */
    //    @Parameter(defaultValue = "${basedir}/src/main/app")
    private static File muleXmlOutputDirectory = new File("src/main/app");

    /**
     * Spec source directory to use as root of muleDomain.
     */
    //    @Parameter (property = "domainDirectory")
    private static File domainDirectory;

    private static String muleVersion = "3.7.0";

    private static Log log;
    public Main()
    {
        //buildContext = new ThreadBuildContext();
    }

    public static void main(String[] args) throws IOException
    {
        Main main = new Main();

        main.readProperties(args);

        Validate.notNull(muleXmlDirectory, "Error: muleXmlDirectory parameter cannot be null");
        Validate.notNull(specDirectory, "Error: specDirectory parameter cannot be null");

        log = new SystemStreamLog();
        System.out.println( "spec " + specDirectory);
        List<String> specFiles = main.getIncludedFiles(specDirectory, specIncludes, specExcludes);
        List<String> muleXmlFiles = main.getIncludedFiles(muleXmlDirectory, muleXmlIncludes, muleXmlExcludes);
        String domainFile = null;

        if (domainDirectory != null)
        {
            List<String> domainFiles = main.getIncludedFiles(domainDirectory, new String[] {"*.xml"}, new String[] {});
            if (domainFiles.size() > 0)
            {
                domainFile = domainFiles.get(0);
                if (domainFiles.size() > 1) {
                    log.info("There is more than one domain file inside of the domain folder. The domain: " + domainFile + " will be used.");
                }
            }
            else
            {
                log.error("The specified domain directory [" + domainDirectory + "] does not contain any xml file.");
            }
        }
        else
        {
            log.info("No domain was provided. To send it, use -DdomainDirectory.");
        }
        log.info("Processing the following RAML files: " + specFiles);
        log.info("Processing the following xml files as mule configs: " + muleXmlFiles);

        Scaffolder scaffolder = Scaffolder.createScaffolder(log, muleXmlOutputDirectory, specFiles, muleXmlFiles,domainFile);
        scaffolder.run();
    }




    private void readProperties(String[] args)
    {
        for ( String arg : args)
        {
            if (arg.startsWith("-D"))
            {
                if (!arg.contains("="))
                {
                    System.out.println("The property [" + arg + "] does not have any value.");
                }
                else
                {
                    String key = arg.substring(2, arg.indexOf('=')).toLowerCase();
                    String value = arg.substring(arg.indexOf('=') + 1);
                    switch (key)
                    {
                        case "specdirectory":
                            specDirectory = new File(value);
                            break;
                        case "mulexmldirectory":
                            muleXmlDirectory = new File(value);
                            break;
                        case "mulexmloutputdirectory":
                            muleXmlOutputDirectory = new File(value);
                            break;
                        case "domaindirectory":
                            domainDirectory = new File(value);
                            break;
                        case "muleversion":
                            muleVersion = value;
                            break;
                    }
                }
            }
        }
    }

    private List<String> getIncludedFiles(File sourceDirectory, String[] includes, String[] excludes) {
        DirectoryScanner scanner = new DirectoryScanner();//buildContext.newScanner(sourceDirectory, true);
        scanner.setBasedir(sourceDirectory);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);
        scanner.scan();

        String[] includedFiles = scanner.getIncludedFiles();
        for (int i = 0; i < includedFiles.length; i++) {
            includedFiles[i] = new File(scanner.getBasedir(), includedFiles[i]).getAbsolutePath();
        }

        String[] result = new String[includedFiles.length];
        System.arraycopy(includedFiles, 0, result, 0, includedFiles.length);
        return Arrays.asList(result);
    }
}