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
    return validate(null, resourceLoader, rootFileName, resourceContent);
  }

  public static List<String> validate(String resourceFolder, String rootFileName, String resourceContent) {
    return validate(resourceFolder, null, rootFileName, resourceContent);
  }

  public static IRaml build(String content, String resourceFolder, String rootFileName) {
    return build(content, resourceFolder, null, rootFileName);
  }

  public static IRaml build(String content, ResourceLoader resourceLoader, String rootFileName) {
    return build(content, null, resourceLoader, rootFileName);
  }

  private static List<String> validate(String resourceFolder, ResourceLoader resourceLoader, String rootFileName,
                                       String resourceContent) {
    IRamlDocumentBuilder ramlDocumentBuilder = getIRamlDocumentBuilder(resourceFolder, resourceLoader);

    List<String> errorsList = new ArrayList<>();
    IRamlValidationService validationService = new RamlValidationServiceImpl(ramlDocumentBuilder);
    IRamlValidationService result = validationService.validate(resourceContent, rootFileName);
    for (IValidationResult validationResult : result.getErrors()) {
      errorsList.add(validationResult.getMessage());
    }
    return errorsList;
  }

  public static IRaml build(String content, String resourceFolder, ResourceLoader resourceLoader, String rootFileName) {
    IRamlDocumentBuilder ramlDocumentBuilder = getIRamlDocumentBuilder(resourceFolder, resourceLoader);

    return ramlDocumentBuilder.build(content, rootFileName);
  }

  private static IRamlDocumentBuilder getIRamlDocumentBuilder(String resourceFolder, ResourceLoader resourceLoader) {
    IRamlDocumentBuilder ramlDocumentBuilder;
    if (resourceLoader == null) {
      ramlDocumentBuilder = new RamlDocumentBuilderImpl();
    } else {
      ramlDocumentBuilder = new RamlDocumentBuilderImpl(resourceLoader);
    }

    if (resourceFolder != null) {
      ramlDocumentBuilder.addPathLookupFirst(resourceFolder);
    }
    return ramlDocumentBuilder;
  }

}
