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
	
	private List<Map<String,Object>> mockEntitySet;

	@Before
	public void setUp() throws Exception {
		mockEntitySet = mockEntitySet();
	}
	
	private List<Map<String,Object>> mockEntitySet() {
		List<Map<String,Object>> newEntitySet = new ArrayList<Map<String, Object>>();
		Map<String, Object> entityDefinition;

		entityDefinition = new HashMap<String, Object>();
		entityDefinition.put("name", "MyEntity");
		entityDefinition.put("remoteName", "RemoteEntity");
		newEntitySet.add(entityDefinition);
		
		List<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
		entityDefinition.put("properties", properties);
		
		Map<String, Object> property;
		property = new HashMap<String, Object>();
		properties.add(property);
		property.put("name", "MyField");
		property.put("sample", "22");
		property.put("type", "Edm.Decimal");
		property.put("nullable", "false");
		property.put("key", "true");
		property.put("description", "This is my field");
		property.put("precision", "2");
		property.put("scale", "2");
		
		return newEntitySet;
	}

	@Test
	public void testPositive() throws JSONException, FileNotFoundException, IOException, ProcessingException, EntityModelParsingException {
		List<Map<String, Object>> entities = new EntityModelParser().getEntities("model/valid.json");
		Assert.assertEquals(mockEntitySet.get(0).get("name"), entities.get(0).get("name"));
		Assert.assertEquals(mockEntitySet.get(0).get("remoteName"), entities.get(0).get("remoteName"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "name"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "sample"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "type"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "nullable"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "key"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "description"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "precision"));
		Assert.assertTrue(equalProp(mockEntitySet, entities, "scale"));
	}
	
	private boolean equalProp(List<Map<String,Object>> entityA, List<Map<String,Object>> entityB, String prop) {
		String propA = (String) ((List<Map<String, Object>>)entityA.get(0).get("properties")).get(0).get(prop);
		String propB = (String) ((List<Map<String, Object>>)entityA.get(0).get("properties")).get(0).get(prop);
		return propA.equals(propB);
	}
	
	@Test
	public void schemaMissmatchType() throws JSONException, FileNotFoundException, IOException, ProcessingException, EntityModelParsingException{
		thrown.expect(EntityModelParsingException.class);
    List<Map<String, Object>> entities = new EntityModelParser().getEntities("model/json-schema-missmatch-type.json");
	}
	
	@Test
	public void schemaMissmatchName() throws JSONException, FileNotFoundException, IOException, ProcessingException, EntityModelParsingException{
		thrown.expect(EntityModelParsingException.class);
    List<Map<String, Object>> entities = new EntityModelParser().getEntities("model/json-schema-missmatch-name.json");
	}
	
	@Test
	public void schemaMissmatchEntity() throws JSONException, FileNotFoundException, IOException, ProcessingException, EntityModelParsingException{
		thrown.expect(EntityModelParsingException.class);
    thrown.expectMessage("object has missing required properties ([\"properties\",\"remoteName\"])");
    List<Map<String, Object>> entities = new EntityModelParser().getEntities("model/json-schema-missmatch-entity.json");
	}
	
	@Test
	public void invalidJson() throws JSONException, FileNotFoundException, IOException, ProcessingException, EntityModelParsingException{
		thrown.expect(EntityModelParsingException.class);
		thrown.expectMessage("object has missing required properties ([\"entities\"])");
    List<Map<String, Object>> entities = new EntityModelParser().getEntities("model/invalid.json");
	}
	

}
