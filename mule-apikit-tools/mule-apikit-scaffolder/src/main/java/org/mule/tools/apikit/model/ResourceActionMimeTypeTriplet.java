/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.model;

import org.apache.commons.lang.Validate;

public class ResourceActionMimeTypeTriplet {
    private final API api;
    private final String uri;
    private final String verb;
    private final String mimeType;


    public ResourceActionMimeTypeTriplet(API api, String uri, String verb) {
        this(api, uri, verb, null);
    }

    public ResourceActionMimeTypeTriplet(API api, String uri, String verb, String mimeType) {
        Validate.notNull(api);
        Validate.notNull(uri);
        Validate.notNull(verb);

        this.uri = uri;
        this.verb = verb.toUpperCase();
        this.api = api;
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceActionMimeTypeTriplet that = (ResourceActionMimeTypeTriplet) o;

        if (!api.equals(that.api)) return false;
        if (!uri.equals(that.uri)) return false;
        if (!verb.equals(that.verb)) return false;
        if (mimeType != null && !mimeType.equals(that.mimeType)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "ResourceActionMimeTypeTriplet{" +
                "uri='" + uri + '\'' +
                ", verb='" + verb + '\'' +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        int result = api.hashCode();
        result = 31 * result + uri.hashCode();
        result = 31 * result + verb.hashCode();
        if (mimeType != null) {
            result = 31 * result + mimeType.hashCode();
        }
        return result;
    }

    public API getApi()
    {
        return api;
    }

    public String getUri()
    {
        return uri;
    }

    public String getVerb()
    {
        return verb;
    }

    public String getMimeType()
    {
        return mimeType;
    }
}
