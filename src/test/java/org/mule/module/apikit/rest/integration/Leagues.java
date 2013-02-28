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
        ObjectMapper mapper = new ObjectMapper();
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
