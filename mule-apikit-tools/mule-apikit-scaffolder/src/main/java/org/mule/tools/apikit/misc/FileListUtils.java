/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

import static java.lang.String.format;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
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

  public Map<File, InputStream> toStreamsOrFail(List<String> absolutePaths) throws IOException {
    Map<File, InputStream> streams = toFiles(absolutePaths, element -> new File(element));

    // If none of the absolutePaths could be processed throw an exception and abort execution
    if ((streams == null || streams.size() == 0) && absolutePaths.size() > 0) {
      throw new IOException("Error: None of the following files could be parsed: " + absolutePaths);
    }

    return streams;
  }

  public <T> Map<File, InputStream> toFiles(List<T> elements, TransformToFile<T> elementToFile) {
    Map<File, InputStream> fileStreams = new LinkedHashMap<>();
    for (T element : elements) {
      File file = elementToFile.transform(element);
      try {
        fileStreams.put(file, new FileInputStream(file));
      } catch (FileNotFoundException e) {
        if (log == null) {
          throw new RuntimeException(e);
        }
        log.error(format("Error opening file [ %s ]", file), e);
      }
    }
    return fileStreams;
  }

  @FunctionalInterface
  public interface TransformToFile<T> {

    File transform(T element);
  }
}

