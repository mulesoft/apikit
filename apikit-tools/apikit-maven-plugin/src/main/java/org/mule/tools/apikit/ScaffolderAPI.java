/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.logging.SystemStreamLog;

public class ScaffolderAPI {

    private final static List<String> apiExtensions = Arrays.asList(".yaml", ".raml", ".yml");
    private final static List<String> appExtensions = Arrays.asList(".xml");

    public ScaffolderAPI() {
        
    }
    
    /**
     * Modifies or creates the Mule config files which are contained in the appDir directory
     * by running the scaffolder on the yamlFiles passed as parameter.
     *  
     * @param yamlFiles the yamlFiles to which the scaffolder will be run on
     * @param appDir the directory which contained the generated Mule config files
     */
    public void run(List<File> yamlFiles, File appDir) {
        List<String> muleXmlFiles = retrieveFilePaths(appDir, appExtensions);
        List<String> yamlFilePaths = retrieveFilePaths(yamlFiles, apiExtensions);
        Scaffolder scaffolder;
        try {
            scaffolder = Scaffolder.createScaffolder(new SystemStreamLog(), appDir, yamlFilePaths, muleXmlFiles);
        } catch(Exception e) {
            throw new RuntimeException("Error executing scaffolder", e);
        }
        scaffolder.run();
    }

    private List<String> retrieveFilePaths(File dir, final List<String> extensions) {
        if(!dir.isDirectory()) {
            throw new IllegalArgumentException("File " + dir.getName() + " must be a directory");
        }
        return retrieveFilePaths(new ArrayList<File>(Arrays.asList(dir.listFiles())), extensions);
    }

    private List<String> retrieveFilePaths(List<File> files, List<String> extensions) {
        List<String> filePaths = new ArrayList<String>();
        if(files != null) {
            for(File file : files) {
                if (containsValidExtension(file, extensions)) {
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }
        return filePaths;
    }

    private boolean containsValidExtension(File file, List<String> extensions) {
        for (String extension : extensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}
