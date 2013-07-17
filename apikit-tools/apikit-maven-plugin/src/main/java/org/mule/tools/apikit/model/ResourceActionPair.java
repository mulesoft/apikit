package org.mule.tools.apikit.model;

import org.apache.commons.lang.Validate;

import java.io.File;

public class ResourceActionPair {
    private final API api;
    private final String uri;
    private final String verb;

    public ResourceActionPair(API api, String uri, String verb) {
        Validate.notNull(api);
        Validate.notNull(uri);
        Validate.notNull(verb);

        this.uri = uri;
        this.verb = verb.toUpperCase();
        this.api = api;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceActionPair that = (ResourceActionPair) o;

        if (!api.equals(that.api)) return false;
        if (!uri.equals(that.uri)) return false;
        if (!verb.equals(that.verb)) return false;

        return true;
    }

    @Override
    public String toString() {
        return "ResourceActionPair{" +
                "uri='" + uri + '\'' +
                ", verb='" + verb + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        int result = api.hashCode();
        result = 31 * result + uri.hashCode();
        result = 31 * result + verb.hashCode();
        return result;
    }
}
