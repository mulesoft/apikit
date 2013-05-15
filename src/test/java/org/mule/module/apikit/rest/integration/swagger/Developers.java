/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.integration.swagger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

@JsonAutoDetect
public class Developers
{

    private List<Developer> developers;

    public List<Developer> getDevelopers()
    {
        return developers;
    }

    public void setDevelopers(List<Developer> developers)
    {
        this.developers = developers;
    }
}
