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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.mule.tools.apikit.model.manager.CustomRamlGenerator;
import org.mule.tools.apikit.model.manager.exception.EntityModelParsingException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import freemarker.template.TemplateException;


/**
 * 
 * @author arielsegura
 */
public class CustomRamlGeneratorTest {

	@Test
	public void testSingleKey() throws FileNotFoundException, JSONException, IOException, TemplateException, ProcessingException, EntityModelParsingException {
		Assert.assertEquals(readFromFile("model/custom.raml"), new CustomRamlGenerator().generate(("model/valid.json")));
	}
	
	@Test
	public void testDoubleKey() throws FileNotFoundException, JSONException, IOException, TemplateException, ProcessingException, EntityModelParsingException {
		Assert.assertEquals(readFromFile("model/custom-double-key.raml"), new CustomRamlGenerator().generate(("model/valid-double-key.json")));
	}
	
	public static String readFromFile(String filePath) throws FileNotFoundException, IOException {
		URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
		File file = new File(url.getPath());
		InputStream is = new FileInputStream(file);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		is.close();
		return writer.toString();
	}

}
