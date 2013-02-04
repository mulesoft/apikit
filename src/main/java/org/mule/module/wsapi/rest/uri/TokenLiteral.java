/**
 * Mule Rest Module
 *
 * Copyright 2011-2012 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * This software is protected under international copyright law. All use of this software is
 * subject to MuleSoft's Master Subscription Agreement (or other master license agreement)
 * separately entered into in writing between you and MuleSoft. If such an agreement is not
 * in place, you may not use the software.
 */

package org.mule.module.wsapi.rest.uri;

import java.util.Map;
import java.util.regex.Pattern;


/**
 * A URI token corresponding to the literal text part of the URI template.
 * 
 * <p>Literal text remains identical during the expansion process (parameters are ignored).
 * 
 * <p>Literal text tokens only match text that is equal.
 * 
 * <p>The expression for a literal token does contain curly brackets.
 * 
 * @author Christophe Lauret
 * @version 9 February 2009
 */
public class TokenLiteral extends TokenBase implements Token, Matchable
{

  /**
   * Creates a new literal text token.
   * 
   * @param text The text corresponding to this URI token.
   * 
   * @throws NullPointerException If the specified text is <code>null</code>.
   */
  public TokenLiteral(String text) throws NullPointerException {
    super(text);
  }

  /**
   * {@inheritDoc}
   */
  public String expand(Parameters parameters) {
    return this.expression();
  }

  /**
   * {@inheritDoc}
   */
  public boolean match(String part) {
    return this.expression().equals(part);
  }

  /**
   * {@inheritDoc}
   */
  public Pattern pattern() {
    return Pattern.compile(Pattern.quote(expression()));
  }

  /**
   * {@inheritDoc}
   * 
   * By definition, no variable in this token. This method does nothing and always
   * returns <code>true</code>.
   */
  public boolean resolve(String expanded, Map<Variable, Object> values) {
    // nothing to resolve - the operation is always successful.
    return true;
  }

}
