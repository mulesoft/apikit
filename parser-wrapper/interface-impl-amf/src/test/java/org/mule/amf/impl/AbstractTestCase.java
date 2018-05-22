package org.mule.amf.impl;

import java.util.Map;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;

import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

abstract class AbstractTestCase {

    private static final String MISSING_RESOURCE = "Resource '%s' is missing in AMF Resources for uri '%s'";

    static void assertEqual(final IParameter actual, final IParameter expected) {
        assertThat(actual.getDefaultValue(), is(equalTo(expected.getDefaultValue())));
        assertThat(actual.getDescription(), is(equalTo(expected.getDescription())));
        assertThat(actual.getDisplayName(), is(equalTo(expected.getDisplayName())));
        assertThat(actual.getExample(), is(equalTo(expected.getExample())));
        assertThat(actual.getExamples().size(), is(expected.getExamples().size()));
        assertThat(actual.isArray(), is(expected.isArray()));
    }

    static void assertEqual(final Map<String, IResource> actual, final Map<String, IResource> expected) {

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
    }
}
