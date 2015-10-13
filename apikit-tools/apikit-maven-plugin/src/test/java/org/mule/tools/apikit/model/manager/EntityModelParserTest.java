/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.apikit.model.manager.EntityModelParser;
import org.mule.tools.apikit.model.manager.FileUtils;
import org.mule.tools.apikit.model.manager.exception.EntityModelParsingException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

/**
 * 
 * @author arielsegura
 */
public class EntityModelParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private List<Map<String, Object>> mockEntitySet;

    @Before
    public void setUp() throws Exception {
	mockEntitySet = mockEntitySet();
    }

    private List<Map<String, Object>> mockEntitySet() {
	List<Map<String, Object>> newEntitySet = new ArrayList<Map<String, Object>>();
	Map<String, Object> entityDefinition;

	entityDefinition = new HashMap<String, Object>();
	entityDefinition.put("name", "entityA");
	entityDefinition.put("remoteName", "entity_A");
	newEntitySet.add(entityDefinition);

	List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
	entityDefinition.put("properties", properties);

	Map<String, Object> property;
	property = new HashMap<String, Object>();
	properties.add(property);
	property.put("name", "id");
	property.put("sample", "12");
	property.put("type", "integer");
	property.put("nullable", false);
	property.put("length", 4);
	// property.put("description", "12");
	property.put("key", true);

	property = new HashMap<String, Object>();
	properties.add(property);
	property.put("name", "name");
	property.put("sample", "Pretty Entity");
	property.put("type", "string");
	property.put("nullable", true);
	property.put("length", 40);
	// property.put("description", "12");
	property.put("key", false);

	return newEntitySet;
    }

    @Test
    public void testPositive() throws JSONException, FileNotFoundException,
	    IOException, ProcessingException, EntityModelParsingException {
	JSONObject obj = new JSONObject(FileUtils.readFromFile("model/valid.json"));
	List<Map<String, Object>> entities = new EntityModelParser().getEntities(obj);
	Assert.assertEquals(mockEntitySet.get(0).get("name"), entities.get(0).get("name"));
	Assert.assertEquals(mockEntitySet.get(0).get("remoteName"), entities.get(0).get("remoteName"));
	Assert.assertEquals(mockEntitySet.get(0).get("properties"), entities.get(0).get("properties"));
    }

    @Test
    public void schemaMissmatchType() throws JSONException,
	    FileNotFoundException, IOException, ProcessingException,
	    EntityModelParsingException {
	thrown.expect(EntityModelParsingException.class);
	thrown.expectMessage("object has missing required properties ([\"type\"])");
	JSONObject obj = new JSONObject(FileUtils.readFromFile("model/json-schema-missmatch-type.json"));
	List<Map<String, Object>> entities = new EntityModelParser().getEntities(obj);
    }

    @Test
    public void schemaMissmatchName() throws JSONException,
	    FileNotFoundException, IOException, ProcessingException,
	    EntityModelParsingException {
	thrown.expect(EntityModelParsingException.class);
	thrown.expectMessage("object has missing required properties ([\"name\"])");
	JSONObject obj = new JSONObject(FileUtils.readFromFile("model/json-schema-missmatch-name.json"));
	List<Map<String, Object>> entities = new EntityModelParser().getEntities(obj);
    }

    @Test
    public void schemaMissmatchEntity() throws JSONException,
	    FileNotFoundException, IOException, ProcessingException,
	    EntityModelParsingException {
	thrown.expect(EntityModelParsingException.class);
	thrown.expectMessage("object has missing required properties ([\"properties\",\"remoteName\"])");
	JSONObject obj = new JSONObject(FileUtils.readFromFile("model/json-schema-missmatch-entity.json"));
	List<Map<String, Object>> entities = new EntityModelParser().getEntities(obj);
    }

    @Test
    public void invalidJson() throws JSONException, FileNotFoundException,
	    IOException, ProcessingException, EntityModelParsingException {
	thrown.expect(EntityModelParsingException.class);
	thrown.expectMessage("object has missing required properties ([\"entities\"])");
	JSONObject obj = new JSONObject(FileUtils.readFromFile("model/invalid.json"));
	List<Map<String, Object>> entities = new EntityModelParser().getEntities(obj);
    }

}
