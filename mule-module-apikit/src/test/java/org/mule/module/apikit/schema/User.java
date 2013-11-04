/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = "http://mulesoft.org/schemas/sample")
@XmlType(namespace = "http://mulesoft.org/schemas/sample", propOrder = {"emailAddresses"})
@JsonAutoDetect
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class User implements Serializable
{

    private String username;
    private String firstName;
    private String lastName;
    private List<String> emailAddresses;

    @XmlAttribute(required = true)
    @JsonProperty(required = true)
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @XmlAttribute(required = true)
    @JsonProperty(required = true)
    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    @XmlAttribute(required = true)
    @JsonProperty(required = true)
    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    @XmlElementWrapper(required = true, name = "email-addresses", namespace = "http://mulesoft.org/schemas/sample")
    @XmlElement(required = true, name = "email-address", namespace = "http://mulesoft.org/schemas/sample", type = String.class)
    @JsonProperty(required = true)
    public List<String> getEmailAddresses()
    {
        return emailAddresses;
    }

    public void setEmailAddresses(List<String> emailAddresses)
    {
        this.emailAddresses = emailAddresses;
    }

}
