/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest;

import org.mule.module.apikit.AbstractWebServiceInterface;
import org.mule.module.apikit.UnauthorizedException;
import org.mule.module.apikit.api.WebServiceRoute;
import org.mule.module.apikit.rest.swagger.json.RestWebServiceInterfaceSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonSerialize(using = RestWebServiceInterfaceSerializer.class)
public class RestWebServiceInterface extends AbstractWebServiceInterface
{
    public RestWebServiceInterface(String name)
    {
        super(name);
    }

    public void setResources(List<WebServiceRoute> resources)
    {
        setRoutes(resources);
    }

}
