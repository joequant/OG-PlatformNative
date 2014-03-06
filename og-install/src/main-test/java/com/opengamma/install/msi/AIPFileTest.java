/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.msi;

import static org.testng.Assert.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests load/save of an AIP file. The files from the project are loaded and written back out - if they are the same the parser is assumed to be sound.
 */
@Test(groups = TestGroup.UNIT)
public class AIPFileTest {

  private static int read(final InputStream in) throws IOException {
    final int c = in.read();
    if (c != '\r') {
      return c;
    }
    if (c == 26) {
      return -1;
    } else {
      return in.read();
    }
  }

  private static void assertFilesEqual(final File a, final File b) throws IOException {
    final InputStream inA = new BufferedInputStream(new FileInputStream(a));
    try {
      final InputStream inB = new BufferedInputStream(new FileInputStream(b));
      try {
        int index = 0;
        int charA = read(inA);
        int charB = read(inB);
        while (charA == charB) {
          charA = read(inA);
          charB = read(inB);
          if ((charA == -1) && (charB == -1)) {
            return;
          }
          index++;
        }
        fail("Output file " + b + " does not match input file " + a + " at " + index + " (A='" + (char) charA + "', B='" + (char) charB + "')");
      } finally {
        inB.close();
      }
    } finally {
      inA.close();
    }
  }

  private void test(final String name) throws IOException {
    final File tmp = File.createTempFile("AIPFileTest", "aip");
    try {
      final File original = new File("../../src/main/ai/" + name + ".aip");
      final AIPFile aip = AIPFile.load(original);
      aip.save(tmp);
      assertFilesEqual(original, tmp);
    } finally {
      tmp.delete();
    }
  }

  public void testFoldersAip() throws IOException {
    test("Folders");
  }

  public void testTestA() throws IOException {
    test("TestA");
  }

  public void testTestB() throws IOException {
    test("TestB");
  }

  public void testUsers() throws IOException {
    test("Users");
  }

}
