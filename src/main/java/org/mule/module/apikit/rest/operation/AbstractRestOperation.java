/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.operation;

import static org.mule.module.apikit.rest.representation.RepresentationMetaData.MULE_RESPONSE_MEDIATYPE_PROPERTY;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.module.apikit.AbstractWebServiceOperation;
import org.mule.module.apikit.rest.MediaTypeNotAcceptableException;
import org.mule.module.apikit.rest.OperationHandlerException;
import org.mule.module.apikit.rest.RestException;
import org.mule.module.apikit.rest.RestRequest;
import org.mule.module.apikit.rest.UnsupportedMediaTypeException;
import org.mule.module.apikit.rest.param.ParameterList;
import org.mule.module.apikit.rest.param.RestInvalidQueryParameterException;
import org.mule.module.apikit.rest.param.RestMissingQueryParameterException;
import org.mule.module.apikit.rest.param.RestParameter;
import org.mule.module.apikit.rest.protocol.UserDefinedStatusCodeException;
import org.mule.module.apikit.rest.representation.RepresentationMetaData;
import org.mule.module.apikit.rest.resource.RestResource;
import org.mule.module.apikit.rest.util.RestContentTypeParser;
import org.mule.module.apikit.rest.validation.InvalidInputException;
import org.mule.module.apikit.rest.validation.InvalidSchemaTypeException;
import org.mule.transport.NullPayload;
import org.mule.util.StringUtils;

import com.google.common.net.MediaType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.codehaus.jackson.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRestOperation extends AbstractWebServiceOperation implements RestOperation
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected RestOperationType type;
    protected RestResource resource;
    protected Collection<RepresentationMetaData> representations = new HashSet<RepresentationMetaData>();
    protected ParameterList parameters;

    @Override
    public RestOperationType getType()
    {
        return type;
    }

    public void setRepresentations(Collection<RepresentationMetaData> representations)
    {
        this.representations = representations;
    }

    public void setParameters(ParameterList parameters)
    {
        this.parameters = parameters;
    }

    @Override
    public ParameterList getParameters()
    {
        return parameters;
    }

    @Override
    public void handle(RestRequest request) throws RestException
    {
        RepresentationMetaData responseRepresentation = null;
        if (!getAllRepresentations().isEmpty())
        {
            if (getType().isRequestExpected())
            {
                validateSupportedRequestMediaType(request);
            }
            if (getType().isResponseExpected())
            {
                responseRepresentation = validateAcceptableResponeMediaType(request);
                String mediaType = responseRepresentation.getMediaType().withoutParameters().toString();
                request.getMuleEvent()
                    .getMessage()
                    .setOutboundProperty(MULE_RESPONSE_MEDIATYPE_PROPERTY, mediaType);
            }
        }
        processParameters(request);
        try
        {
            MuleEvent responseEvent = getHandler().process(request.getMuleEvent());

            if (responseEvent != null && responseEvent.getMessage() != null)
            {
                if (responseEvent.getMessage().getExceptionPayload() != null)
                {
                    throw new OperationHandlerException(responseEvent.getMessage()
                        .getExceptionPayload()
                        .getException());
                }
                else
                {
                    request.getMuleEvent().setMessage(responseEvent.getMessage());
                    for (String name : responseEvent.getFlowVariableNames())
                    {
                        request.getMuleEvent().setFlowVariable(name, responseEvent.getFlowVariable(name));
                    }
                    if (request.getProtocolAdaptor().isCustomStatusCodeSet(request))
                    {
                        throw new UserDefinedStatusCodeException();
                    }
                }
            }
            else
            {
                request.getMuleEvent().setMessage(
                    new DefaultMuleMessage(NullPayload.getInstance(), request.getService().getMuleContext()));
            }
            if (responseRepresentation != null)
            {
                Object payload = responseRepresentation.toRepresentation(request.getMuleEvent(), request);
                request.getMuleEvent().getMessage().setPayload(payload);
            }
        }
        catch (UserDefinedStatusCodeException ue)
        {
            throw ue;
        }
        catch (Exception e)
        {
            throw new OperationHandlerException(e);
        }
    }

    protected void validateSupportedRequestMediaType(RestRequest request)
        throws UnsupportedMediaTypeException, InvalidSchemaTypeException, InvalidInputException
    {
        MediaType requestMediaType = request.getProtocolAdaptor().getRequestMediaType();
        if (requestMediaType == null)
        {
            // if request Content-Type is not sent, skip validation
            return;
        }

        boolean found = false;
        for (RepresentationMetaData representation : getAllRepresentations())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(String.format("comparing media type %s with %s\n",
                    representation.getMediaType(), requestMediaType));
            }
            if (representation.getMediaType()
                .withoutParameters()
                .is(request.getProtocolAdaptor().getRequestMediaType().withoutParameters()))
            {
                found = true;
                representation.validate(request);
                break;
            }
        }
        if (!found)
        {
            throw new UnsupportedMediaTypeException();
        }
    }

    protected RepresentationMetaData validateAcceptableResponeMediaType(RestRequest request)
        throws MediaTypeNotAcceptableException
    {
        MediaType bestMatch = RestContentTypeParser.bestMatch(getAllRepresentations(),
            request.getProtocolAdaptor().getAcceptableResponseMediaTypes());
        if (bestMatch == null)
        {
            throw new MediaTypeNotAcceptableException();
        }
        for (RepresentationMetaData representation : getAllRepresentations())
        {
            if (representation.getMediaType().equals(bestMatch))
            {
                return representation;
            }
        }
        throw new MediaTypeNotAcceptableException();
    }

    private void processParameters(RestRequest request)
        throws RestMissingQueryParameterException, RestInvalidQueryParameterException
    {
        if (parameters == null)
        {
            return;
        }

        for (RestParameter parameter : parameters)
        {
            populateQueryDefaultValue(parameter, request);
            validateRequiredQueryParameter(parameter, request);
            validateQueryAllowableValues(parameter, request);
        }
    }

    private void populateQueryDefaultValue(RestParameter parameter, RestRequest request)
    {
        if (!request.getProtocolAdaptor().getQueryParameters().containsKey(parameter.getName())
            && parameter.getDefaultValue() != null)
        {
            request.getProtocolAdaptor()
                .getQueryParameters()
                .put(parameter.getName(), parameter.getDefaultValue());
        }
    }

    private void validateRequiredQueryParameter(RestParameter parameter, RestRequest request)
        throws RestMissingQueryParameterException
    {
        if (parameter.isRequired()
            && !request.getProtocolAdaptor().getQueryParameters().containsKey(parameter.getName()))
        {
            throw new RestMissingQueryParameterException("Query parameter " + parameter.getName()
                                                         + " is missing");
        }
    }

    private void validateQueryAllowableValues(RestParameter parameter, RestRequest request)
        throws RestInvalidQueryParameterException
    {
        if (parameter.getAllowableValues() != null
            && request.getProtocolAdaptor().getQueryParameters().containsKey(parameter.getName()))
        {
            if (!parameter.getAllowableValues().contains(
                request.getProtocolAdaptor().getQueryParameters().get(parameter.getName())))
            {
                throw new RestInvalidQueryParameterException(
                    "Query parameter " + parameter.getName()
                                    + " does not have a value listed as an allowable value");
            }
        }
    }

    @Override
    public Collection<RepresentationMetaData> getRepresentations()
    {
        return representations;
    }

    public void setResource(RestResource resource)
    {
        this.resource = resource;
    }

    public Collection<RepresentationMetaData> getAllRepresentations()
    {
        Collection<RepresentationMetaData> allRepresentations = new ArrayList<RepresentationMetaData>();
        allRepresentations.addAll(resource.getRepresentations());
        allRepresentations.addAll(getRepresentations());
        return allRepresentations;
    }

    @Override
    public void appendSwaggerDescriptor(JsonGenerator jsonGenerator) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDescription()
    {
        if (!StringUtils.isEmpty(super.getDescription()))
        {
            return super.getDescription();
        }
        else
        {
            return type.name() + " " + resource.getName();
        }
    }

}
