/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.mule.tools.apikit.model.manager.exception.EntityModelParsingException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

/**
 * 
 * @author arielsegura
 */
public class CustomRamlGenerator {

    private static Configuration fmkCfg;
    private EntityModelParser entityModelParser;

    public CustomRamlGenerator() {
	entityModelParser = new EntityModelParser();
    }

    private static Configuration getConfiguration() {
	if (fmkCfg == null) {
	    fmkCfg = new Configuration();

	    // Where do we load the templates from:
	    fmkCfg.setClassForTemplateLoading(CustomRamlGenerator.class, "/");

	    // Some other recommended settings:
	    fmkCfg.setIncompatibleImprovements(new Version(2, 3, 20));
	    fmkCfg.setDefaultEncoding("UTF-8");
	    fmkCfg.setLocale(Locale.US);
	    fmkCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

	}
	return fmkCfg;
    }

    public String generate(JSONObject json) throws FileNotFoundException,
	    JSONException, IOException, TemplateException, ProcessingException,
	    EntityModelParsingException {
	return generate(entityModelParser.getEntities(json));
    }

    public String generate(String path) throws FileNotFoundException,
	    JSONException, IOException, TemplateException, ProcessingException,
	    EntityModelParsingException {
	JSONObject obj = new JSONObject(FileUtils.readFromFile(path));
	return generate(entityModelParser.getEntities(obj));
    }

    public String generate(InputStream inputStream)
	    throws FileNotFoundException, JSONException, IOException,
	    TemplateException, ProcessingException, EntityModelParsingException {
	JSONObject obj = new JSONObject(FileUtils.readFromFile(inputStream));
	return generate(entityModelParser.getEntities(obj));
    }

    public boolean isModelValid(InputStream input)
	    throws JsonProcessingException, IOException, ProcessingException {
	JSONObject obj = new JSONObject(FileUtils.readFromFile(input));
	return entityModelParser.validateJson(obj).isSuccess();
    }

    private String generate(List<Map<String, Object>> entitySet)
	    throws FileNotFoundException, IOException, TemplateException {

	Map<String, Object> raml = new HashMap<String, Object>();

	Configuration cfg = getConfiguration();

	// modify the raml object
	raml.put("title", "Auto-generated RAML");
	raml.put("version", "0.1");
	raml.put("ramlVersion", "0.8");
	raml.put("schemas", entitySet);

	List<Map<String, Object>> resources = new ArrayList<Map<String, Object>>();

	for (Map<String, Object> entity : entitySet) {
	    Map<String, Object> resource = new HashMap<String, Object>();
	    resource.put("name", entity.get("name"));
	    resource.put("displayName", entity.get("name"));
	    resource.put("key", buildKeyForResource(entity));
	    resources.add(resource);
	}
	raml.put("resources", resources);
	Template template = cfg.getTemplate("custom-raml-template.ftl");

	Writer out = new StringWriter();
	template.process(raml, out);

	return out.toString();

    }

    /**
     * 
     * @param entity
     * @return {entityId} or key1_{key1}-key2_{key2}-...-keyN_{keyN}
     */
    private String buildKeyForResource(Map<String, Object> entity) {
	List<String> keys = (List<String>) entity.get("keys");
	String ret = "";
	String delim = "";
	if (keys.size() > 1) {
	    for (int i = 0; i < keys.size(); i++) {
		String key = keys.get(i);
		ret += delim;
		ret += key + "_{" + key + "}";
		delim = "-";
	    }
	} else {
	    ret = "{" + entity.get("name") + "Id}";
	}
	return ret;
    }

}
