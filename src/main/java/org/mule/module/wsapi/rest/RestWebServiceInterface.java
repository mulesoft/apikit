/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.wsapi.rest;

import org.mule.module.wsapi.AbstractWebServiceInterface;
import org.mule.module.wsapi.rest.swagger.json.RestWebServiceInterfaceSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = RestWebServiceInterfaceSerializer.class)
public class RestWebServiceInterface extends AbstractWebServiceInterface
{
    public RestWebServiceInterface(String name)
    {
        super(name);
    }

}
