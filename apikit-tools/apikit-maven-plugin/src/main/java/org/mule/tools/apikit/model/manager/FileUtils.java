/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.io.IOUtils;

/**
 * 
 * @author arielsegura
 */
public class FileUtils {

    // This methods probably should be in another mulesoft labs repo
    public static String readFromFile(String filePath)
	    throws FileNotFoundException, IOException {
	URL url = Thread.currentThread().getContextClassLoader()
		.getResource(filePath);
	File file = new File(url.getPath());
	InputStream is = new FileInputStream(file);
	StringWriter writer = new StringWriter();
	IOUtils.copy(is, writer);
	is.close();
	return writer.toString();
    }

    public static String readFromFile(InputStream input) throws IOException {
	StringWriter writer = new StringWriter();
	IOUtils.copy(input, writer);
	input.close();
	return writer.toString();
    }

    public static File stringToFile(String path, String body) throws IOException {
	File file = new File(path);

	try (FileOutputStream fop = new FileOutputStream(file)) {

	    // if file doesn't exists, then create it
	    if (!file.exists()) {
		file.createNewFile();
	    }

	    // get the content in bytes
	    byte[] contentInBytes = body.getBytes();

	    fop.write(contentInBytes);
	    fop.flush();
	    fop.close();

	} catch (IOException e) {
	    throw e;
	}
	
	return file;
    }
}
