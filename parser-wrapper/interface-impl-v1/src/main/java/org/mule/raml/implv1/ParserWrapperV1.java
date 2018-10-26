/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1;

import static java.util.stream.Collectors.toList;
import static org.mule.raml.interfaces.model.ApiVendor.RAML_08;
import static org.raml.parser.rule.ValidationResult.Level.ERROR;
import static org.raml.parser.rule.ValidationResult.Level.WARN;
import static org.raml.parser.rule.ValidationResult.UNKNOWN;

import org.mule.raml.implv1.injector.RamlUpdater;
import org.mule.raml.implv1.loader.ApiSyncResourceLoader;
import org.mule.raml.implv1.model.RamlImplV1;
import org.mule.raml.implv1.parser.rule.ValidationResultImpl;
import org.mule.raml.interfaces.ParserWrapper;
import org.mule.raml.interfaces.injector.IRamlUpdater;
import org.mule.raml.interfaces.model.ApiVendor;
import org.mule.raml.interfaces.model.IRaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.SerializationUtils;
import org.mule.raml.interfaces.parser.rule.DefaultValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationReport;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.raml.emitter.RamlEmitter;
import org.raml.model.Action;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.loader.DefaultResourceLoader;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserWrapperV1 implements ParserWrapper {

  private static final Logger logger = LoggerFactory.getLogger(ParserWrapperV1.class);

  private final String ramlPath;
  private final ResourceLoader resourceLoader;
  private Raml baseApi; //original api to clone

  public ParserWrapperV1(String ramlPath) {
    this.ramlPath = ramlPath;
    this.resourceLoader = new CompositeResourceLoader(new DefaultResourceLoader(), new ApiSyncResourceLoader(ramlPath));;
  }

  @Override
  public ApiVendor getApiVendor() {
    return RAML_08;
  }

  @Override
  public void validate() {
    List<ValidationResult> results = RamlValidationService.createDefault(resourceLoader).validate(ramlPath);
    List<ValidationResult> errors = ValidationResult.getLevel(ERROR, results);
    if (!errors.isEmpty()) {
      String msg = aggregateMessages(errors, "Invalid API descriptor -- errors found: ");
      throw new RuntimeException(msg);
    }
    List<ValidationResult> warnings = ValidationResult.getLevel(WARN, results);
    if (!warnings.isEmpty()) {
      logger.warn(aggregateMessages(warnings, "API descriptor Warnings -- warnings found: "));
    }
  }

  @Override
  public IValidationReport validationReport() {
    final List<ValidationResult> results = RamlValidationService.createDefault(resourceLoader).validate(ramlPath);
    final List<IValidationResult> validationResults = results.stream().map(ValidationResultImpl::new).collect(toList());
    return new DefaultValidationReport(validationResults);
  }

  private String aggregateMessages(List<ValidationResult> results, String header) {
    StringBuilder sb = new StringBuilder();
    sb.append(header).append(results.size()).append("\n\n");
    for (ValidationResult result : results) {
      sb.append(result.getMessage()).append(" -- ");
      sb.append(" file: ");
      sb.append(result.getIncludeName() != null ? result.getIncludeName() : ramlPath);
      if (result.getLine() != UNKNOWN) {
        sb.append(" -- line ");
        sb.append(result.getLine());
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  @Override
  public IRaml build() {
    RamlDocumentBuilder builder = new RamlDocumentBuilder(resourceLoader);
    Raml api = builder.build(ramlPath);
    return new RamlImplV1(api);
  }

  @Override
  public String dump(String ramlContent, IRaml api, String oldSchemeHostPort, String newSchemeHostPort) {
    String newBaseUri = null;
    if (!oldSchemeHostPort.equals(newSchemeHostPort)) {
      newBaseUri = api.getBaseUri().replace(oldSchemeHostPort, newSchemeHostPort);
    }
    return dump(api, newBaseUri);
  }

  @Override
  public String dump(IRaml api, String newBaseUri) {
    Raml ramlImpl = getRamlImpl(api);
    if (newBaseUri != null) {
      Raml clone = shallowCloneRaml(ramlImpl);
      clone.setBaseUri(newBaseUri);
      ramlImpl = clone;
    }
    return new RamlEmitter().dump(ramlImpl);
  }

  private Raml shallowCloneRaml(Raml source) {
    try {
      return (Raml) BeanUtils.cloneBean(source);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public IRamlUpdater getRamlUpdater(IRaml api) {
    if (baseApi == null) {
      baseApi = deepCloneRaml(getRamlImpl(api));
    }
    return new RamlUpdater(deepCloneRaml(baseApi));
  }

  @Override
  public void updateBaseUri(IRaml api, String baseUri) {
    Raml ramlImpl = getRamlImpl(api);
    ramlImpl.setBaseUri(baseUri);
    cleanBaseUriParameters(ramlImpl);
  }

  @Override
  public InputStream fetchResource(String resource) {
    if (resourceLoader != null)
      return resourceLoader.fetchResource(resource);

    return null;
  }

  private void cleanBaseUriParameters(Raml ramlApi) {
    ramlApi.getBaseUriParameters().clear();
    cleanBaseUriParameters(ramlApi.getResources());
  }

  private void cleanBaseUriParameters(Map<String, Resource> resources) {
    for (Resource resource : resources.values()) {
      resource.getBaseUriParameters().clear();
      for (Action action : resource.getActions().values()) {
        action.getBaseUriParameters().clear();
      }
      if (!resource.getResources().isEmpty()) {
        cleanBaseUriParameters(resource.getResources());
      }
    }
  }

  private Raml getRamlImpl(IRaml api) {
    return ((RamlImplV1) api).getRaml();
  }

  private Raml deepCloneRaml(Raml source) {
    Raml target = (Raml) SerializationUtils.deserialize(SerializationUtils.serialize(source));
    copyCompiledSchemas(source, target);
    return target;
  }

  private void copyCompiledSchemas(Raml source, Raml target) {
    target.setCompiledSchemas(source.getCompiledSchemas());
  }


}
