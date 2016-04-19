/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.model;

import org.mule.raml.interfaces.model.ISecurityScheme;
import org.raml.model.SecurityScheme;

public class SecuritySchemeImpl implements ISecurityScheme
{
    SecurityScheme securityScheme;
    public SecuritySchemeImpl(SecurityScheme securityScheme)
    {
        this.securityScheme = securityScheme;
    }

    public Object getInstance()
    {
        return securityScheme;
    }
}
