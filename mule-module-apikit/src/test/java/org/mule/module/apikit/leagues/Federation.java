/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.leagues;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class Federation
{

    private String name;
    private int year;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getYear()
    {
        return year;
    }

    public void setYear(int year)
    {
        this.year = year;
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

        Federation that = (Federation) o;

        if (year != that.year)
        {
            return false;
        }
        if (!name.equals(that.name))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + year;
        return result;
    }

    @Override
    public String toString()
    {
        return "Federation{" +
               "name='" + name + '\'' +
               ", year=" + year +
               '}';
    }
}
