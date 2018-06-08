package org.mule.tools.apikit;

public enum Parser {
    AMF, RAML;

    public static Parser defaultType() {
        return RAML;
    }
}
