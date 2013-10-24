package org.mule.examples.leagues.response;

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
        ObjectMapper mapper = new ObjectMapper();
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
