/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.config;

import org.mule.module.apikit.Configuration;
import org.mule.module.apikit.Console;
import org.mule.module.apikit.FlowMapping;
import org.mule.module.apikit.Router;
import org.mule.runtime.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.runtime.config.spring.handlers.MuleNamespaceHandler;
import org.mule.runtime.config.spring.parsers.collection.ChildListEntryDefinitionParser;
import org.mule.runtime.config.spring.parsers.generic.ChildDefinitionParser;
import org.mule.runtime.config.spring.parsers.generic.MuleOrphanDefinitionParser;

public class ApikitNamespaceHandler extends AbstractMuleNamespaceHandler
{

    public void init()
    {}
}
