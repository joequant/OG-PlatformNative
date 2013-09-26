/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextFactory;
import com.opengamma.language.install.ConfigureMain;
import com.opengamma.language.test.TestUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Entry point for the Language add-in, defining static methods for calling externally.
 */
public class Main {

  private static final Logger s_logger = LoggerFactory.getLogger(Main.class);

  private static LanguageSpringContext s_springContext;
  private static final ExecutorService s_executorService = Executors.newCachedThreadPool(new CustomizableThreadFactory("Client-"));

  private static int s_activeConnections;
  private static Thread s_stopping;

  /**
   * Sets a system property.
   * 
   * @param property key of the property to set, never null
   * @param value value to set, never null
   */
  private static boolean setProperty(final String property, final String value) {
    try {
      s_logger.debug("Setting system property {}={}", property, value);
      System.setProperty(property, value);
      return true;
    } catch (final Throwable t) {
      s_logger.error("Couldn't set property {}={}", property, value);
      s_logger.warn("Exception thrown", t);
      return false;
    }
  }

  /**
   * Updates the store that the service wrapper retrieves properties from that are passed to {@link #setProperty}.
   * 
   * @param property key of the property to set, never null
   * @param value value to set, or null to delete the property
   */
  private static native void writeProperty(final String property, final String value);

  /**
   * Debug entry point from the service wrapper tests.
   * 
   * @return true always
   */
  public static boolean svcTest() {
    s_logger.info("svcTest called");
    return true;
  }

  private static String exceptionToString(final Throwable e) {
    Throwable t = e;
    s_logger.warn("Exception thrown", t);
    while (t instanceof BeanCreationException) {
      t = t.getCause();
    }
    final String message = ((t != null) ? t : e).getMessage();
    s_logger.error("{}", message);
    return message;
  }

  /**
   * Entry point from the service wrapper - starts the service.
   * 
   * @return null if the service started properly, otherwise a string for display to the user describing why the stack wasn't started
   */
  public static String svcStart() {
    try {
      s_logger.info("Starting OpenGamma language integration service");
      s_springContext = new LanguageSpringContext();
      return null;
    } catch (final Throwable t) {
      return exceptionToString(t);
    }
  }

  /**
   * Entry point for a Java integration test that require a OG-Language stack session context (rather than a simulated one from {@link TestUtils} that contains suitable mocks).
   * <p>
   * This should not be called in a system running normally. Tests should also not call this directly, but via {@link TestUtils}.
   * <p>
   * 
   * @param userName the user name of the connection, not {@code null}
   * @param languageID the language identifier to return the context for, not {@code null}
   * @param debug true if the debugging components should be enabled for diagnostics, false to be representative of a real deployment
   * @return an uninitialized session context, not null
   */
  public static synchronized SessionContext createIntegrationTestSessionContext(final String userName, final String languageID, final boolean debug) {
    if (s_springContext == null) {
      final String startupFailed = svcStart();
      if (startupFailed != null) {
        throw new OpenGammaRuntimeException("Couldn't run integration test - " + startupFailed);
      }
    }
    final Pair<ClientFactory, SessionContextFactory> factories = s_springContext.getLanguageFactories(languageID);
    return factories.getSecond().createSessionContext(userName, debug);
  }

  /**
   * Entry point from the service wrapper - starts a connection handler for a given client.
   * 
   * @param userName the user name of the incoming connection
   * @param inputPipeName the pipe created for sending data from C++ to Java
   * @param outputPipeName the pipe created for sending data from Java to C++
   * @param languageID the identifier of the bound language. Language specific factories will be used if present, otherwise the default factories will be used.
   * @param debug true if the bound language is a debug build
   * @return null if the connection started okay, an error message otherwise
   */
  public static synchronized String svcAccept(final String userName, final String inputPipeName,
      final String outputPipeName, final String languageID, final boolean debug) {
    try {
      s_logger.info("Accepted {} connection from {}", languageID, userName);
      s_logger.debug("Using pipes IN:{} OUT:{}", inputPipeName, outputPipeName);
      final Pair<ClientFactory, SessionContextFactory> factories = s_springContext.getLanguageFactories(languageID);
      final SessionContext sessionContext = factories.getSecond().createSessionContext(userName, debug);
      final Client client = factories.getFirst().createClient(inputPipeName, outputPipeName, sessionContext);
      s_activeConnections++;
      s_executorService.submit(new Runnable() {
        @Override
        public void run() {
          client.run();
          s_logger.info("Session for {} disconnected", userName);
          clientDisconnected();
        }
      });
      return null;
    } catch (final Throwable t) {
      Client.failPipes(inputPipeName, outputPipeName);
      return exceptionToString(t);
    }
  }

  /**
   * Reports that a client has disconnected. The last client to disconnect will cause the service to terminate. The potential race between this and a pending call to {@link #svcAccept} is handled by
   * locking within the service wrapper.
   */
  private static synchronized void clientDisconnected() {
    if (--s_activeConnections == 0) {
      s_logger.info("Attempting to stop service on last client disconnect");
      notifyStop();
    } else {
      s_logger.info("{} clients still connected", s_activeConnections);
    }
  }

  /**
   * Entry point for the service wrapper - queries if there are no active clients.
   * 
   * @return true if there are no active clients
   */
  public static synchronized boolean svcIsStopped() {
    return s_activeConnections == 0;
  }

  /**
   * Requests the host process stop the service. This may be ignored if there is a pending request that has not reached {@link #svcAccept} yet, or if stopping on the last client disconnect is
   * disabled.
   */
  private static native void notifyStop();

  /**
   * Requests the host process halt the service. Use with caution. This should be used for serious issues that mean the existing instance is no longer viable; such as the back-end server no longer
   * being available or connected to a different data environment.
   * 
   * @param logMessage the message to report to the user through some mechanism as to why the Java stack has aborted
   */
  private static native void notifyHalt(String logMessage);

  /**
   * Deadlocks the calling thread against the pipe dispatch thread within the C++ layer. This is for testing error recovery by deliberately hanging the JVM. DO NOT CALL THIS FUNCTION UNLESS YOU WANT
   * THINGS TO BREAK.
   */
  public static native void notifyPause();

  private static synchronized void beginStop() {
    Thread stopping = s_stopping;
    s_stopping = Thread.currentThread();
    if (stopping != null) {
      throw new OpenGammaRuntimeException("Already stopped by " + stopping.getName());
    }
  }

  private static synchronized void forceStop() {
    if (s_stopping != null) {
      s_logger.info("Interrupting thread {} to stop", s_stopping.getName());
      s_stopping.interrupt();
    }
    s_stopping = Thread.currentThread();
  }

  /**
   * Entry point from the service wrapper - stops the service.
   * 
   * @return true if the service stopped cleanly
   */
  public static boolean svcStop() {
    try {
      s_logger.info("Waiting for client threads to stop");
      s_executorService.shutdown();
      beginStop();
      s_executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      s_logger.info("Stopping application context");
      s_springContext.stop();
      s_logger.info("OpenGamma Language Integration service stopped");
      return true;
    } catch (final Throwable t) {
      s_logger.error("Exception thrown", t);
      return false;
    }
  }

  public static void halt(final String logMessage) {
    forceStop();
    notifyHalt(logMessage);
  }

  /**
   * Entry point from the service wrapper to configure the application.
   * 
   * @return true if the configuration callback ran, false if there was a problem
   */
  public static boolean svcConfigure() {
    new ConfigureMain(new ConfigureMain.Callback() {

      @Override
      public void setProperty(final String property, final String value) {
        Main.writeProperty(property, value);
      }

      @Override
      public String getProperty(final String property) {
        return System.getProperty(property);
      }

    }).run();
    return true;
  }

}
