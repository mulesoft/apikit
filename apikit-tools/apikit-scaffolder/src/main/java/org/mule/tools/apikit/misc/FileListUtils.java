/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileListUtils {

    private final Log log;

    public FileListUtils(Log log) {
        this.log = log;
    }

    public FileListUtils() {
        this.log = null;
    }

    public Map<File,InputStream> toStreamsOrFail(List<String> absolutePaths) throws IOException
    {
        Map<File,InputStream> streams = toFiles(absolutePaths);

        // If none of the absolutePaths could be processed throw an exception and abort execution
        if ((streams == null || streams.size() == 0) && absolutePaths.size() > 0) {
            throw new IOException("Error: None of the following files could be parsed: " + absolutePaths);
        }

        return streams;
    }

    public Map<File,InputStream> toFiles(List<String> absolutePaths) {
        Map<File,InputStream> fileStreams = new HashMap<File, InputStream>();

        for (String absolutePath : absolutePaths) {
            createFile(fileStreams, new File(absolutePath));
        }

        return fileStreams;
    }

    public Map<File,InputStream> toStreamFromFiles(List<File> files) {
        Map<File,InputStream> fileStreams = new HashMap<File, InputStream>();

        for (File file : files) {
            createFile(fileStreams, file);
        }

        return fileStreams;
    }

    void createFile(Map<File, InputStream> fileStreams, File file) {
        try {
            File absoluteFile = file.getAbsoluteFile();
            fileStreams.put(absoluteFile, new FileInputStream(absoluteFile));
        } catch (FileNotFoundException e) {
            if (log != null) {
                log.error("Error opening file [" + file + "]", e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
