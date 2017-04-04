/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.interfaces;

public class ParserUtils
{

    public static String resolveVersion(String path, String version)
    {
        if (path == null)
        {
            throw new IllegalArgumentException("path cannot be null");
        }
        if (!path.contains("{version}"))
        {
            return path;
        }
        if (version == null)
        {
            throw new IllegalStateException("RAML does not contain version information and is required by resource: " + path);
        }
        return path.replaceAll("\\{version}", version);
    }

}
