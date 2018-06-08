package org.mule.tools.apikit;

public enum ParserType {
    AMF, RAML;

    public static ParserType defaultType() {
        return RAML;
    }
}
