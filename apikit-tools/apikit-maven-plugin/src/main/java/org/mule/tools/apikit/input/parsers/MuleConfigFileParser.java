package org.mule.tools.apikit.input.parsers;


import org.jdom2.Document;

public interface MuleConfigFileParser {

    Object parse(Document document);

}
