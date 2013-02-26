/**
 * Mule Rest Module
 *
 * Copyright 2011-2012 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package org.mule.module.apikit.rest.integration;

import org.mule.api.annotations.ContainsTransformerMethods;
import org.mule.api.annotations.Transformer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

@ContainsTransformerMethods
public class LeagueTransformers {
    @Transformer(resultMimeType = "application/json")
    public String toJson(League league) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(league);
    }

    @Transformer(sourceMimeType = "application/json")
    public League fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, League.class);
    }

    @Transformer(resultMimeType = "text/xml")
    public String toXml(League league) throws IOException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(League.class);

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        m.marshal(league, boas);

        return new String(boas.toByteArray());
    }


}
