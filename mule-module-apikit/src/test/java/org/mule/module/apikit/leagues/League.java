/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.leagues;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@JsonAutoDetect
@XmlRootElement(namespace = "http://mulesoft.com/schemas/soccer")
@XmlType(namespace = "http://mulesoft.com/schemas/soccer")
public class League
{

    private String id;
    private String name;
    private String description;
    private List<Team> teams;
    private Federation federation;

    public League()
    {
    }

    public League(String id)
    {
        this.id = id;
    }

    @JsonProperty
    @XmlElement(required = false, namespace = "http://mulesoft.com/schemas/soccer")
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @JsonProperty
    @XmlElement(required = false, namespace = "http://mulesoft.com/schemas/soccer")
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @XmlTransient
    @JsonIgnore
    public Federation getFederation()
    {
        return federation;
    }

    public void setFederation(Federation federation)
    {
        this.federation = federation;
    }

    @Override
    public String toString()
    {
        return "League{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
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

    @XmlTransient
    @JsonIgnore
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
}
