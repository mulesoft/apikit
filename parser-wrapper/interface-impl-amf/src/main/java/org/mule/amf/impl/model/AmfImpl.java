package org.mule.amf.impl.model;

import amf.client.model.domain.WebApi;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.model.IResource;
import org.mule.raml.interfaces.model.ISecurityScheme;
import org.mule.raml.interfaces.model.ITemplate;
import org.mule.raml.interfaces.model.parameter.IParameter;

import static java.util.Collections.emptyMap;

public class AmfImpl implements IRaml {

    private WebApi webApi;

  public AmfImpl(final WebApi webApi) {
        this.webApi = webApi;
  }
  
  @Override
  public IResource getResource(String path) {
    return null;
  }

  @Override
  public Map<String, String> getConsolidatedSchemas() {
    return null;
  }

  @Override
  public Map<String, Object> getCompiledSchemas() {
    return null;
  }

  @Override
  public String getBaseUri() {
    return null;
  }

  @Override
  public Map<String, IResource> getResources() {
      Map<String, IResource> map = new LinkedHashMap<>();
      List<Resource> resources = api.resources();
      for (Resource resource : resources) {
          map.put(resource.relativeUri().value(), new ResourceImpl(resource));
      }
      return map;
  }

  @Override
  public String getVersion() {
    return null;
  }

  @Override
  public Map<String, IParameter> getBaseUriParameters() {
    return null;
  }

  @Override
  public List<Map<String, ISecurityScheme>> getSecuritySchemes() {
    return null;
  }

  @Override
  public List<Map<String, ITemplate>> getTraits() {
    return null;
  }

  @Override
  public String getUri() {
    return null;
  }

  @Override
  public List<Map<String, String>> getSchemas() {
    return null;
  }

  @Override
  public Object getInstance() {
    return null;
  }

  @Override
  public void cleanBaseUriParameters() {

  }

  @Override
  public void injectTrait(String name) {

  }

  @Override
  public void injectSecurityScheme(Map<String, ISecurityScheme> securityScheme) {

  }
}
