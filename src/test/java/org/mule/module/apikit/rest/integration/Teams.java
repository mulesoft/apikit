package org.mule.module.apikit.rest.integration;

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
        ObjectMapper mapper = new ObjectMapper();
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
