package org.mule.amf.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.parameter.IParameter;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class CompatibilityTestCase extends AbstractCompatibilityTestCase {


  public CompatibilityTestCase(final File input, final String name) {
    super(input, name);
  }

  @Parameterized.Parameters(name = "{1}")
  public static Collection<Object[]> getData() throws IOException, URISyntaxException {
    final URI baseFolder = CompatibilityTestCase.class.getResource("").toURI(); //body-type-in-raml10
    return getData(baseFolder);
  }

  @Test
  public void baseUri() {
    final String ramlBaseUri = raml.getBaseUri();
    final String amfBaseUri = amf.getBaseUri();

    assertThat(ramlBaseUri, is(equalTo(amfBaseUri)));
  }

  @Ignore
  public void uri() {
    final String ramlUri = raml.getBaseUri();
    final String amfUri = amf.getBaseUri();
    assertThat(ramlUri, is(equalTo(amfUri)));
  }

  @Test
  public void version() {
    final String ramlVersion = raml.getVersion();
    final String amfVersion = amf.getVersion();
    assertThat(ramlVersion, is(equalTo(amfVersion)));
  }

  @Test
  public void baseUriParameters() {
    final Map<String, IParameter> ramlBaseUriParameters = raml.getBaseUriParameters();
    final Map<String, IParameter> amfBaseUriParameters = amf.getBaseUriParameters();

    assertThat(ramlBaseUriParameters.size(), is(amfBaseUriParameters.size()));

    ramlBaseUriParameters.forEach((k, v) -> {
      assertThat(amfBaseUriParameters.containsKey(k), is(true));
      assertResourcesEqual(v, amfBaseUriParameters.get(k));
    });
  }

  @Test
  public void resources() {
    final Map<String, IResource> ramlResources = raml.getResources();
    final Map<String, IResource> amfResources = amf.getResources();

    assertResourcesEqual(amfResources, ramlResources);
  }
}
