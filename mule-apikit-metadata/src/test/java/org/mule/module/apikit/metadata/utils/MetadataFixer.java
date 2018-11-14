/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.util.Pair;

public class MetadataFixer {

  private static final String FORMAT = "%s : %s";
  private static final String FORMAT_ARRAY = "%s : [%s]";
  public static final String ENUM = "@enum(";

  private MetadataFixer() {}

  public static String normalizeEnums(final String value) {
    return value.contains(ENUM) ? normalize(value) : value;
  }

  private static String normalize(final String value) {
    final String[] lines = value.split("\n");

    final ArrayList<String> result = new ArrayList<>();
    for (String line : lines) {
      result.add(line.contains(ENUM) ? normalizeEnumLine(line) : line);
    }

    final String newValue = result.stream()
        .collect(Collectors.joining("\n"));
    return newValue;
  }

  private static String normalizeEnumLine(final String line) {

    final Pair<String, String> keyValue = keyValue(line);

    String key = keyValue.getKey();
    String value = keyValue.getValue();

    final boolean endsWithComma = line.trim().endsWith(",");
    final boolean isArray = isArray(value);

    if (endsWithComma)
      value = value.substring(0, value.lastIndexOf(','));
    if (isArray)
      value = arrayValue(value);

    value = fixEnumValue(value);

    final String format = isArray ? FORMAT_ARRAY : FORMAT;
    final String newLine = String.format(format, key, value) + (endsWithComma ? ", " : "");


    return newLine;
  }

  private static Pair<String, String> keyValue(final String value) {
    final int i = value.indexOf(" : ");
    return new Pair<>(value.substring(0, i), value.substring(i + 3));
  }

  private static String fixEnumValue(final String value) {
    final int i = value.indexOf(" & ");

    // Old Parser don't need fix it;
    if (i == -1)
      return value;

    String op1 = value.substring(0, i);
    String op2 = value.substring(i + 3);

    return op1.contains(ENUM) ? op1 + " & " + op2 : op2 + " & " + op1;
  }

  private static String arrayValue(final String value) {
    return value.substring(value.indexOf('[') + 1, value.lastIndexOf(']'));
  }

  private static boolean isArray(final String value) {
    return value.trim().startsWith("[");
  }
}
