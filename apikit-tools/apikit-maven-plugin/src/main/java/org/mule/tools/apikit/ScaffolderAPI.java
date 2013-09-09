package org.mule.tools.apikit;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.logging.SystemStreamLog;

public class ScaffolderAPI {

    private final static List<String> apiExtensions = Arrays.asList(".yaml", ".raml", ".yml");
    private final static List<String> appExtensions = Arrays.asList(".xml");

    private final File apiDir;
    private final File appDir;

    public ScaffolderAPI(File apiDir, File appDir) {
        this.apiDir = apiDir;
        this.appDir = appDir;
    }

    public void run() {
        List<String> muleXmlFiles = retrieveFilePaths(appDir, appExtensions);
        List<String> yamlFiles = retrieveFilePaths(apiDir, apiExtensions);
        Scaffolder scaffolder;
        try {
            scaffolder = Scaffolder.createScaffolder(new SystemStreamLog(), appDir, yamlFiles, muleXmlFiles);
        } catch(Exception e) {
            throw new RuntimeException("Error executing scaffolder", e);
        }
        scaffolder.run();
    }

    private List<String> retrieveFilePaths(File dir, final List<String> extensions) {
        List<String> filePaths = new ArrayList<String>();
        if(!dir.isDirectory()) {
            throw new IllegalArgumentException("File " + dir.getName() + " must be a directory");
        }

        File[] files = dir.listFiles(new FilenameFilter()  {
            @Override
            public boolean accept(File file, String fileName) {
                for(String extension : extensions) {
                    if(fileName.endsWith(extension)) {
                        return true;
                    }
                }
                return false;
            }
        });

        if(files != null) {
            for(File file : files) {
                filePaths.add(file.getAbsolutePath());
            }
        }

        return filePaths;
    }

}
