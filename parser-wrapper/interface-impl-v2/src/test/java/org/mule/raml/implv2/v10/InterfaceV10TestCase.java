/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv2.v10;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.mule.raml.implv2.ParserV2Utils;
import org.mule.raml.implv2.loader.ExchangeDependencyResourceLoader;
import org.mule.raml.implv2.v10.model.RamlImpl10V2;
import org.mule.raml.interfaces.model.IRaml;

import org.junit.Test;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.loader.CompositeResourceLoader;
import org.raml.v2.api.loader.DefaultResourceLoader;
import org.raml.v2.api.loader.ResourceLoader;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class InterfaceV10TestCase
{
    private static final DefaultResourceLoader DEFAULT_RESOURCE_LOADER = new DefaultResourceLoader();


    @Test
    public void check()
    {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        IRaml raml = ParserV2Utils.build(resourceLoader, "org/mule/raml/implv2/v10/full-1.0.raml");
        assertThat(raml.getVersion(), is("1.0"));
        assertThat(raml.getSchemas().get(0).size(), is(2));
        assertThat(raml.getSchemas().get(0).get("User"), is("[yaml-type-flag]"));
        assertThat(raml.getSchemas().get(0).get("UserJson"), containsString("firstname"));
    }

    @Test
    public void references() {
        final String ramlPath = "org/mule/raml/implv2/v10/references/api.raml";

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        RamlModelResult ramlModelResult = new RamlModelBuilder(resourceLoader).buildApi(ramlPath);
        RamlImpl10V2 raml = new RamlImpl10V2(ramlModelResult.getApiV10(), resourceLoader, ramlPath);

        List<String> allReferences = raml.getAllReferences();
        for(String ref : allReferences){
            assertThat("Invalid URI", URI.create(ref).toString(), is(ref));
        }
        assertEquals(6, allReferences.size());

        assertThat(endWithAndExists (allReferences, "org/mule/raml/implv2/v10/references/address.raml", resourceLoader), is(true));
        assertThat(endWithAndExists(allReferences, "org/mule/raml/implv2/v10/references/company-example.json", resourceLoader),
                is(true));
        assertThat(endWithAndExists(allReferences, "org/mule/raml/implv2/v10/references/partner.raml", resourceLoader), is(true));
        assertThat(endWithAndExists(allReferences, "org/mule/raml/implv2/v10/references/data-type.raml", resourceLoader), is(true));
        assertThat(endWithAndExists(allReferences, "org/mule/raml/implv2/v10/references/library.raml", resourceLoader), is(true));
        assertThat( endWithAndExists(allReferences, "org/mule/raml/implv2/v10/references/company.raml", resourceLoader), is(true));
    }

    @Test
    public void referencesWithExchangeModule() {
        final String ramlPath = "org/mule/raml/implv2/v10/exchange/api.raml";
        final CompositeResourceLoader resourceLoader =
                new CompositeResourceLoader(DEFAULT_RESOURCE_LOADER, new ExchangeDependencyResourceLoader());


        RamlModelResult ramlModelResult = new RamlModelBuilder(resourceLoader).buildApi(ramlPath);
        RamlImpl10V2 raml = new RamlImpl10V2(ramlModelResult.getApiV10(), resourceLoader, ramlPath);


        List<String> allReferences = raml.getAllReferences();
        assertEquals(3, allReferences.size());

        assertThat(endWithAndExists(allReferences, "org/mule/raml/implv2/v10/exchange/exchange_modules/library1.raml", resourceLoader),
                is(true));
        assertThat(endWithAndExists(allReferences, "org/mule/raml/implv2/v10/exchange/exchange_modules/library2.raml", resourceLoader),
                is(true));
        assertThat(endWithAndExists(allReferences, "org/mule/raml/implv2/v10/exchange/exchange_modules/library3.raml", resourceLoader),
                is(true));
    }

    @Test
    public void getAllRefsIncludeArrayExample() {
        String ramlPath = "org/mule/raml/implv2/v10/get-all-refs/array-example.raml";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        RamlModelResult ramlModelResult = new RamlModelBuilder(resourceLoader).buildApi(ramlPath);
        RamlImpl10V2 raml = new RamlImpl10V2(ramlModelResult.getApiV10(), resourceLoader, ramlPath);
        assertThat(raml.getAllReferences().size(), is(1));
    }

    @Test
    public void absoluteIncludes() {
        URL resource = getClass().getClassLoader().getResource("org/mule/raml/implv2/v10/library-references-absolute/input.raml");
        RamlModelResult ramlModelResult = new RamlModelBuilder(DEFAULT_RESOURCE_LOADER).buildApi(resource.toString());

        RamlImpl10V2 raml = new RamlImpl10V2(ramlModelResult.getApiV10(), DEFAULT_RESOURCE_LOADER, resource.toString());

        List<String> references = raml.getAllReferences();
        assertReference(references, "org/mule/raml/implv2/v10/library-references-absolute/libraries/resourceTypeLibrary.raml");
        assertReference(references, "org/mule/raml/implv2/v10/library-references-absolute/libraries/typeLibrary.raml");
        assertReference(references, "org/mule/raml/implv2/v10/library-references-absolute/libraries/traitsLibrary.raml");
        assertReference(references, "org/mule/raml/implv2/v10/library-references-absolute/traits/trait.raml");
        assertThat(raml.getAllReferences().size(), is(4));
    }

    private void assertReference(List<String> references, String s) {

        assertThat(exists(references,s), is(true));
    }

    private boolean exists(List<String> references, String s){
        for(String reference : references){
            if(reference.endsWith(s)){
                return true;
            }
        }
        return false;
    }

    private boolean endWithAndExists(List<String> references, String goldenFile, ResourceLoader resourceLoader) {
        for(String reference:references){
            if(reference.endsWith(goldenFile) && resourceLoader.fetchResource(reference) != null){
                return true;
            }
        }

        return false;
    }
}
