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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
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

	private static final String[] FIELD_PROPERTIES = { "name", "type", "nullable", "key", "defaultValue", "maxLength", "fixedLength", "collation", "unicode", "precision" };
    private static final String DEFAULT_JSON_SCHEMA = "model-schema.json";
    
    public EntityModelParser() {

    }

    public ProcessingReport validateJson(JSONObject obj)
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
	    JSONObject entityJson = (JSONObject) ((JSONObject) schemas.get(i))
		    .get("entity");
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
	jsonSchema.put("edm.name", entityJson.getString("name"));
	jsonSchema.put("edm.remoteName", entityJson.getString("remoteName"));
	jsonSchema.put("$schema", "http://json-schema.org/draft-04/schema#");
	jsonSchema.put("type", "object");
	jsonSchema.put("required", generateJsonSchemaRequiredProperties(entityJson.getJSONArray("properties")));
	jsonSchema.put("additionalProperties", false);

	return jsonSchema;
    }
    
    private JSONArray generateJsonSchemaRequiredProperties(JSONArray properties) {
    	JSONArray required = new JSONArray();
    	
    	for (int i = 0; i < properties.length(); i++) {
    		String name = (String) properties.getJSONObject(i).getJSONObject("field").get("name");
    		required.put(name);
    	}
    	
    	return required;
    }

    private JSONObject generateJsonSchemaProperties(JSONArray jsonArray) {
	JSONObject jsonProperties = new JSONObject();

	for (int i = 0; i < jsonArray.length(); i++) {
	    JSONObject jsonProperty = jsonArray.getJSONObject(i).getJSONObject(
		    "field");
	    JSONObject jsonStructure = new JSONObject();
	    
	    
	    for (String prop: FIELD_PROPERTIES) {
	    	try {
	    		jsonStructure.put("edm." + prop, jsonProperty.get(prop));
	    	} catch (Exception e) {
	    		// ignore missing property
	    	}
	    }
	    
	    // infer json schema type from edm.type
	    String type = (String) jsonProperty.get("type");
	    
	    jsonStructure.put("type", getSchemaTypeFromEdmType(type));

	    jsonProperties.put(jsonProperty.getString("name"), jsonStructure);
	}

	return jsonProperties;
    }
    
    public static String getSchemaTypeFromEdmType (String edmType) {
    	String schemaType = "string";
	    switch (edmType) {
		    case "Edm.Boolean":
		    	schemaType = "boolean";
		    	break;
		    case "Edm.Decimal":
		    case "Edm.Double":
		    case "Edm.Single":
		    	schemaType = "number";
		    	break;	    	
		    case "Edm.Int16":
		    case "Edm.Int32":
		    case "Edm.Int64":
		    case "Edm.SByte":
		    	schemaType = "integer";
		    	break;
		    case "Edm.Guid":
		    case "Edm.Binary":
		    case "Edm.DateTime":
		    case "Edm.String":
		    case "Edm.Time":
		    case "Edm.DateTimeOffset":
		    	schemaType = "string";
		    	break;
	    }
	    return schemaType;
    }

    /**
     * This method return a map with two keys: 'Properties' and 'Keys'.
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

		Map<String, Object> property = new HashMap<String, Object>();
		
		for (String prop: FIELD_PROPERTIES) {
	    	try {
	    		property.put(prop, propertyJson.get(prop));
	    	} catch (Exception e) {
	    		// ignore missing property
	    	}
		}
		
		boolean isKey = (Boolean) propertyJson.get("key");
		if (isKey) {
			keys.add((String) propertyJson.get("name"));
		}
		
		entityProperties.add(property);
	    }
	}
	return ret;
    }
}