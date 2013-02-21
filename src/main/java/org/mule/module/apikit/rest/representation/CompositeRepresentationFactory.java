/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.representation;

import org.mule.module.apikit.rest.RestRequest;

import java.util.Collection;

public class CompositeRepresentationFactory implements RepresentationFactory
{

    protected Collection<RepresentationFactory> representationFactories;

    @Override
    public Object createRepresentation(RestRequest request)
    {
        return null;
    }

    public Collection<RepresentationFactory> getRepresentationFactories()
    {
        return representationFactories;
    }

    public void setRepresentationFactories(Collection<RepresentationFactory> representationFactories)
    {
        this.representationFactories = representationFactories;
    }

}
