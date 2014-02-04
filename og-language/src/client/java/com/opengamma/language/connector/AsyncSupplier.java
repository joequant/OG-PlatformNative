/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.lambdava.functions.Function1;

/**
 * Implementation of a {@link Supplier} that allows a caller to subscribe for notification when the value becomes available.
 * <p>
 * This pattern is made available to allow multi-threading during the Spring context initialization stage - beans which take a while to construct can be returned as an {@code AsyncSupplier} instance
 * so Spring can carry on with any other tasks that are possible until the real bean is finally needed.
 */
public abstract class AsyncSupplier<T> implements Supplier<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(AsyncSupplier.class);

  /**
   * Listener for notification when the value becomes available.
   */
  public interface Listener<T> {

    /**
     * Notifies of the value.
     * 
     * @param instance the instance produced
     * @param error any exception thrown, null if there was no error
     */
    void value(T instance, RuntimeException error);

  }

  /**
   * Holds any exception thrown by the underlying source.
   */
  private RuntimeException _error;

  /**
   * Holds any result returned by the underlying source.
   */
  private T _instance;

  /**
   * Holds any listeners that have been registered, or null if a result/error has been posted.
   */
  private Collection<Listener<T>> _listeners = Collections.emptySet();

  /**
   * Passes the result to any blocked callers to {@link #get} or listeners.
   * <p>
   * A sub-class is responsible for making sure this gets called, typically from a spawned thread or as the result of another supplier producing its value.
   */
  public final void post(final T instance, RuntimeException error) {
    if (instance == null) {
      if (error == null) {
        s_logger.debug("Null result available from {}", this);
      } else {
        s_logger.debug("Error available from {}", this);
      }
    } else {
      if (error != null) {
        // Can't set both instance and error
        throw new IllegalArgumentException();
      }
      s_logger.debug("Result available from {}", this);
    }
    final Collection<Listener<T>> listeners;
    synchronized (this) {
      assert _listeners != null;
      _instance = instance;
      _error = error;
      notifyAll();
      listeners = _listeners;
      _listeners = null;
    }
    if (!listeners.isEmpty()) {
      s_logger.info("Notifying listeners from {}", this);
      for (Listener<T> listener : listeners) {
        try {
          s_logger.debug("Notifying listener from {}", this);
          listener.value(instance, error);
        } catch (Throwable t) {
          s_logger.error("Couldn't notify listener from {} - {}", this, t);
          s_logger.warn("Caught exception", t);
        }
      }
    }
  }

  /**
   * Registers a listener for notification when the value becomes available. The listener is called immediately if the value is already available.
   * 
   * @param listener the listener to register, not null
   */
  public final void get(final Listener<T> listener) {
    T instance;
    RuntimeException error;
    synchronized (this) {
      if (_listeners != null) {
        s_logger.debug("Registering listener for {}", this);
        if (_listeners.isEmpty()) {
          _listeners = new ArrayList<Listener<T>>();
        }
        _listeners.add(listener);
        return;
      }
      instance = _instance;
      error = _error;
    }
    s_logger.debug("Value already available at {}", this);
    listener.value(instance, error);
  }

  // Supplier

  /**
   * Returns the instance, blocking if it is not yet available.
   * 
   * @return the instance returned by the underlying
   */
  @Override
  public final synchronized T get() {
    try {
      do {
        if (_error != null) {
          throw _error;
        }
        if (_listeners == null) {
          s_logger.debug("Got {}", this);
          return _instance;
        }
        s_logger.info("Waiting for {}", this);
        /*try {
          throw new OpenGammaRuntimeException("stacktrace");
        } catch (Exception e) {
          s_logger.info("Stack trace", e);
        }*/
        wait();
      } while (true);
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
  }

  public static Executor EXECUTOR = Executors.newCachedThreadPool();

  /**
   * Implementation which converts a value produced by another supplier.
   */
  public static class Filter<X, Y> extends AsyncSupplier<Y> {

    public Filter(final AsyncSupplier<X> underlying, final Function1<X, Y> filter) {
      underlying.get(new Listener<X>() {
        @Override
        public void value(final X instance, final RuntimeException error) {
          if (error == null) {
            EXECUTOR.execute(new Runnable() {
              @Override
              public void run() {
                post(filter.execute(instance), null);
              }
            });
          } else {
            post(null, error);
          }
        }
      });
    }

  }

  /**
   * Implementation which creates the instance in another thread.
   */
  public static abstract class Spawned<T> extends AsyncSupplier<T> {

    public void start() {
      EXECUTOR.execute(new Runnable() {
        @Override
        public void run() {
          doGet();
        }
      });
    }

    protected abstract T getImpl();

    protected void doGet() {
      T instance = null;
      RuntimeException error = null;
      try {
        s_logger.info("Creating {}", this);
        instance = getImpl();
        s_logger.info("Created {}", this);
      } catch (RuntimeException e) {
        s_logger.error("Couldn't create {} - {}", this, e);
        s_logger.warn("Caught exception", e);
        error = e;
      } finally {
        post(instance, error);
      }
    }

  }

}
