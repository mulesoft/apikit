/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.apikit.api.uri;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.mule.module.apikit.uri.Matchable;
import org.mule.module.apikit.uri.Token;
import org.mule.module.apikit.uri.TokenLiteral;
import org.mule.module.apikit.uri.URICoder;
import org.mule.module.apikit.uri.URITemplate;
import org.mule.module.apikit.uri.URITemplateSyntaxException;


/**
 * A URI Pattern for matching URI following the same regular structure.
 * <p/>
 * <p/>
 * Instances of this class implement the PageSeeder URL pattern as defined by the "PageSeeder URI
 * Templates" document.
 * <p/>
 * <p/>
 * A PageSeeder URI Pattern follows the URI syntax defined for URI templates but must only contain
 * matchable tokens.
 *
 * @author Christophe Lauret
 * @version 27 October 2009
 */
public class URIPattern extends URITemplate implements Matchable {

  private static final Set<Character> ESCAPE_CHARS = new HashSet<Character>(Arrays.asList('/', '{', '}'));

  /**
   * The regular expression pattern for matching URIs to this URI Pattern.
   */
  private Pattern _pattern;

  /**
   * The score for this pattern, the length of the literal text.
   */
  private int _score = -1;

  /**
   * Creates a new URI Pattern instance from the specified URI template string.
   *
   * @param template The string following the URI template syntax.
   * @throws URITemplateSyntaxException If the string provided does not follow the proper syntax.
   */
  public URIPattern(String template, boolean encode) throws IllegalArgumentException {
    super(encode ? URICoder.encode(template, ESCAPE_CHARS) : template);
    if (!isMatchable(this)) {
      throw new IllegalArgumentException(
                                         "Cannot create a URL pattern containing non-matchable tokens.");
    }
    this._pattern = computePattern(tokens());
  }

  public URIPattern(String template) throws IllegalArgumentException {
    this(template, true);
  }

  /**
   * Creates a new URI Pattern instance from an existing URI Template.
   *
   * @param template The URI template to generate the pattern from.
   * @throws URITemplateSyntaxException If the string provided does not follow the proper syntax.
   */
  public URIPattern(URITemplate template) throws IllegalArgumentException {
    super(template != null ? template.toString() : "");
    if (template == null) {
      throw new NullPointerException("Cannot create a URL pattern with a null template");
    }
    if (!isMatchable(template)) {
      throw new IllegalArgumentException(
                                         "Cannot create a URL pattern from template containing non-matchable tokens.");
    }
    this._pattern = computePattern(tokens());
  }

  /**
   * Indicates whether the given URI template can be used to construct a new URI Pattern instance.
   * <p/>
   * <p/>
   * A template is matchable only if all its components are matchable tokens, that is the token
   * implements the {@link Matchable} interface.
   *
   * @param template The template to test.
   * @return <code>true</code> if the template is matchable; <code>false</code> otherwise.
   */
  public static boolean isMatchable(URITemplate template) {
    // return false if any non-matchable token is found.
    for (Token t : template.tokens()) {
      if (!(t instanceof Matchable)) {
        return false;
      }
    }
    // all tokens are matchable at this point.
    return true;
  }

  /**
   * Indicates whether this URI Pattern matches the specified URL.
   *
   * @param uri The URI to test.
   * @return <code>true</code> if this URI Pattern matches this
   */
  public boolean match(String uri) {
    return this._pattern.matcher(uri).matches();
  }

  /**
   * Returns the regular expression pattern corresponding to this URI pattern.
   *
   * @return The regular expression pattern corresponding to this URI pattern.
   */
  public Pattern pattern() {
    return this._pattern;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    // use the original regex pattern string because the Pattern object's hashCode
    return 7 * this._pattern.pattern().hashCode() + 3 * this.toString().hashCode() + 31;
  }

  /**
   * Returns the score for this URI pattern.
   * <p/>
   * The score corresponds to the length of literal content.
   *
   * @return the score for this URI pattern.
   */
  protected int score() {
    if (this._score < 0) {
      this._score = computeScore(this.tokens());
    }
    return this._score;
  }

  // private helpers ----------------------------------------------------------

  /**
   * Compute the Regular Expression pattern for this URI Pattern.
   * <p/>
   * Important note: the regular expression contain the same number of capturing groups as the
   * number of token to facilitate the resolve process.
   *
   * @return The regex Pattern instance corresponding to this URI pattern.
   */
  private Pattern computePattern(List<Token> tokens) {
    StringBuffer p = new StringBuffer();
    for (Token t : tokens) {
      Matchable mt = (Matchable) t;
      // wrap each token in a capturing group to facilitate the resolve process.
      p.append('(');
      p.append(mt.pattern());
      p.append(')');
    }
    return Pattern.compile(p.toString());
  }

  /**
   * Compute the score from the specified tokens.
   * <p/>
   * The score is the sum of the length of each literal token.
   *
   * @return The score from the specified tokens.
   */
  private int computeScore(List<Token> tokens) {
    int score = 0;
    for (Token t : tokens) {
      if (t instanceof TokenLiteral) {
        score += t.expression().length();
      }
    }
    return score;
  }

}
