/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.injector;

import java.util.Map;

import org.raml.model.Action;
import org.raml.model.Response;

public class InjectableTrait extends InjectableRamlFeature
{

    private static final String RESOURCE = "/base";
    private static final String ACTION = "get";
    private static final String TEMPLATE_BEFORE = "#%RAML 0.8\ntitle: t\ntraits:\n - injected:\n";
    private static final String TEMPLATE_AFTER = RESOURCE + ":\n " + ACTION + ":\n  is: [injected]\n";
    private static final String INDENTATION = "    ";

    private Action cache;

    public InjectableTrait(String name, String yaml)
    {
        super(name, yaml);
    }

    @Override
    protected String getBoilerPlateBefore()
    {
        return TEMPLATE_BEFORE;
    }

    @Override
    protected String getBoilerPlateAfter()
    {
        return TEMPLATE_AFTER;
    }

    @Override
    protected String getIndentation()
    {
        return INDENTATION;
    }

    @Override
    public void applyToAction(Action target)
    {
        if (cache == null)
        {
            cache = parse().getResource(RESOURCE).getAction(ACTION);
        }
        Action source = resolveParams(target);
        mergeActions(target, source);
        target.getIs().add(name);
    }

    private void mergeActions(Action target, Action source)
    {
        putAllSkipExisting(target.getHeaders(), source.getHeaders());
        putAllSkipExisting(target.getQueryParameters(), source.getQueryParameters());
        if (source.getBody() != null)
        {
            putAllSkipExisting(target.getBody(), source.getBody());
        }
        for (Map.Entry<String, Response> response : source.getResponses().entrySet())
        {
            if (target.getResponses().containsKey(response.getKey()))
            {
                putAllSkipExisting(target.getResponses().get(response.getKey()).getBody(), response.getValue().getBody());
                putAllSkipExisting(target.getResponses().get(response.getKey()).getHeaders(), response.getValue().getHeaders());
            }
            else
            {
                target.getResponses().put(response.getKey(), response.getValue());
            }
        }
    }

    private Action resolveParams(Action target)
    {
        //TODO replace implicit parameters
        return cache;
    }

    private <K, V> void putAllSkipExisting(Map<K, V> to, Map<K, V> from)
    {
        if (to != null && from != null)
        {
            for (Map.Entry<K, V> entry : from.entrySet())
            {
                if (!to.containsKey(entry.getKey()))
                {
                    to.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
