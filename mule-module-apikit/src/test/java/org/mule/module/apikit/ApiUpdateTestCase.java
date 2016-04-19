/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.construct.Flow;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.module.apikit.uri.URIPattern;
import org.mule.module.http.internal.listener.DefaultHttpListener;
import org.mule.module.http.internal.listener.DefaultHttpListenerConfig;
import org.mule.raml.interfaces.model.IAction;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import org.junit.Before;
import org.junit.Test;

public class ApiUpdateTestCase extends AbstractMuleContextTestCase
{

    private static final String METHOD_GET = "get";
    private static final String RESOURCE_LEAGUES = "/leagues";
    private static final String GET_LEAGUES = METHOD_GET + ":" + RESOURCE_LEAGUES;
    private static final String RESOURCE_ORDERS = "/orders";
    private static final String GET_ORDERS = METHOD_GET + ":" + RESOURCE_ORDERS;
    private Configuration config;
    private String traitName = "secured";
    private String traitYaml = "usage: Apply this to any method that needs to be secured\n" +
                               "description: Some requests require authentication.\n" +
                               "headers:\n" +
                               "  injected:\n" +
                               "    displayName: injected\n" +
                               "    required: true\n" +
                               "queryParameters:\n" +
                               "  access_token:\n" +
                               "    description: Access Token\n" +
                               "    type: string\n" +
                               "    example: ACCESS_TOKEN\n" +
                               "    required: true";

    private void setupConfig(String yamlPath) throws InitialisationException
    {
        DefaultHttpListenerConfig listenerConfig = mock(DefaultHttpListenerConfig.class);
        when(listenerConfig.getHost()).thenReturn("localhost");
        when(listenerConfig.getPort()).thenReturn(8080);
        when(listenerConfig.getTlsContext()).thenReturn(null);
        DefaultHttpListener listener = mock(DefaultHttpListener.class);
        when(listener.getConfig()).thenReturn(listenerConfig);
        when(listener.getPath()).thenReturn("api");
        Flow flow = mock(Flow.class);
        when(flow.getMessageSource()).thenReturn(listener);

        config = new Configuration();
        config.setMuleContext(muleContext);
        config.setRaml("org/mule/module/apikit/" + yamlPath);
        config.initialise();
        config.loadApiDefinition(flow);
    }

    @Before
    public void setupConfigWithTraits() throws InitialisationException
    {
        setupConfig("leagues/leagues.yaml");
    }

    private void setupConfigWithoutTraits() throws InitialisationException
    {
        setupConfig("pathless/pathless.yaml");
    }

    @Test
    public void addAndRemoveTrait() throws InitialisationException
    {
        assertInitialStateWithTraits();
        config.getRamlUpdater().injectTrait(traitName, traitYaml).applyTrait(traitName, GET_LEAGUES).resetAndUpdate();

        assertThat(config.getApi().getTraits().size(), is(2));

        assertTraitInjected(config.getApi().getResource(RESOURCE_LEAGUES).getAction(METHOD_GET));
        assertTraitInjected(config.routingTable.get(new URIPattern(RESOURCE_LEAGUES)).getAction(METHOD_GET));

        config.getRamlUpdater().reset();
        assertInitialStateWithTraits();
    }

    private void assertTraitInjected(IAction action)
    {
        assertThat(action.getIs().size(), is(1));
        assertThat(action.getIs().get(0), is(traitName));
        assertThat(action.getHeaders().size(), is(1));
        assertThat(action.getHeaders().get("injected").getDisplayName(), is("injected"));
        assertThat(action.getQueryParameters().size(), is(1));
        assertThat(action.getQueryParameters().get("access_token").getDescription(), is("Access Token"));
    }

    @Test
    public void addAndRemoveFirstTrait() throws InitialisationException
    {
        setupConfigWithoutTraits();

        assertInitialStateWithoutTraits();
        config.getRamlUpdater().injectTrait(traitName, traitYaml).applyTrait(traitName, GET_ORDERS).resetAndUpdate();

        assertThat(config.getApi().getTraits().size(), is(1));

        assertTraitInjected(config.getApi().getResource(RESOURCE_ORDERS).getAction(METHOD_GET));
        assertTraitInjected(config.routingTable.get(new URIPattern(RESOURCE_ORDERS)).getAction(METHOD_GET));

        config.getRamlUpdater().reset();
        assertInitialStateWithoutTraits();
    }

    @Test
    public void addAndRemoveSecurityScheme() throws InitialisationException
    {
        String name = "oauth2SecurityScheme";
        String yaml = "description: |\n" +
                      "    Dropbox supports OAuth 2.0 for authenticating all API requests.\n" +
                      "type: OAuth 2.0\n" +
                      "describedBy:\n" +
                      "    headers:\n" +
                      "        Authorization:\n" +
                      "            description: |\n" +
                      "               Used to send a valid OAuth 2 access token. Do not use\n" +
                      "               with the \"access_token\" query string parameter.\n" +
                      "            type: string\n" +
                      "    queryParameters:\n" +
                      "        access_token:\n" +
                      "            description: |\n" +
                      "               Used to send a valid OAuth 2 access token. Do not use together with\n" +
                      "               the \"Authorization\" header\n" +
                      "            type: string\n" +
                      "    responses:\n" +
                      "        401:\n" +
                      "            description: |\n" +
                      "                Bad or expired token.\n" +
                      "        403:\n" +
                      "            description: |\n" +
                      "                Bad OAuth request (wrong consumer key, bad nonce, expired\n" +
                      "                timestamp...). Unfortunately, re-authenticating the user won't help here.\n" +
                      "settings:\n" +
                      "  authorizationUri: https://www.dropbox.com/1/oauth2/authorize\n" +
                      "  accessTokenUri: https://api.dropbox.com/1/oauth2/token\n" +
                      "  authorizationGrants: [code, token]\n" +
                      "  scopes: [ 'https://www.google.com/m8/feeds' ]\n";

        assertInitialStateWithTraits();
        config.getRamlUpdater().injectSecuritySchemes(name, yaml)
                .applySecurityScheme(name, GET_LEAGUES).resetAndUpdate();

        assertThat(config.getApi().getSecuritySchemes().size(), is(1));
        assertSecuritySchemeInjected(config.getApi().getResource(RESOURCE_LEAGUES).getAction(METHOD_GET), name);
        assertSecuritySchemeInjected(config.routingTable.get(new URIPattern(RESOURCE_LEAGUES)).getAction(METHOD_GET), name);

        config.getRamlUpdater().reset();
        assertInitialStateWithTraits();
    }

    private void assertSecuritySchemeInjected(IAction action, String name)
    {
        assertThat(action.getSecuredBy().size(), is(1));
        assertThat(action.getSecuredBy().get(0).getName(), is(name));
    }

    @Test(expected = ApikitRuntimeException.class)
    public void traitMismatch()
    {
        config.getRamlUpdater().injectTrait("rightName", "headers:").applyTrait("wrongName", GET_LEAGUES).resetAndUpdate();
    }

    @Test(expected = ApikitRuntimeException.class)
    public void securitySchemaMismatch()
    {
        config.getRamlUpdater().injectSecuritySchemes("rightName", "settings:").applySecurityScheme("wrongName", GET_LEAGUES).resetAndUpdate();
    }

    @Test(expected = ApikitRuntimeException.class)
    public void injectAndResetForbidden()
    {
        config.getRamlUpdater().injectSecuritySchemes("name", "settings:").reset();
    }

    private void assertInitialState(int traits, String resource)
    {
        assertThat(config.getApi().getTraits().size(), is(traits));
        assertThat(config.getApi().getSecuritySchemes().size(), is(0));
        IAction action = config.getApi().getResource(resource).getAction(METHOD_GET);
        assertThat(action.getIs().size(), is(0));
        assertThat(action.getHeaders().size(), is(0));
        assertThat(action.getQueryParameters().size(), is(0));
        assertThat(action.getSecuredBy().size(), is(0));
    }

    private void assertInitialStateWithTraits()
    {
        assertInitialState(1, RESOURCE_LEAGUES);
    }

    private void assertInitialStateWithoutTraits()
    {
        assertInitialState(0, RESOURCE_ORDERS);

    }

}
