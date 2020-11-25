/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.leagues;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.mule.api.annotations.ContainsTransformerMethods;
import org.mule.api.annotations.Transformer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@ContainsTransformerMethods
@XmlRootElement
@JsonAutoDetect
public class Leagues
{

    private List<League> leagues;

    @XmlElement(name = "league")
    public List<League> getLeagues()
    {
        return leagues;
    }

    public void setLeagues(List<League> leagues)
    {
        this.leagues = leagues;
    }

    public League getLeague(String id)
    {
        for (League league : leagues)
        {
            if (league.getId().equals(id))
            {
                return league;
            }
        }
        return null;
    }

    public boolean deleteLeague(String id)
    {
        return leagues.remove(new League(id));
    }

    @Transformer(resultMimeType = "application/json")
    public String toJson(Leagues leagues) throws IOException
    {
        ObjectMapper mapper = JsonMapper.builder().deactivateDefaultTyping().build();
        return mapper.writeValueAsString(leagues);
    }

    @Transformer(resultMimeType = "text/xml")
    public String toXml(Leagues leagues) throws IOException, JAXBException
    {
        JAXBContext context = JAXBContext.newInstance(getClass());

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        m.marshal(leagues, boas);

        return new String(boas.toByteArray());
    }
}
