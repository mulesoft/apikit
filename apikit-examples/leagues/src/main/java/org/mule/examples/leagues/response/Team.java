/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.examples.leagues.response;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@JsonAutoDetect
@XmlRootElement(namespace = "http://mulesoft.com/schemas/soccer")
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Team {

    private String id;
    private String name;
    private String homeCity;
    private String stadium;
    private Integer matches;

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    @XmlElement(required = true, namespace = "http://mulesoft.com/schemas/soccer")
    public String getHomeCity() {
        return homeCity;
    }

    public void setHomeCity(String homeCity) {
        this.homeCity = homeCity;
    }

    @JsonProperty
    @XmlElement(required = false, namespace = "http://mulesoft.com/schemas/soccer")
    public String getStadium() {
        return stadium;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    @JsonProperty
    @XmlElement(required = false, namespace = "http://mulesoft.com/schemas/soccer")
    public Integer getMatches() {
        return matches;
    }

    public void setMatches(int matches) {
        this.matches = matches;
    }
}