/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.examples.leagues.response;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.mule.api.annotations.Transformer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;

public class Fixture {
    private List<Match> fixture;

    @XmlElement(name = "fixture")
    public List<Match> getFixture() {
        return fixture;
    }

    public void setFixture(List<Match> fixture) {
        this.fixture = fixture;
    }

    @Transformer(resultMimeType = "application/json")
    public String toJson(Fixture fixture) throws IOException
    {
        ObjectMapper mapper = JsonMapper.builder().deactivateDefaultTyping().build();
        return mapper.writeValueAsString(fixture);
    }

    @Transformer(resultMimeType = "text/xml")
    public String toXml(Fixture fixture) throws IOException, JAXBException
    {
        JAXBContext context = JAXBContext.newInstance(getClass());

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        m.marshal(fixture, boas);

        return new String(boas.toByteArray());
    }

}
