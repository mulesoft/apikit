package org.mule.module.apikit.api;

import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.core.api.construct.Flow;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.mule.apikit.common.CommonUtils.cast;
import static org.mule.module.apikit.MessageSourceUtils.getUriFromFlow;

public class FlowUtils {

  public static final String FLOW_TAG_NAME = "flow";
  public static final String MULE_NAMESPACE = "mule";

  public static List<Flow> getFlowsList(ConfigurationComponentLocator locator) {
    return cast(locator.find(ComponentIdentifier.builder().name(FLOW_TAG_NAME).namespace(MULE_NAMESPACE).build()));
  }

  public static Optional<Component> getSource(ConfigurationComponentLocator locator, String flowName) {
    return locator.find(Location.builder().globalName(flowName).addSourcePart().build());
  }

  public static Optional<URI> getSourceLocation(Component sourceComponent) {
    return ofNullable(getUriFromFlow(sourceComponent));
  }

  public static Optional<URI> getSourceLocation(ConfigurationComponentLocator locator, String flowName) {
    return getSource(locator, flowName).flatMap(source -> ofNullable(getUriFromFlow(source)));
  }

}
