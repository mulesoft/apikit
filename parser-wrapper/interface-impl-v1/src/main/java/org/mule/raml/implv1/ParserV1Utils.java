/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.raml.implv1;

import org.mule.raml.implv1.parser.visitor.RamlDocumentBuilderImpl;
import org.mule.raml.implv1.parser.visitor.RamlValidationServiceImpl;
import org.mule.raml.interfaces.model.IRaml;
import org.mule.raml.interfaces.parser.rule.IValidationResult;
import org.mule.raml.interfaces.parser.visitor.IRamlDocumentBuilder;
import org.mule.raml.interfaces.parser.visitor.IRamlValidationService;
import org.raml.parser.loader.ResourceLoader;

import java.util.ArrayList;
import java.util.List;

public class ParserV1Utils {

  public static List<String> validate(ResourceLoader resourceLoader, String rootFileName, String resourceContent) {
    List<String> errorsList = new ArrayList<>();
    IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl(resourceLoader);
    IRamlValidationService validationService = new RamlValidationServiceImpl(ramlDocumentBuilder);
    IRamlValidationService result = validationService.validate(resourceContent, rootFileName);
    for (IValidationResult validationResult : result.getErrors()) {
      errorsList.add(validationResult.getMessage());
    }
    return errorsList;
  }


  public static List<String> validate(String resourceFolder, String rootFileName, String resourceContent) {
    List<String> errorsList = new ArrayList<>();
    IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl();
    ramlDocumentBuilder.addPathLookupFirst(resourceFolder);
    IRamlValidationService validationService = new RamlValidationServiceImpl(ramlDocumentBuilder);
    IRamlValidationService result = validationService.validate(resourceContent, rootFileName);
    for (IValidationResult validationResult : result.getErrors()) {
      errorsList.add(validationResult.getMessage());
    }
    return errorsList;
  }

  public static IRaml build(String content, String resourceFolder, String rootFileName) {
    IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl();
    ramlDocumentBuilder.addPathLookupFirst(resourceFolder);
    return ramlDocumentBuilder.build(content, rootFileName);
  }

  public static IRaml build(String content, ResourceLoader resourceLoader, String rootFileName) {
    IRamlDocumentBuilder ramlDocumentBuilder = new RamlDocumentBuilderImpl(resourceLoader);
    return ramlDocumentBuilder.build(content, rootFileName);
  }
}
