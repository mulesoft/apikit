/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit.output.scopes;

import org.mule.tools.apikit.misc.APIKitTools;
import org.mule.tools.apikit.model.APIKitConfig;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;

public class APIKitConfigScope implements Scope {

  private final Element mule;
  private final APIKitConfig config;
  private Integer index;

  public APIKitConfigScope(APIKitConfig config, Element mule, Integer index) {
    this.mule = mule;
    this.config = config;
    this.index = index;
  }

  @Override
  public Element generate() {
    Element config = null;
    if (this.config != null) {
      config = new Element(APIKitConfig.ELEMENT_NAME,
                           APIKitTools.API_KIT_NAMESPACE.getNamespace());

      if (!StringUtils.isEmpty(this.config.getName())) {
        config.setAttribute(APIKitConfig.NAME_ATTRIBUTE, this.config.getName());
      }

      if (this.config.getApi() != null)
        config.setAttribute(APIKitConfig.API_ATTRIBUTE, this.config.getApi());
      if (this.config.getRaml() != null)
        config.setAttribute(APIKitConfig.RAML_ATTRIBUTE, this.config.getRaml());
      if (this.config.isExtensionEnabled() != null) {
        config.setAttribute(APIKitConfig.EXTENSION_ENABLED_ATTRIBUTE, String.valueOf(this.config.isExtensionEnabled()));
      }
      if (this.config.getOutboundHeadersMapName() != null) {
        config.setAttribute(APIKitConfig.OUTBOUND_HEADERS_MAP_ATTRIBUTE, this.config.getOutboundHeadersMapName());
      }
      if (this.config.getHttpStatusVarName() != null) {
        config.setAttribute(APIKitConfig.HTTP_STATUS_VAR_ATTRIBUTE, this.config.getHttpStatusVarName());
      }

      if (index != null)
        mule.addContent(index, config);
      else
        mule.addContent(config);
    }
    return config;
  }
}
