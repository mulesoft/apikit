/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.module.apikit.exception.BadRequestException;
import org.mule.raml.implv2.v10.model.MimeTypeImpl;

public class BodyValidatorTestCase {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Test
  public void validateBodyIsXmlWithXmlContentType() throws Exception {
    expected.expect(BadRequestException.class);
    expected.expectMessage("Expected XML body");
    BodyValidator bodyValidator = new BodyValidator(new MimeTypeImpl(null));
    bodyValidator.validateSchemaV2( false, "{\"test\" : \"test\"}");
  }

  @Test
  public void validateBodyIsJSONWithJSONContentType() throws Exception {
    expected.expect(BadRequestException.class);
    expected.expectMessage("Expected JSON body");
    BodyValidator bodyValidator = new BodyValidator(new MimeTypeImpl(null));
    bodyValidator.validateSchemaV2(true, "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
        "<data><type>pinViewTokens</type> <attributes><cardNumber>4539799524650412</cardNumber> </attributes></data>");
  }

}
