package org.mule.module.apikit.rest.validation;

import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.registry.RegistrationException;
import org.mule.module.apikit.rest.validation.cache.JsonSchemaAndNode;
import org.mule.module.apikit.rest.validation.cache.JsonSchemaCache;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import org.eel.kitchen.jsonschema.report.ValidationReport;
import org.eel.kitchen.jsonschema.util.JsonLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestJsonSchemaValidator extends AbstractRestSchemaValidator
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public RestJsonSchemaValidator(MuleContext muleContext)
    {
        super(muleContext);
    }

    @Override
    public void validate(String schemaLocation, MuleEvent muleEvent) throws InvalidInputException
    {
        try
        {
            JsonNode data;
            Object input = muleEvent.getMessage().getPayload();
            if (input instanceof String)
            {
                data = JsonLoader.fromReader(new StringReader((String) input));
            }
            else if (input instanceof InputStream)
            {
                data = JsonLoader.fromReader(new InputStreamReader((InputStream) input));
            }
            else if (input instanceof byte[])
            {
                data = JsonLoader.fromReader(new InputStreamReader(new ByteArrayInputStream((byte[]) input)));
            }
            else
            {
                throw new InvalidInputException("Don't know how to parse " + input.getClass().getName());
            }

            JsonSchemaAndNode schema = JsonSchemaCache.getJsonSchemaCache(muleContext).get(schemaLocation);

            ValidationReport report = schema.getJsonSchema().validate(data);

            if (!report.isSuccess())
            {
                String message = report.getMessages().get(0);
                logger.info("Schema validation failed: " + message);
                throw new InvalidInputException(message);
            }
        }
        catch (ExecutionException e)
        {
            throw new InvalidInputException(e);
        }
        catch (RegistrationException e)
        {
            throw new InvalidInputException(e);
        }
        catch (IOException e)
        {
            throw new InvalidInputException(e);
        }
    }
}
