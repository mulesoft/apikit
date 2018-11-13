/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.metadata.utils;

import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.util.stream.Collectors.toMap;

public class TestDataProvider {

  final protected File input;
  final protected Map<String, String> expectedMap;
  protected Map<String, String> currentMap;
  final protected String name;

  public TestDataProvider(File input, Map<String, String> expectedMap, String name) {
    this.input = input;
    this.expectedMap = expectedMap;
    this.name = name;
  }

  @Rule
  public TestWatcher watchman = new TestWatcher() {

    @Override
    protected void failed(Throwable e, Description description) {
      updateTests();
    }

    private void updateTests() {
      if (System.getProperty("updateTests") != null) {
        try {
          // Remove *.out
          final String inputPath = input.getPath().replace("target/test-classes", "src/test/resources");
          final Path basePath = Paths.get(inputPath).getParent();

          //noinspection ResultOfMethodCallIgnored
          Files.walk(basePath, 1)
              .map(Path::toFile)
              .filter(f -> f.isFile() && f.getName().endsWith(".out"))
              .forEach(File::delete);

          // Write golden files  with current values
          currentMap.forEach((name, content) -> {
            try {
              Files.write(basePath.resolve(name), content.getBytes("UTF-8"));
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  };

  public static Collection<Object[]> getData(URI baseFolder, String inputFileName, Pattern outputPattern) throws IOException {
    return collectParameters(baseFolder, inputFileName, outputPattern);
  }

  private static class TestFileVisitor extends SimpleFileVisitor<Path> {

    private final String inputName;
    private final Pattern outputPattern;

    private final List<File> outputFiles = new ArrayList<>();
    private File inputFile = null;

    private List<Object[]> getParameters() {
      return parameters;
    }

    private final List<Object[]> parameters = new ArrayList<>();

    private TestFileVisitor(String inputName, Pattern outputPattern) {
      this.inputName = inputName;
      this.outputPattern = outputPattern;
    }

    static TestFileVisitor create(String input, Pattern outputPattern) {
      return new TestFileVisitor(input, outputPattern);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      final File candidate = file.toFile();
      final String name = candidate.getName();
      if (name.equals(inputName)) {
        inputFile = candidate;
      } else if (outputPattern.matcher(name).matches()) {
        outputFiles.add(candidate);
      }

      return CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path scenarioPath, BasicFileAttributes attrs) throws IOException {
      if (!scenarioPath.endsWith("include")) {
        if (!scenarioPath.toString().contains("exchange")) {
          outputFiles.clear();
          inputFile = null;
        }
      }

      return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      final File scenarioDir = dir.toFile();

      if (inputFile != null && !dir.endsWith("include") && !dir.toString().contains("exchange")) {
        final Map<String, String> outputMap = outputFiles.stream().collect(toMap(File::getName, TestDataProvider::readFile));
        parameters.add(new Object[] {inputFile, outputMap, scenarioDir.getName()});
      }

      return CONTINUE;
    }
  }

  private static String readFile(File file) {
    try {
      return new String(Files.readAllBytes(Paths.get(file.toURI())));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<Object[]> collectParameters(URI folderPath, String inputFileName, Pattern outputPattern)
      throws IOException {
    final TestFileVisitor visitor = TestFileVisitor.create(inputFileName, outputPattern);
    Files.walkFileTree(Paths.get(folderPath), visitor);

    return visitor.getParameters();
  }

}
