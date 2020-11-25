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
public class Teams
{

    private List<Team> teams;

    @XmlElement(name = "teams")
    public List<Team> getTeams()
    {
        return teams;
    }

    public void setTeams(List<Team> teams)
    {
        this.teams = teams;
    }

    public Team getTeam(String id)
    {
        for (Team team : teams)
        {
            if (team.getId().equals(id))
            {
                return team;
            }
        }
        return null;
    }

    @Transformer(resultMimeType = "application/json")
    public String toJson(Teams teams) throws IOException
    {
        ObjectMapper mapper = JsonMapper.builder().deactivateDefaultTyping().build();
        return mapper.writeValueAsString(teams);
    }

    @Transformer(resultMimeType = "text/xml")
    public String toXml(Teams teams) throws IOException, JAXBException
    {
        JAXBContext context = JAXBContext.newInstance(getClass());

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        m.marshal(teams, boas);

        return new String(boas.toByteArray());
    }
}
