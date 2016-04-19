/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.injector;

import org.mule.module.apikit.AbstractConfiguration;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.raml.implv1.model.RamlImplV1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.SecurityScheme;
import org.raml.model.Template;

public class RamlUpdater
{

    private Raml raml;
    private AbstractConfiguration config;
    private Set<String> currentTraits;
    private Set<String> currentSecuritySchemes;
    private Map<String, InjectableTrait> injectedTraits;
    private Map<String, InjectableSecurityScheme> injectedSecuritySchemes;

    public RamlUpdater(Raml raml, AbstractConfiguration config)
    {
        this.raml = raml;
        this.config = config;
        injectedTraits = new HashMap<String, InjectableTrait>();
        injectedSecuritySchemes = new HashMap<String, InjectableSecurityScheme>();
        populateCurrentTraits();
        populateCurrentSecuritySchemes();
    }

    private void populateCurrentSecuritySchemes()
    {
        currentSecuritySchemes = new HashSet<String>();
        for (Map<String, SecurityScheme> schemes : raml.getSecuritySchemes())
        {
            currentSecuritySchemes.addAll(schemes.keySet());
        }
    }

    private void populateCurrentTraits()
    {
        currentTraits = new HashSet<String>();
        for (Map<String, Template> traitMap : raml.getTraits())
        {
            currentTraits.addAll(traitMap.keySet());
        }
    }

    public void resetAndUpdate()
    {
        config.updateApi(new RamlImplV1(raml));
    }

    public void reset()
    {
        if (injectedTraits.isEmpty() && injectedSecuritySchemes.isEmpty())
        {
            this.resetAndUpdate();
        }
        else
        {
            throw new ApikitRuntimeException("Cannot inject and reset with the same Updater");
        }
    }

    private Template getTemplate(String name)
    {
        Template template = new Template();
        template.setDisplayName(name);
        return template;
    }

    public RamlUpdater injectTrait(String name, String traitYaml)
    {
        if (currentTraits.contains(name))
        {
            throw new TraitAlreadyDefinedException("Duplicate Trait definition: " + name);
        }
        currentTraits.add(name);
        Map<String, Template> traitDef = new HashMap<String, Template>();
        traitDef.put(name, getTemplate(name));
        raml.getTraits().add(traitDef);
        this.injectedTraits.put(name, new InjectableTrait(name, traitYaml));
        return this;
    }

    public RamlUpdater applyTrait(String name, String... actionRefs)
    {
        for (String actionRef : actionRefs)
        {
            Action action = getAction(actionRef);
            InjectableTrait injectableTrait = injectedTraits.get(name);
            if (injectableTrait == null)
            {
                throw new ApikitRuntimeException("Trying to apply an undefined Trait: " + name);
            }
            injectableTrait.applyToAction(action);
        }
        return this;
    }

    private Action getAction(String actionRef)
    {
        String[] coord = actionRef.split(":");
        Action action = raml.getResource(coord[1]).getAction(coord[0]);
        return action;
    }

    public RamlUpdater injectSecuritySchemes(String name, String securitySchemeYaml)
    {
        if (currentSecuritySchemes.contains(name))
        {
            throw new SecuritySchemeAlreadyDefinedException("Duplicate Security Scheme definition: " + name);
        }
        currentSecuritySchemes.add(name);
        Map<String, SecurityScheme> securitySchemeDef = new HashMap<String, SecurityScheme>();
        InjectableSecurityScheme injectableSecurityScheme = new InjectableSecurityScheme(name, securitySchemeYaml);
        securitySchemeDef.put(name, injectableSecurityScheme.getSecurityScheme());
        raml.getSecuritySchemes().add(securitySchemeDef);
        this.injectedSecuritySchemes.put(name, injectableSecurityScheme);
        return this;
    }

    public RamlUpdater applySecurityScheme(String name, String... actionRefs)
    {
        for (String actionRef : actionRefs)
        {
            Action action = getAction(actionRef);
            InjectableSecurityScheme injectableSecurityScheme = injectedSecuritySchemes.get(name);
            if (injectableSecurityScheme == null)
            {
                throw new ApikitRuntimeException("Trying to apply an undefined Security Scheme: " + name);
            }
            injectableSecurityScheme.applyToAction(action);
        }
        return this;
    }

}
