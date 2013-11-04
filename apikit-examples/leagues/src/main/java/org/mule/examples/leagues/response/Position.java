/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.examples.leagues.response;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@JsonAutoDetect
@XmlRootElement(namespace = "http://mulesoft.com/schemas/soccer")
public class Position {

    private int position;
    private String team;
    private int points;
    private int matchesPlayed;
    private int matchesWon;
    private int matchesDraw;
    private int matchesLost;
    private int goalsInFavor;
    private int goalsAgainst;

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(int matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public int getMatchesWon() {
        return matchesWon;
    }

    public void setMatchesWon(int matchesWon) {
        this.matchesWon = matchesWon;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public int getMatchesDraw() {
        return matchesDraw;
    }

    public void setMatchesDraw(int matchesDraw) {
        this.matchesDraw = matchesDraw;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public int getMatchesLost() {
        return matchesLost;
    }

    public void setMatchesLost(int matchesLost) {
        this.matchesLost = matchesLost;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public int getGoalsInFavor() {
        return goalsInFavor;
    }

    public void setGoalsInFavor(int goalsInFavor) {
        this.goalsInFavor = goalsInFavor;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    public void setGoalsAgainst(int goalsAgainst) {
        this.goalsAgainst = goalsAgainst;
    }
}
