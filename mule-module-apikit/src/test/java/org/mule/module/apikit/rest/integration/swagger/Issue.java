/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.integration.swagger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.Date;

@JsonAutoDetect
public class Issue
{

    private String id;
    private String name;
    private String description;
    private float cvp;
    private double storyPoints;
    private boolean resolved;
    private long workHours;
    private int fixBuildNumber;
    private byte category;
    private Date creationDate;
    private Developer assignee;

    public Issue()
    {
    }

    public Issue(String id)
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

        Issue issue = (Issue) o;

        if (id != null ? !id.equals(issue.id) : issue.id != null)
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Developer getAssignee()
    {
        return assignee;
    }

    public void setAssignee(Developer assignee)
    {
        this.assignee = assignee;
    }

    public float getCvp()
    {
        return cvp;
    }

    public void setCvp(float cvp)
    {
        this.cvp = cvp;
    }

    public double getStoryPoints()
    {
        return storyPoints;
    }

    public void setStoryPoints(double storyPoints)
    {
        this.storyPoints = storyPoints;
    }

    public boolean isResolved()
    {
        return resolved;
    }

    public void setResolved(boolean resolved)
    {
        this.resolved = resolved;
    }

    public long getWorkHours()
    {
        return workHours;
    }

    public void setWorkHours(long workHours)
    {
        this.workHours = workHours;
    }

    public int getFixBuildNumber()
    {
        return fixBuildNumber;
    }

    public void setFixBuildNumber(int fixBuildNumber)
    {
        this.fixBuildNumber = fixBuildNumber;
    }

    public byte getCategory()
    {
        return category;
    }

    public void setCategory(byte category)
    {
        this.category = category;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }
}
