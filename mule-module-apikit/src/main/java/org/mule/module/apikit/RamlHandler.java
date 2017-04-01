/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.module.apikit.parser.ParserService;
import org.mule.raml.interfaces.model.IRaml;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RamlHandler
{

    private String ramlLocation;
    private boolean keepRamlBaseUri;
    private Map<String, String> apikitRaml;
    private String apiServer;
    private IRaml api;
    private ParserService parserService;


    public RamlHandler(String ramlLocation, String apiServer, boolean keepRamlBaseUri, String appHome)
    {
        this.ramlLocation = ramlLocation;
        this.keepRamlBaseUri = keepRamlBaseUri;
        this.apiServer = apiServer;

        apikitRaml = new ConcurrentHashMap<String, String>();
        parserService = new ParserService(ramlLocation, appHome);
        parserService.validateRaml();
        this.api = parserService.build();
        if (!keepRamlBaseUri && apiServer != null)
        {
            parserService.updateBaseUri(api, apiServer);
        }
    }

    public boolean isParserV2() {
        return parserService.isParserV2();
    }

    public IRaml getApi()
    {
        return api;
    }

    public void setApi(IRaml api)
    {
        this.api = api;
    }
}