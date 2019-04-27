/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import static org.mule.module.apikit.ApikitErrorTypes.throwErrorType;
import static org.mule.module.apikit.api.FlowUtils.getFlowsList;
import static org.mule.module.apikit.helpers.AttributesHelper.getMediaType;
import static org.mule.module.apikit.helpers.FlowName.FLOW_NAME_SEPARATOR;
import static org.mule.module.apikit.helpers.FlowName.URL_RESOURCE_SEPARATOR;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.module.apikit.api.RamlHandler;
import org.mule.module.apikit.api.RoutingTable;
import org.mule.module.apikit.api.uri.URIPattern;
import org.mule.module.apikit.api.uri.URIResolver;
import org.mule.module.apikit.exception.NotImplementedException;
import org.mule.module.apikit.exception.UnsupportedMediaTypeException;
import org.mule.module.apikit.helpers.FlowName;
import org.mule.raml.interfaces.model.IAction;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.exception.ErrorTypeRepository;
import org.mule.runtime.core.api.construct.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FlowFinder {

  protected static final Logger logger = LoggerFactory.getLogger(FlowFinder.class);

  private Map<String, IResource> flatResourceTree = new HashMap<>();
  private Map<String, Flow> restFlowMap;
  private Map<String, Flow> restFlowMapUnwrapped;

  protected RoutingTable routingTable;

  private RamlHandler ramlHandler;
  private String configName;
  private List<FlowMapping> flowMappings;
  private ConfigurationComponentLocator locator;
  private ErrorTypeRepository errorTypeRepository;

  public FlowFinder(RamlHandler ramlHandler, String configName, ConfigurationComponentLocator locator,
                    List<FlowMapping> flowMappings) {
    this(ramlHandler, configName, locator, flowMappings, null);
  }

  public FlowFinder(RamlHandler ramlHandler, String configName, ConfigurationComponentLocator locator,
                    List<FlowMapping> flowMappings, ErrorTypeRepository errorTypeRepository) {
    this.ramlHandler = ramlHandler;
    this.configName = configName;
    this.flowMappings = flowMappings;
    this.locator = locator;
    this.errorTypeRepository = errorTypeRepository;
    initializeRestFlowMap();
    loadRoutingTable();
  }

  protected void initializeRestFlowMap() {
    final IRaml api = ramlHandler.getApi();
    flattenResourceTree(api.getResources(), api.getVersion());

    if (restFlowMap == null) {
      restFlowMap = new HashMap<>();

      List<Flow> flows = getFlows();

      // init flows by convention
      for (Flow flow : flows) {
        String key = getRestFlowKey(flow.getName());
        if (key != null) {
          restFlowMap.put(key, flow);
        }
      }

      //// init flow mappings
      for (FlowMapping mapping : flowMappings) {
        for (Flow flow : flows) {
          if (flow.getName().equals(mapping.getFlowRef())) {
            mapping.setFlow(flow);
            restFlowMap.put(mapping.getKey(), mapping.getFlow());
          }
        }
      }

      logMissingMappings(api.getVersion());

      restFlowMapUnwrapped = new HashMap<>(restFlowMap);
    }
  }

  private List<Flow> getFlows() {
    return getFlowsList(locator);
  }

  private void flattenResourceTree(Map<String, IResource> resources, String version) {
    for (IResource resource : resources.values()) {
      flatResourceTree.put(resource.getResolvedUri(version), resource);
      if (resource.getResources() != null) {
        flattenResourceTree(resource.getResources(), version);
      }
    }
  }

  public Map<String, Flow> getRawRestFlowMap() {
    return restFlowMap;
  }

  /**
   * validates if name is a valid router flow name according to the following pattern:
   * method:\resource[:content-type][:config-name]
   *
   * @param name to be validated
   * @return the name with the config-name stripped or null if it is not a router flow
   */
  private String getRestFlowKey(String name) {
    final String[] validMethods = {"get", "put", "post", "delete", "head", "patch", "options"};

    final String[] coords = FlowName.decode(name).split(FLOW_NAME_SEPARATOR);

    if (coords.length < 2)
      return null;

    final String method = coords[0];
    final String resource = coords[1];

    if (coords.length > 4 ||
        !Arrays.asList(validMethods).contains(method) ||
        !resource.startsWith(URL_RESOURCE_SEPARATOR)) {
      return null;
    }

    if (coords.length == 4) {
      if (coords[3].equals(configName)) {
        final String contentType = coords[2];
        return validateRestFlowKeyAgainstApi(method, resource, contentType);
      }
      return null;
    }

    if (coords.length == 3) {
      if (!coords[2].equals(configName)) {
        final String contentType = coords[2];
        return validateRestFlowKeyAgainstApi(method, resource, contentType);
      }
    }

    return validateRestFlowKeyAgainstApi(method, resource);
  }

  private String validateRestFlowKeyAgainstApi(String... coords) {
    String method = coords[0];
    String resource = coords[1];
    String type = coords.length == 3 ? coords[2] : null;
    String key = String.format("%s:%s", method, resource);

    if (type != null) {
      key = key + ":" + type;
    }

    IResource apiResource = flatResourceTree.get(resource);
    if (apiResource != null) {
      IAction action = apiResource.getAction(method);
      if (action != null) {
        if (type == null)
          return key;
        if (!action.hasBody() || action.getBody().entrySet().stream().anyMatch(v -> v.getKey().contains(type))) {
          return key;
        }
      }
    }

    logger.warn(String.format("Flow named \"%s\" does not match any RAML descriptor resource", key));
    return null;
  }

  private void logMissingMappings(String version) {
    for (IResource resource : flatResourceTree.values()) {
      String fullResource = resource.getResolvedUri(version);
      for (IAction action : resource.getActions().values()) {
        String method = action.getType().name().toLowerCase();
        String key = method + ":" + fullResource;
        if (restFlowMap.get(key) != null) {
          continue;
        }
        if (action.hasBody()) {
          for (String contentType : action.getBody().keySet()) {
            if (restFlowMap.get(key + ":" + getMediaType(contentType)) == null) {
              logger.warn(String.format("Action-Resource-ContentType triplet has no implementation -> %s:%s:%s ",
                                        method, fullResource, getMediaType(contentType)));
            }
          }
        } else {
          logger.warn(String.format("Action-Resource pair has no implementation -> %s:%s ",
                                    method, fullResource));
        }
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("==== RestFlows defined:");
      for (String key : restFlowMap.keySet()) {
        logger.debug("\t\t" + key);
      }
    }
  }

  private void loadRoutingTable() {
    if (routingTable == null) {
      routingTable = new RoutingTable(ramlHandler.getApi());
    }

  }


  public Flow getFlow(IResource resource, String method, String contentType) throws UnsupportedMediaTypeException {
    String baseKey = method + ":" + resource.getResolvedUri(ramlHandler.getApi().getVersion());
    Map<String, Flow> rawRestFlowMap = getRawRestFlowMap();
    Flow flow = rawRestFlowMap.get(baseKey + ":" + contentType);
    if (flow == null) {
      flow = rawRestFlowMap.get(baseKey);
      if (flow == null) {
        if (isFlowDeclaredWithDifferentMediaType(rawRestFlowMap, baseKey)) {
          throw throwErrorType(new UnsupportedMediaTypeException(), errorTypeRepository);
        } else {
          throw throwErrorType(new NotImplementedException(), errorTypeRepository);
        }
      }
    }
    return flow;
  }

  public IResource getResource(URIPattern uriPattern) {
    return routingTable.getResource(uriPattern);
  }

  private boolean isFlowDeclaredWithDifferentMediaType(Map<String, Flow> map, String baseKey) {
    for (String flowName : map.keySet()) {
      String[] split = flowName.split(":");
      String methodAndResoruce = split[0] + ":" + split[1];
      if (methodAndResoruce.equals(baseKey))
        return true;
    }
    return false;
  }

  public URIPattern findBestMatch(URIResolver resolver) {
    return resolver.find(routingTable.keySet(), URIResolver.MatchRule.BEST_MATCH);
  }
}
