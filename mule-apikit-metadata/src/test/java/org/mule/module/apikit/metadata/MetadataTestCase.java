/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.metadata.api.model.FunctionType;
import org.mule.metadata.internal.utils.MetadataTypeWriter;
import org.mule.module.apikit.metadata.interfaces.Notifier;
import org.mule.module.apikit.metadata.interfaces.ResourceLoader;
import org.mule.module.apikit.metadata.model.Flow;
import org.mule.module.apikit.metadata.raml.RamlHandler;
import org.mule.module.apikit.metadata.utils.MockedApplicationModel;
import org.mule.module.apikit.metadata.utils.TestDataProvider;
import org.mule.runtime.config.internal.model.ApplicationModel;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(Parameterized.class)
public class MetadataTestCase extends TestDataProvider {

  private static final Pattern OUTPUT_PATTERN = Pattern.compile(".*\\.out");

  public MetadataTestCase(File input, Map<String, String> expectedOutput, String name) {
    super(input, expectedOutput, name);
  }

  @Parameterized.Parameters(name = "{2}")
  public static Collection<Object[]> getData() throws IOException, URISyntaxException {
    final URI baseFolder = MetadataTestCase.class.getResource("").toURI(); //body-type-in-raml10
    return getData(baseFolder, "app.xml", OUTPUT_PATTERN);
  }

  @Test
  public void runTest() throws Exception {
    final ResourceLoader resourceLoader = new TestResourceLoader();
    final TestNotifier notifier = new TestNotifier();
    final RamlHandler ramlHandler = new RamlHandler(resourceLoader, notifier);

    final ApplicationModel applicationModel = createApplicationModel(input);
    assertThat(applicationModel, notNullValue());

    final ApplicationModelWrapper wrapper = new ApplicationModelWrapper(applicationModel, ramlHandler, notifier);

    final Metadata metadata = new Metadata.Builder()
        .withApplicationModel(applicationModel)
        .withResourceLoader(resourceLoader)
        .withNotifier(notifier).build();

    // Search metadata for each flow
    final List<FlowFunctionType> types = wrapper.findFlows().stream()
        .map(flow -> getMetadata(metadata, flow))
        .filter(Optional::isPresent).map(Optional::get)
        .collect(toList());

    currentMap = types.stream().collect(toMap(FlowFunctionType::getFileName, FlowFunctionType::toString));

    assertThat("Number of function metadata differs from the expected ones.", currentMap.size(), is(equalTo(expectedMap.size())));
    assertThat("Flow names differs from the expected ones.", currentMap.keySet(), is(equalTo(expectedMap.keySet())));

    currentMap.forEach((name, actual) -> {
      final String expected = expectedMap.get(name);
      assertThat(format("Function metadata differ from expected. File: '%s'", name), actual, is(equalTo(expected)));
    });

    notifier.messages.keySet()
        .forEach(key -> notifier.messages(key).forEach(message -> System.out.println(key.toUpperCase() + " - " + message)));
  }

  private static Optional<FlowFunctionType> getMetadata(Metadata metadata, Flow flow) {
    final Optional<FunctionType> metadataForFlow = metadata.getMetadataForFlow(flow.getName());

    return metadataForFlow.map(type -> FlowFunctionType.create(type, flow));
  }

  private ApplicationModel createApplicationModel(File resource) throws Exception {
    final MockedApplicationModel.Builder builder = new MockedApplicationModel.Builder();
    builder.addConfig("apiKitSample", resource);
    final MockedApplicationModel mockedApplicationModel = builder.build();
    return mockedApplicationModel.getApplicationModel();
  }

  private static class FlowFunctionType {

    private final FunctionType type;
    private final Flow flow;

    private FlowFunctionType(FunctionType type, Flow flow) {
      this.type = type;
      this.flow = flow;
    }

    private static FlowFunctionType create(FunctionType type, Flow flow) {
      return new FlowFunctionType(type, flow);
    }

    private FunctionType getType() {
      return type;
    }

    private Flow getFlow() {
      return flow;
    }

    private String getFileName() {
      return flow.getName()
          .replace("\\", "")
          .replace(":", "-") + ".out";
    }

    @Override
    public String toString() {
      return new MetadataTypeWriter().toString(type);
    }
  }

}
