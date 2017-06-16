/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.examples.leagues.response;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonAutoDetect
@XmlRootElement(namespace = "http://mulesoft.com/schemas/soccer")
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Match {

    private String homeTeam;
    private String awayTeam;

    @JsonSerialize(using = org.mule.examples.leagues.serializer.JsonDateSerializer.class, include=JsonSerialize.Inclusion.NON_NULL)
    private Date date;
    private Integer homeTeamScore;
    private Integer awayTeamScore;

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public String getHomeTeam()
    {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam)
    {
        this.homeTeam = homeTeam;
    }

    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public String getAwayTeam()
    {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam)
    {
        this.awayTeam = awayTeam;
    }

    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public Integer getHomeTeamScore()
    {
        return homeTeamScore;
    }

    public void setHomeTeamScore(Integer homeTeamScore)
    {
        this.homeTeamScore = homeTeamScore;
    }

    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public Integer getAwayTeamScore()
    {
        return awayTeamScore;
    }

    public void setAwayTeamScore(Integer awayTeamScore)
    {
        this.awayTeamScore = awayTeamScore;
    }

}
