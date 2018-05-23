package org.mule.amf.impl;

import java.util.Map;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IActionType;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;

import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

abstract class AbstractTestCase {

    private static final String MISSING_RESOURCE = "Resource '%s' missing in AMF Resources for uri '%s'";
    private static final String MISSING_ACTION = "Action '%s' missing";
    private static final String MISSING_PARAMETER = "Parameter '%s' missing";

    static void assertResourcesEqual(final IParameter actual, final IParameter expected) {
        assertThat(actual.getDefaultValue(), is(equalTo(expected.getDefaultValue())));
        assertThat(actual.getDescription(), is(equalTo(expected.getDescription())));
        assertThat(actual.getDisplayName(), is(equalTo(expected.getDisplayName())));
        assertThat(actual.getExample(), is(equalTo(expected.getExample())));
        assertThat(actual.getExamples().size(), is(expected.getExamples().size()));
        assertThat(actual.isArray(), is(expected.isArray()));
    }

    static void assertResourcesEqual(final Map<String, IResource> actual, final Map<String, IResource> expected) {

        assertThat(actual.size(), is(expected.size()));

        actual.forEach((k, v) -> {
            assertThat(format(MISSING_RESOURCE, k, v.getUri()), expected.containsKey(k), is(true));
            assertEqual(v, expected.get(k));
        });
    }

    static void assertEqual(final IResource actual, final IResource expected) {
        assertThat(actual.getUri(), is(equalTo(expected.getUri())));
        assertThat(actual.getParentUri(), is(equalTo(expected.getParentUri())));
        assertThat(actual.getRelativeUri(), is(equalTo(expected.getRelativeUri())));
        assertThat(actual.getDisplayName(), is(equalTo(expected.getDisplayName())));
        
        assertActionsEqual(actual.getActions(), expected.getActions());
        assertParametersEqual(actual.getResolvedUriParameters(), expected.getResolvedUriParameters());

        // TODO
        // actual.getBaseUriParameters();
        
        assertResourcesEqual(actual.getResources(), expected.getResources());
    }

    static void assertActionsEqual(final Map<IActionType, IAction> actual, final Map<IActionType, IAction> expected) {

        assertThat(actual.size(), is(expected.size()));

        actual.forEach((k, v) -> {
            assertThat(format(MISSING_ACTION, k), expected.containsKey(k), is(true));
            assertEqual(v, expected.get(k));
        });
    }

    static void assertEqual(final IAction actual, final IAction expected) {
        assertThat(actual.getType(), is(equalTo(expected.getType())));
        
        assertParametersEqual(actual.getHeaders(), expected.getHeaders());
        // TODO MORE cases
    }

    static void assertParametersEqual(final Map<String, IParameter> actual, final Map<String, IParameter> expected) {

        assertThat(actual.size(), is(expected.size()));

        actual.forEach((k, v) -> {
            assertThat(format(MISSING_PARAMETER, k), expected.containsKey(k), is(true));
            assertEqual(v, expected.get(k));
        });
    }

    static void assertEqual(final IParameter actual, final IParameter expected) {
        assertThat(actual.getDisplayName(), is(equalTo(expected.getDisplayName())));
        // TODO MORE cases
    }


}
