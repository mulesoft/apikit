/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1.model;

import org.mule.raml.interfaces.model.ISecurityReference;
import org.raml.model.SecurityReference;

public class SecurityReferenceImpl implements ISecurityReference
{
    SecurityReference securityReference;

    public SecurityReferenceImpl(SecurityReference securityReference)
    {
        this.securityReference = securityReference;
    }

    public SecurityReferenceImpl(String name)
    {
        securityReference = new SecurityReference(name);
    }

    public String getName()
    {
        return securityReference.getName();
    }

    public Object getInstance()
    {
        return securityReference;
    }
}
