/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import org.junit.Before;
import org.junit.Test;
import org.raml.model.Action;

public class ApiUpdateTestCase extends AbstractMuleContextTestCase
{

    private static final String GET_LEAGUES = "get:/leagues";
    private Configuration config;

    @Before
    public void setupConfig() throws InitialisationException
    {
        config = new Configuration();
        config.setMuleContext(muleContext);
        config.setRaml("org/mule/module/apikit/leagues/leagues.yaml");
        config.initialise();
    }

    @Test
    public void addAndRemoveTrait() throws InitialisationException
    {
        String name = "header";
        String yaml = "headers:\n" +
                             "  injected:\n" +
                             "    displayName: injected\n" +
                             "    required: true";

        assertInitialState();
        config.getRamlUpdater().injectTrait(name, yaml).applyTrait(name, GET_LEAGUES).resetAndUpdate();

        assertThat(config.getApi().getTraits().size(), is(2));
        Action action = config.getApi().getResource("/leagues").getAction("get");
        assertThat(action.getIs().size(), is(1));
        assertThat(action.getIs().get(0), is("header"));
        assertThat(action.getHeaders().size(), is(1));
        assertThat(action.getHeaders().get("injected").getDisplayName(), is("injected"));

        config.getRamlUpdater().reset();
        assertInitialState();
    }

    @Test
    public void addAndRemoveSecurityScheme()
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

        assertInitialState();
        config.getRamlUpdater().injectSecuritySchemes(name, yaml)
                .applySecurityScheme(name, GET_LEAGUES).resetAndUpdate();

        assertThat(config.getApi().getSecuritySchemes().size(), is(1));
        Action action = config.getApi().getResource("/leagues").getAction("get");
        assertThat(action.getSecuredBy().size(), is(1));
        assertThat(action.getSecuredBy().get(0).getName(), is(name));

        config.getRamlUpdater().reset();
        assertInitialState();
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

    private void assertInitialState()
    {
        assertThat(config.getApi().getTraits().size(), is(1));
        assertThat(config.getApi().getSecuritySchemes().size(), is(0));
        Action action = config.getApi().getResource("/leagues").getAction("get");
        assertThat(action.getIs().size(), is(0));
        assertThat(action.getHeaders().size(), is(0));
        assertThat(action.getSecuredBy().size(), is(0));
    }

}
