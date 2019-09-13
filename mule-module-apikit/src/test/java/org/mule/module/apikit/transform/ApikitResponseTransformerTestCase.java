/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.transform;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.registry.MuleRegistry;
import org.mule.api.registry.RegistrationException;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.CoreMessages;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.raml.interfaces.model.IMimeType;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.transformer.types.SimpleDataType;
import org.mule.transport.NullPayload;

import com.google.common.cache.LoadingCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class ApikitResponseTransformerTestCase
{

    private ApikitResponseTransformer responseTransformer = new ApikitResponseTransformer();

    @Mock
    private MuleMessage message;
    @Mock
    private MuleContext muleContext;
    @Mock
    private MuleRegistry muleRegistry;
    @Mock
    private LoadingCache<DataTypePair, Transformer> transformerCache;
    @Mock
    private Transformer transformer;
    @Mock
    private IMimeType mimeType;

    @Before
    public void setUp() throws ExecutionException, RegistrationException
    {
        initMocks(this);
        responseTransformer.setMuleContext(muleContext);
        when(muleContext.getRegistry()).thenReturn(muleRegistry);
        when(muleContext.getRegistry().get("__restRouterTransformerCache")).thenReturn(transformerCache);
    }

    @Test
    public void jsonRAMLJsonAccept() throws TransformerException
    {
        when(message.getPayload()).thenReturn("{application/json;charset=UTF-8}");
        when(message.getDataType()).thenReturn(new SimpleDataType(String.class, "application/json"));
        when(message.getEncoding()).thenReturn("UTF-8");
        List<String> responseMimeType = new ArrayList<>();
        String mimeType = "application/json";
        responseMimeType.add(mimeType);
        Object responsePayload = responseTransformer.transformToExpectedContentType(message, "application/json", responseMimeType, "application/json");

        assertNotNull(responsePayload);
        assertThat(responsePayload, instanceOf(String.class));
        assertEquals("{application/json;charset=UTF-8}", responsePayload);
        Mockito.verifyZeroInteractions(transformerCache);
        Mockito.verifyZeroInteractions(transformer);
    }

    @Test
    public void jsonRAMLJsonAcceptContentXml() throws TransformerException
    {
        when(message.getPayload()).thenReturn("{application/json;charset=UTF-8}");
        when(message.getDataType()).thenReturn(new SimpleDataType(String.class, "application/json"));
        when(message.getEncoding()).thenReturn("UTF-8");
        when(message.getOutboundProperty("Content-Type")).thenReturn("application/xml");
        List<String> responseMimeType = new ArrayList<>();
        String mimeType = "application/json";
        responseMimeType.add(mimeType);
        Object responsePayload = responseTransformer.transformToExpectedContentType(message, "application/json", responseMimeType, "application/json");

        assertNotNull(responsePayload);
        assertThat(responsePayload, instanceOf(String.class));
        assertEquals("{application/json;charset=UTF-8}", responsePayload);
        Mockito.verifyZeroInteractions(transformerCache);
        Mockito.verifyZeroInteractions(transformer);
    }

    @Test
    public void jsonRAMLJsonAcceptContentJson() throws TransformerException
    {
        when(message.getPayload()).thenReturn("{application/json;charset=UTF-8}");
        when(message.getDataType()).thenReturn(new SimpleDataType(String.class, "application/json"));
        when(message.getEncoding()).thenReturn("UTF-8");
        when(message.getOutboundProperty("Content-Type")).thenReturn("application/json");
        List<String> responseMimeType = new ArrayList<>();
        String mimeType = "application/json";
        responseMimeType.add(mimeType);
        Object responsePayload = responseTransformer.transformToExpectedContentType(message, "application/json", responseMimeType, "application/json");

        assertNotNull(responsePayload);
        assertThat(responsePayload, instanceOf(String.class));
        assertEquals("{application/json;charset=UTF-8}", responsePayload);
        Mockito.verifyZeroInteractions(transformerCache);
        Mockito.verifyZeroInteractions(transformer);
    }

    @Test
    public void xmlRAMLJsonAccept() throws TransformerException, RegistrationException, ExecutionException
    {
        String payload = "{application/xml;charset=UTF-8}";
        when(message.getPayload()).thenReturn(payload);
        when(message.getDataType()).thenReturn(new SimpleDataType(String.class, "application/xml"));
        when(message.getEncoding()).thenReturn("UTF-8");
        when(TransformerCache.getTransformerCache(muleContext)).thenReturn(transformerCache);
        when(transformerCache.get(Mockito.isA(DataTypePair.class))).thenReturn(transformer);
        List<String> responseMimeType = new ArrayList<>();
        String mimeType = "application/json";
        responseMimeType.add(mimeType);
        Object responsePayload = responseTransformer.transformToExpectedContentType(message, "application/xml", responseMimeType, "application/json");

        assertNull(responsePayload);
        Mockito.verify(transformer).transform(payload);
    }

    @Test
    public void jsonRAMLXmlAccept() throws TransformerException, RegistrationException, ExecutionException
    {
        String payload = "{application/json;charset=UTF-8}";
        when(message.getPayload()).thenReturn(payload);
        when(message.getDataType()).thenReturn(new SimpleDataType(String.class, "application/xml"));
        when(message.getEncoding()).thenReturn("UTF-8");
        when(TransformerCache.getTransformerCache(muleContext)).thenReturn(transformerCache);
        when(transformerCache.get(Mockito.isA(DataTypePair.class))).thenReturn(transformer);
        List<String> responseMimeType = new ArrayList<>();
        String mimeType = "application/json";
        responseMimeType.add(mimeType);
        Object responsePayload = responseTransformer.transformToExpectedContentType(message, "application/xml", responseMimeType, "application/xml");

        assertNull(responsePayload);
        Mockito.verify(transformer).transform(payload);
    }

    @Test
    public void contentJsonMsgNull() throws TransformerException, RegistrationException, ExecutionException
    {
        String payload = "{application/json;charset=UTF-8}";
        when(message.getPayload()).thenReturn(payload);
        when(message.getDataType()).thenReturn(null);
        when(message.getEncoding()).thenReturn(null);
        when(message.getOutboundProperty("Content-Type")).thenReturn("application/json");
        when(TransformerCache.getTransformerCache(muleContext)).thenReturn(transformerCache);
        when(transformerCache.get(Mockito.isA(DataTypePair.class))).thenReturn(transformer);
        List<String> responseMimeType = new ArrayList<>();
        String mimeType = "application/json";
        responseMimeType.add(mimeType);
        Object responsePayload = responseTransformer.transformToExpectedContentType(message, "application/json", responseMimeType, "application/json");

        assertNotNull(payload);
        assertThat(payload, instanceOf(String.class));
        assertEquals("{application/json;charset=UTF-8}", responsePayload);
        Mockito.verifyZeroInteractions(transformerCache);
        Mockito.verifyZeroInteractions(transformer);
    }

    @Test(expected = ApikitRuntimeException.class)
    public void nullResponseRepresentationContentTypeNull() throws TransformerException
    {
        try
        {
            responseTransformer.transformToExpectedContentType(message, "*/*", null, "application/json");
        }
        catch (ApikitRuntimeException e)
        {
            assertEquals("Content-Type must be set in the flow when declaring */* response type", e.getMessage());
            throw e;
        }
    }

    @Test
    public void nullResponseRepresentationContentTypeNotNull() throws TransformerException
    {
        String payload = "{application/json;charset=UTF-8}";
        when(message.getPayload()).thenReturn(payload);
        when(message.getOutboundProperty("Content-Type")).thenReturn("application/xml");
        Object responsePayload = responseTransformer.transformToExpectedContentType(message, "*/*", null, "application/json");

        assertNotNull(payload);
        assertThat(payload, instanceOf(String.class));
        assertEquals("{application/json;charset=UTF-8}", responsePayload);
    }

    @Test
    public void nullPayload() throws TransformerException
    {
        when(message.getPayload()).thenReturn(NullPayload.getInstance());
        Object responsePayload = responseTransformer.transformToExpectedContentType(message, "**/*//*", null, "application/json");

        assertTrue(responsePayload instanceof NullPayload);
        Mockito.verifyZeroInteractions(transformerCache);
        Mockito.verifyZeroInteractions(transformer);
    }

    @Test
    public void transformerException() throws TransformerException, ExecutionException, RegistrationException
    {
        String payload = "{application/json;charset=UTF-8}";
        when(message.getPayload()).thenReturn(payload);
        when(message.getDataType()).thenReturn(new SimpleDataType(String.class, "application/xml"));
        when(message.getEncoding()).thenReturn("UTF-8");
        when(TransformerCache.getTransformerCache(muleContext)).thenReturn(transformerCache);
        when(transformerCache.get(Mockito.isA(DataTypePair.class))).thenReturn(transformer);
        Mockito.when(transformer.transform(message.getPayload())).thenThrow(new NullPointerException());
        List<String> responseMimeType = new ArrayList<>();
        String mimeType = "application/json";
        responseMimeType.add(mimeType);
        Object responsePayload = null;
        try
        {
            responsePayload = responseTransformer.transformToExpectedContentType(message, "application/xml", responseMimeType, "application/xml");
            assertTrue("Expected exception not thrown",false);
        }
        catch (Exception e)
        {
            assertTrue(e instanceof TransformerException);
        }
        Mockito.verify(transformer).transform(payload);
        assertNull(responsePayload);
    }

    @Test
    public void compoundAcceptOnlyJson() throws TransformerException
    {
        when(message.getPayload()).thenReturn("{application/json;charset=UTF-8}");
        when(message.getDataType()).thenReturn(new SimpleDataType(String.class, "application/json"));
        when(message.getEncoding()).thenReturn("UTF-8");
        List<String> responseMimeType = new ArrayList<>();
        String mimeType = "application/json";
        responseMimeType.add(mimeType);
        Object responsePayload = responseTransformer.transformToExpectedContentType(message, "application/json", responseMimeType, "application/json,application/xml;q=0.9,*/*;q=0.8;charset=UTF-8");

        assertNotNull(responsePayload);
        assertThat(responsePayload, instanceOf(String.class));
        assertEquals("{application/json;charset=UTF-8}", responsePayload);
        Mockito.verifyZeroInteractions(transformerCache);
        Mockito.verifyZeroInteractions(transformer);
    }

    @Test
    public void compoundAcceptOnlyXml() throws TransformerException
    {
        String payload = "{application/xml;charset=UTF-8}";
        when(message.getPayload()).thenReturn(payload);
        when(message.getDataType()).thenReturn(new SimpleDataType(String.class, "application/xml"));
        when(message.getEncoding()).thenReturn("UTF-8");
        List<String> responseMimeType = new ArrayList<>();
        String mimeType = "application/xml";
        responseMimeType.add(mimeType);
        Object responsePayload = responseTransformer.transformToExpectedContentType(message, "application/xml", responseMimeType, "application/json,application/xml;q=0.9,*/*;q=0.8;charset=UTF-8");

        assertNotNull(responsePayload);
        assertThat(responsePayload, instanceOf(String.class));
        assertEquals("{application/xml;charset=UTF-8}", responsePayload);
        Mockito.verifyZeroInteractions(transformerCache);
        Mockito.verifyZeroInteractions(transformer);
    }

    @Test
    public void preserveDataType() throws TransformerException
    {
        String payload = "{text/xml;charset=UTF-8}";
        when(message.getPayload()).thenReturn(payload);
        when(message.getDataType()).thenReturn(new SimpleDataType(String.class, "text/xml"));
        when(message.getEncoding()).thenReturn("UTF-8");
        List<String> responseMimeTypes = new ArrayList<>();
        responseMimeTypes.add("application/xml");
        responseMimeTypes.add("application/json");
        responseMimeTypes.add("text/xml");
        responseTransformer.transformToExpectedContentType(message, "application/xml", responseMimeTypes, "*/*");

        verify(message).setOutboundProperty("Content-Type", "text/xml;charset=UTF-8");
    }

    @Test
    public void compoundAcceptOnlyOther() throws TransformerException, ExecutionException
    {
        String payload = "application/json;charset=UTF-8";
        when(message.getPayload()).thenReturn(payload);
        when(message.getDataType()).thenReturn(new SimpleDataType(String.class, "application/json"));
        when(message.getEncoding()).thenReturn("UTF-8");
        List<String> responseMimeType = new ArrayList<>();
        String mimeType = "text/plain";
        responseMimeType.add(mimeType);
        DataType sourceDataType = DataTypeFactory.create(String.class, "application/json;charset=UTF-8");
        DataType resultDataType = DataTypeFactory.create(String.class, "*/*");
        when(transformerCache.get(Mockito.isA(DataTypePair.class))).thenThrow(new ExecutionException(new TransformerException(CoreMessages.noTransformerFoundForMessage(sourceDataType, resultDataType))));
        Object responsePayload = null;
        try
        {
            responsePayload = responseTransformer.transformToExpectedContentType(message, null, responseMimeType, "application/json,application/xml;q=0.9,*/*;q=0.8;charset=UTF-8");
            assertTrue("Expected exception not thrown",false);
        }
        catch (Exception e)
        {
            assertTrue(e instanceof TransformerException);
        }
        assertNull(responsePayload);
    }
}

