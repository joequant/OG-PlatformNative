/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import static org.apache.commons.io.IOUtils.toByteArray;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CustomClassLoader extends ClassLoader {

  private static final Logger s_logger = LoggerFactory.getLogger(CustomClassLoader.class);

  public CustomClassLoader() {
    super(CustomClassLoader.class.getClassLoader());
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    s_logger.error("Call to loadClass {}/{}", name, resolve);
    // respect the java.* packages.
    if (name.startsWith("java.")) {
      return super.loadClass(name, resolve);
    } else {
      // see if we have already loaded the class.
      Class<?> c = findLoadedClass(name);
      if (c != null) {
        return c;
      }
      // the class is not loaded yet.  Since the parent class loader has all of the
      // definitions that we need, we can use it as our source for classes.
      try (InputStream in = getParent().getResourceAsStream(name.replaceAll("\\.", "/") + ".class")) {
        // get the input stream, throwing ClassNotFound if there is no resource.
        if (in == null) {
          throw new ClassNotFoundException("Could not find " + name);
        }
        // read all of the bytes and define the class.
        final byte[] cBytes = toByteArray(in);
        c = defineClass(name, cBytes, 0, cBytes.length);
        if (resolve) {
          resolveClass(c);
        }
        s_logger.error("Loaded {}", c.getName());
        return c;
      } catch (IOException e) {
        throw new ClassNotFoundException("Could not load " + name, e);
      }
    }
  }

}
