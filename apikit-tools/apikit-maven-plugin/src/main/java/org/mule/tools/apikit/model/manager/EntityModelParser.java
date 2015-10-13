/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.tools.apikit.model.manager.exception.EntityModelParsingException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * 
 * @author arielsegura
 */
public class EntityModelParser {

    private static final String SAMPLE_PROPERTY_TEXT = "sample";
    private static final String TYPE_PROPERTY_TEXT = "type";
    private static final String NULLABLE_PROPERTY_TEXT = "nullable";
    private static final String LENGTH_PROPERTY_TEXT = "length";
    private static final String KEY_PROPERTY_TEXT = "key";

    private static final String DEFAULT_JSON_SCHEMA = "model-schema.json";
    
    public EntityModelParser() {

    }
    
    protected ProcessingReport validateJson(JSONObject obj)
	    throws JsonProcessingException, IOException, ProcessingException {
	// Validate json data against json schema
	ObjectMapper m = new ObjectMapper();
	JsonNode fstabSchema = m.readTree(getClass().getClassLoader().getResource(DEFAULT_JSON_SCHEMA));

	JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
	JsonSchema schema = factory.getJsonSchema(fstabSchema);
	JsonNode good = JsonLoader.fromString(obj.toString());

	return schema.validate(good);
    }

    public List<Map<String, Object>> getEntities(JSONObject obj)
	    throws IOException, ProcessingException,
	    EntityModelParsingException {

	ProcessingReport report;
	report = validateJson(obj);

	if (!report.isSuccess()) {
	    String msg = "";
	    Iterator<ProcessingMessage> iterator = report.iterator();
	    while (iterator.hasNext()) {
		ProcessingMessage message = iterator.next();
		msg += message.getMessage();
	    }
	    throw new EntityModelParsingException(msg);
	}

	List<Map<String, Object>> entitySet = new ArrayList<Map<String, Object>>();

	JSONArray schemas = obj.getJSONArray("entities");
	for (int i = 0; i < schemas.length(); i++) {	
	    JSONObject entityJson = (JSONObject) ((JSONObject) schemas.get(i)).get("entity");
	    String entityName = entityJson.getString("name");
	    String remoteName = entityJson.getString("remoteName");

	    Map<String, Object> entity = new HashMap<String, Object>();
	    entity.put("name", entityName);
	    entity.put("remoteName", remoteName);
	    entity.put("json", generateJsonSchema(entityJson));
	    Map<String, Object> parsedProperties = parseEntityProperties(entityJson.getJSONArray("properties"));
	    entity.put("properties", parsedProperties.get("properties"));
	    entity.put("keys", parsedProperties.get("keys"));
	    entitySet.add(entity);

	}

	return entitySet;
    }

    private JSONObject generateJsonSchema(JSONObject entityJson) {
	JSONObject jsonSchema = new JSONObject();

	jsonSchema.put("properties", generateJsonSchemaProperties(entityJson.getJSONArray("properties")));
	jsonSchema.put("remoteName", entityJson.getString("remoteName"));
	jsonSchema.put("$schema", "http://json-schema.org/draft-04/schema#");
	jsonSchema.put("additionalProperties", false);

	return jsonSchema;
    }

    private JSONObject generateJsonSchemaProperties(JSONArray jsonArray) {
	JSONObject jsonProperties = new JSONObject();

	for (int i = 0; i < jsonArray.length(); i++) {
	    JSONObject jsonProperty = jsonArray.getJSONObject(i).getJSONObject("field");
	    JSONObject jsonStructure = new JSONObject();

	    jsonStructure.put("type", jsonProperty.get(TYPE_PROPERTY_TEXT));
	    jsonStructure.put("sample", jsonProperty.get(SAMPLE_PROPERTY_TEXT));
	    jsonStructure.put("maxLength", jsonProperty.get(LENGTH_PROPERTY_TEXT));
	    jsonStructure.put("nullable", jsonProperty.get(NULLABLE_PROPERTY_TEXT));
	    jsonStructure.put("key", jsonProperty.get(KEY_PROPERTY_TEXT));

	    jsonProperties.put(jsonProperty.getString("name"), jsonStructure);
	}

	return jsonProperties;
    }

    /**
     * This method returns a map with two keys: 'Properties' and 'Keys'.
     * Properties is instance of List<Map<String, Object>>
     * Keys is instance of List<String>
     * @param properties
     * @return
     */
    private Map<String, Object> parseEntityProperties(JSONArray properties) {
    Map<String, Object> ret = new HashMap<String, Object>();
	List<Map<String, Object>> entityProperties = new ArrayList<Map<String, Object>>();
	
	List<String> keys = new ArrayList<String>();
	ret.put("properties", entityProperties);
	ret.put("keys", keys);
	
	if (properties != null) {
	    for (int j = 0; j < properties.length(); j++) {
		JSONObject propertyJson = properties.getJSONObject(j)
			.getJSONObject("field");

		String propertyName = propertyJson.getString("name");

		String sample = String.valueOf(propertyJson.get(SAMPLE_PROPERTY_TEXT));
		checkFieldNotNull("Sample", sample);
		String type = String.valueOf(propertyJson.get(TYPE_PROPERTY_TEXT));
		checkFieldNotNull("Type", type);
		Boolean nullable = Boolean.valueOf(String.valueOf(propertyJson.get(NULLABLE_PROPERTY_TEXT)));
		checkFieldNotNull("Nullable", nullable);
		Integer length = Integer.valueOf(String.valueOf(propertyJson.get(LENGTH_PROPERTY_TEXT)));
		checkFieldNotNull("Length", length);
		Boolean key = false;
		if (propertyJson.has(KEY_PROPERTY_TEXT)) {
		    key = Boolean.valueOf(String.valueOf(propertyJson.get(KEY_PROPERTY_TEXT)));
		    checkFieldNotNull("Key", key);
		    if(key) keys.add(propertyName);
		}
		Map<String, Object> property = new HashMap<String, Object>();
		property.put("name", propertyName);
		property.put(SAMPLE_PROPERTY_TEXT, sample);
		property.put(TYPE_PROPERTY_TEXT, type);
		property.put(NULLABLE_PROPERTY_TEXT, nullable);
		property.put(LENGTH_PROPERTY_TEXT, length);
		property.put(KEY_PROPERTY_TEXT, key);
		entityProperties.add(property);
	    }
	}
	return ret;
    }

    private void checkFieldNotNull(String expected, Object actual)
	    throws NullPointerException {
	if (actual == null) {
	    throw new NullPointerException(expected + " not found.");
	}
    }

}