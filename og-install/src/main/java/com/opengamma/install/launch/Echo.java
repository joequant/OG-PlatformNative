/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.launch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Echos the command line arguments to a file
 */
public class Echo {

  private static void echo(final String text) {
    try {
      PrintStream out = new PrintStream(new FileOutputStream("echo.log", true));
      out.println(text);
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) { //CSIGNORE
    echo("Echoing " + args.length + " parameters");
    for (final String arg : args) {
      echo(arg);
    }
    echo("Done");
  }

}
