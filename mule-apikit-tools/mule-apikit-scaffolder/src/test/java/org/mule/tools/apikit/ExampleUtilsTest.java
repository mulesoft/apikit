/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.apikit;

import org.junit.Test;
import org.mule.tools.apikit.misc.ExampleUtils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ExampleUtilsTest {

  @Test
  public void simpleStringWithoutQuotesIsNotAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("Hello"), is(false));
  }

  @Test
  public void singleStringIsAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("\"Hello\""), is(true));
  }

  @Test
  public void simpleNumberIsValidJSON() {
    assertThat(ExampleUtils.isValidJSON("23"), is(true));
  }

  @Test
  public void simplePairIsAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("\"game\" : \"League Of Legends\""), is(true));
  }

  @Test
  public void simplePairIsNotAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("game : \"League Of Legends\""), is(false));
  }

  @Test
  public void objectIsAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("{ \"game\" : \"League Of Legends\" }"), is(true));
  }

  @Test
  public void emptyObjectIsAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("{ }"), is(true));
  }

  @Test
  public void nullIsAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("null"), is(true));
  }

  @Test
  public void simpleBooleanIsAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("true"), is(true));
    assertThat(ExampleUtils.isValidJSON("false"), is(true));
  }


  @Test
  public void objectIsNotAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("{ \"game\" : \"League Of Legends\" "), is(false));
  }

  @Test
  public void singleArrayIsAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("[ 1, 2, 3, 14, \"Hello\", \"Hello\" ]"), is(true));
  }

  @Test
  public void emptyArrayIsAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("[]"), is(true));
  }

  @Test
  public void simpleArrayIsNotAValidJSON() {
    assertThat(ExampleUtils.isValidJSON("[ 1, 2, 3, 14, \"Hello\", \"Hello\""), is(false));
  }

  @Test
  public void getSimpleYamlKeyValueAsJSON() {
    assertThat(ExampleUtils.getExampleAsJSONIfNeeded("name: Mandy"), is("{\"name\":\"Mandy\"}"));
  }

  @Test
  public void getSimpleYamlStringAsJSON() {
    assertThat(ExampleUtils.getExampleAsJSONIfNeeded("Mandy"), is("\"Mandy\""));
  }

  @Test
  public void getSimpleYamlArrayAsJSON() {
    assertThat(ExampleUtils.getExampleAsJSONIfNeeded("- One\n- Two\n- Three"), is("[\"One\",\"Two\",\"Three\"]"));
  }

  @Test
  public void getNestedYamlArrayAsJSON() {
    assertThat(ExampleUtils.getExampleAsJSONIfNeeded(
                                                     "              books:\n" +
                                                         "                - title: In Cold Blood\n" +
                                                         "                  author: Truman Capote\n" +
                                                         "                  year: 1966\n" +
                                                         "                - title: El Salvaje\n" +
                                                         "                  author: Guillermo Arriaga\n" +
                                                         "                  year: 2016"),
               is("{\"books\":[{\"title\":\"In Cold Blood\",\"author\":\"Truman Capote\",\"year\":1966},{\"title\":\"El Salvaje\",\"author\":\"Guillermo Arriaga\",\"year\":2016}]}"));
  }
}
