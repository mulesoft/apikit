
package org.mule.webservice.api;

import org.mule.api.processor.MessageProcessor;

public interface LeafOperation extends Operation
{
    MessageProcessor getHandler();
}
