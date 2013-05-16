/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.integration.swagger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class Product
{

    private String id;
    private String name;
    private Team team;
    private Issues issues;

    public Product()
    {
    }

    public Product(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

        Product product = (Product) o;

        if (id != null ? !id.equals(product.id) : product.id != null)
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

    public void setTeam(Team team)
    {
        this.team = team;
    }

    public void setIssues(Issues issues)
    {
        this.issues = issues;
    }

    public Team getTeam()
    {
        return team;
    }

    public Issues getIssues()
    {
        return issues;
    }
}
