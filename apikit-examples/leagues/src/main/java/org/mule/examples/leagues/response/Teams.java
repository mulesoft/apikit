package org.mule.examples.leagues.response;

import org.mule.api.annotations.Transformer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;

public class Teams implements Serializable {
    private List<Team> teams;

    @XmlElement(name = "teams")
    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    @Transformer(resultMimeType = "application/json")
    public String toJson(Teams teams) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(teams);
    }

    @Transformer(resultMimeType = "text/xml")
    public String toXml(Teams teams) throws IOException, JAXBException {
        JAXBContext context = JAXBContext.newInstance(getClass());

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        m.marshal(teams, boas);

        return new String(boas.toByteArray());
    }

}
