package org.mule.tooling.apikit.template;

import java.io.Reader;
import java.io.Writer;

public interface Replacer {

	void replace(Reader reader, Writer writer) throws Exception;


}
