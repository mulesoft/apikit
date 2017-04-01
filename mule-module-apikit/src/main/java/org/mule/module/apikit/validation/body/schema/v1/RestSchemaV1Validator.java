/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.body.schema.v1;

import org.mule.module.apikit.ApikitRegistry;
import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.MessageHelper;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.module.apikit.validation.body.schema.IRestSchemaValidator;
import org.mule.module.apikit.validation.body.schema.v1.cache.SchemaCacheUtils;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.runtime.api.message.Message;

import com.github.fge.jsonschema.main.JsonSchema;
import com.google.common.cache.LoadingCache;

import javax.xml.validation.Schema;

public class RestSchemaV1Validator implements IRestSchemaValidator
{
    LoadingCache<String, JsonSchema> jsonSchemaCache;
    LoadingCache<String, Schema> xmlSchemaCache;
    IAction action;

    public RestSchemaV1Validator (LoadingCache<String, JsonSchema> jsonSchemaCache, LoadingCache<String, Schema> xmlSchemaCache, IAction action)
    {
        this.jsonSchemaCache = jsonSchemaCache;
        this.xmlSchemaCache = xmlSchemaCache;
        this.action = action;
    }

    public Message validate(Message message) throws BadRequestException
    {
        String requestMimeType = MessageHelper.getMediaType(message);
        if (requestMimeType.contains("json"))
        {
            RestJsonSchemaValidator validatorV1 = new RestJsonSchemaValidator(jsonSchemaCache);
            return validatorV1.validate(SchemaCacheUtils.getSchemaCacheKey(action, requestMimeType), message);

        }
        else
        {
            RestXmlSchemaValidator validatorV1 = new RestXmlSchemaValidator(xmlSchemaCache);
            return validatorV1.validate(SchemaCacheUtils.getSchemaCacheKey(action, requestMimeType), message);
        }
    }
}
