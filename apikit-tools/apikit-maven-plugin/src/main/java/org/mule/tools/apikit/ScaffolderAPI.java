/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.mule.tools.apikit.model.manager.CustomRamlGenerator;
import org.mule.tools.apikit.model.manager.FileUtils;

public class ScaffolderAPI {

    private final static List<String> apiExtensions = Arrays.asList(".yaml", ".raml", ".yml");
    private final static List<String> appExtensions = Arrays.asList(".xml");
    private final static List<String> dataModelExtensions = Arrays.asList(".json");

    public ScaffolderAPI() {
        
    }
    
    /**
     * Modifies or creates the Mule config files which are contained in the appDir directory
     * by running the scaffolder on the ramlFiles passed as parameter.
     *  
     * @param ramlFiles the ramlFiles to which the scaffolder will be run on
     * @param appDir the directory which contained the generated Mule config files
     */

    public void run(List<File> ramlFiles, File appDir)
    {
        run(ramlFiles, appDir, null);
    }

    /**
     * Modifies or creates the Mule config files which are contained in the appDir directory
     * by running the scaffolder on the ramlFiles passed as parameter.
     *
     * @param ramlFiles the ramlFiles to which the scaffolder will be run on
     * @param appDir the directory which contained the generated Mule config files
     * @param domainDir the directory which contained the domain used by the mule config files
     */
    public void run(List<File> ramlFiles, File appDir, File domainDir) {
	
	List<String> ramlFilePaths = processDataModelFiles(ramlFiles, dataModelExtensions);
	ramlFilePaths.addAll(retrieveFilePaths(ramlFiles, apiExtensions));
        List<String> muleXmlFiles = retrieveFilePaths(appDir, appExtensions);
        SystemStreamLog log = new SystemStreamLog();
        String domain = null;
        if (domainDir != null)
        {
            List<String> domainFiles = retrieveFilePaths(domainDir,appExtensions);
            if (domainFiles.size() > 0)
            {
                domain = domainFiles.get(0);
                if (domainFiles.size() > 1)
                {
                    log.info("There is more than one domain file inside of the domain folder. The domain: " + domain + " will be used.");
                }
            }
        }
        Scaffolder scaffolder;
        try {
            scaffolder = Scaffolder.createScaffolder(log, appDir, ramlFilePaths, muleXmlFiles, domain);
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

    private List<String> processDataModelFiles(List<File> files, List<String> extensions) {
        List<String> ramlFilePaths = new ArrayList<String>();
        if(files != null) {
            for(File file : files) {
                if (containsValidExtension(file, extensions) && isValidDataModel(file)) {
                    ramlFilePaths.add(generateRamlFromDataModel(file).getAbsolutePath());
                }
            }
        }
        return ramlFilePaths;
    }
    
    private File generateRamlFromDataModel(File model) {
	CustomRamlGenerator ramlGenerator = new CustomRamlGenerator();
	File raml = null;

	try {
	    String ramlContents = ramlGenerator.generate(new FileInputStream(model));
	    String path = model.getCanonicalPath().replace(".json", ".raml");
	    raml = FileUtils.stringToFile(path, ramlContents);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException("Error parsing data model file", e);
	}

	return raml;
    }
    
    private boolean isValidDataModel(File file) {
	CustomRamlGenerator ramlGenerator = new CustomRamlGenerator();
	try {
	    return ramlGenerator.isModelValid(new FileInputStream(file));
	} catch (Exception e) {
	    return false;
	}
	
    }
}
