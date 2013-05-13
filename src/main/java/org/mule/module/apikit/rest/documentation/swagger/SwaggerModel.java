/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.rest.documentation.swagger;

import org.mule.module.apikit.rest.representation.RepresentationMetaData;
import org.mule.module.apikit.rest.util.NameUtils;

import java.util.StringTokenizer;

/**
 *
 */
public class SwaggerModel {
    private RepresentationMetaData representationMetaData;

    public SwaggerModel(RepresentationMetaData representationMetaData) {
        this.representationMetaData = representationMetaData;
    }

    public String getName()
    {
        StringTokenizer stringTokenizer = new StringTokenizer(representationMetaData.getSchemaLocation(), ".");
        String name = null;
        while (stringTokenizer.hasMoreTokens())
        {
            name = stringTokenizer.nextToken();
        }
        return NameUtils.camel(name);
    }

    public String getSchemaLocation() {
        return representationMetaData.getSchemaLocation();
    }
}
