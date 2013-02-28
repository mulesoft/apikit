package org.mule.module.apikit.rest.integration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@JsonAutoDetect
@XmlRootElement(namespace = "http://mulesoft.com/schemas/soccer")
@XmlType(namespace = "http://mulesoft.com/schemas/soccer")
public class League
{

    private String id;
    private String name;

    public League()
    {
    }

    public League(String id)
    {
        this.id = id;
    }

    @JsonProperty(required = false)
    @XmlElement(required = false, namespace = "http://mulesoft.com/schemas/soccer")
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @JsonProperty(required = true)
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "League{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        League league = (League) o;

        if (id != null ? !id.equals(league.id) : league.id != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
