/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.misc;

import org.jdom2.Namespace;

import org.mule.tools.apikit.ExtensionManager;
import org.mule.tools.apikit.model.API;
import org.mule.tools.apikit.output.NamespaceWithLocation;

public class APIKitTools {
    public static final NamespaceWithLocation API_KIT_NAMESPACE = new NamespaceWithLocation(
            Namespace.getNamespace("apikit", "http://www.mulesoft.org/schema/mule/apikit"),
            "http://www.mulesoft.org/schema/mule/apikit/current/mule-apikit.xsd"
    );

    public static String getPathFromUri(String baseUri, boolean addAsterisk) {
        int start = baseUri.indexOf("//") + 2;
        if (start == -1)
        {
            start = 0;
        }

        int slash = baseUri.indexOf("/", start);
        if (slash == -1 || slash == baseUri.length())
        {
            return addAsterisk? "/*" : "/";
        }
        String path = baseUri.substring(slash, baseUri.length());
        int curlyBrace = baseUri.indexOf("{",slash);
        if (curlyBrace == -1)
        {
            return addAsterisk? addAsteriskToPath(path): path;
        }
        path = baseUri.substring(slash,curlyBrace);
        return addAsterisk? addAsteriskToPath(path): path;
    }

    public static String addAsteriskToPath(String path)
    {
        if (path == null)
        {
            return "/*";
        }
        if (!path.endsWith("*"))
        {
            path = path.endsWith("/")? path + "*" : path + "/*";
        }
        return path;
    }

    public static String getHostFromUri(String baseUri)
    {
        int start = baseUri.indexOf("//") + 2;
        if (start == -1)
        {
            start = 0;
        }

        int twoDots = baseUri.indexOf(":", start);
        if (twoDots == -1)
        {
            twoDots = baseUri.length();
        }
        int slash = baseUri.indexOf("/", start);
        if (slash == -1)
        {
            slash = baseUri.length();
        }
        int hostEnd = twoDots < slash ? twoDots : slash;
        return baseUri.substring(start,hostEnd);
    }

    public static String getPortFromUri(String baseUri)
    {
        int hostStart = baseUri.indexOf("//") + 2;
        if (hostStart == -1)
        {
            hostStart = 0;
        }
        int slash = baseUri.indexOf("/", hostStart);
        if (slash == -1)
        {
            slash = baseUri.length();
        }
        int twoDots = baseUri.indexOf(":", hostStart);
        if (twoDots == -1 || twoDots > slash)
        {
            return Integer.toString(API.DEFAULT_PORT);
        }
        return baseUri.substring(twoDots + 1, slash);
    }

    public static String getCompletePathFromBasePathAndPath(String basePath, String listenerPath)
    {

        String path = basePath + listenerPath;
        if (path.contains("/*"))
        {
            path = path.replace("/*","");
        }
        if (path.endsWith("/"))
        {
            path = path.substring(0,path.length() -1);
        }
        if (path.contains("//"))
        {
            path = path.replace("//","/");
        }
        return path;
    }

    public static boolean defaultIsInboundEndpoint(String candidateVersion){
        if (candidateVersion == null) {
            return false;
        }
        String[] versionParts = candidateVersion.split("\\.");
        if (versionParts.length < 2)
        {
            return false;
        }
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        if (major == 3 && minor < 6)
        {
            return true;
        }
        return false;
    }

    public static boolean canExtensionsBeEnabled(String candidateVersion)
    {
        if (candidateVersion == null)
        {
            return false;
        }
        String[] versionParts = candidateVersion.split("\\.");
        if (versionParts.length < 2)
        {
            return false;
        }
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        if (major > 3 || (major == 3 && minor >= 7))
        {
            return true;
        }
        return false;
    }
}
