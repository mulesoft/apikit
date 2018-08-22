package org.mule.tools.apikit;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.apikit.misc.FileListUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ParserTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  private FileListUtils fileListUtils = new FileListUtils();

  @Before
  public void setUp() throws IOException {
    folder.newFolder("parser");
    createFile("amf-only.raml");
    createFile("raml-parser-only.raml");
    createFile("failing-api.raml");
  }

  private File createFile(String s) throws IOException {
    File file = folder.newFile(s);
    file.createNewFile();
    InputStream resourceAsStream = ScaffolderTest.class.getClassLoader().getResourceAsStream(s);
    IOUtils.copy(resourceAsStream,
                 new FileOutputStream(file));
    return file;
  }



}
