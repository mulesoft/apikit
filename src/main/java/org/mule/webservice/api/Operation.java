package org.mule.webservice.api;

import org.mule.api.NamedObject;
import org.mule.api.processor.MessageProcessor;

public interface Operation extends NamedObject, MessageProcessor {

	String getRoles();
	
}
