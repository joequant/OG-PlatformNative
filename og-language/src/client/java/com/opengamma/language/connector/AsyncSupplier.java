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

  public static Executor EXECUTOR = Executors.newCachedThreadPool();

  /**
   * Listener for notification when the value becomes available.
   */
  public interface Listener<T> {

    /**
     * Notifies of the value.
     * 
     * @param instance the instance produced
     */
    void value(T instance);

    /**
     * Notifies of an exception.
     * 
     * @param exception the exception thrown
     */
    void exception(RuntimeException exception);

    /**
     * Notifies of an error.
     * 
     * @param error the error thrown
     */
    void error(Error error);

  }

  private final class Notifier implements Runnable {

    private final Listener<T> _listener;

    public Notifier(final Listener<T> listener) {
      _listener = listener;
    }

    // Runnable

    @Override
    public void run() {
      try {
        if (_error != null) {
          _listener.error(_error);
        } else if (_exception != null) {
          _listener.exception(_exception);
        } else {
          _listener.value(_instance);
        }
      } catch (Throwable t) {
        s_logger.error("Caught exception", t);
      }
    }

  }

  /**
   * The executor to use, not null
   */
  private final Executor _executor;

  /**
   * Holds any exception thrown by the underlying source.
   */
  private volatile RuntimeException _exception;

  /**
   * Holds any error thrown by the underlying source.
   */
  private volatile Error _error;

  /**
   * Holds any result returned by the underlying source.
   */
  private volatile T _instance;

  /**
   * Holds any listeners that have been registered, or null if a result/error has been posted.
   */
  private Collection<Runnable> _listeners = Collections.emptyList();

  protected AsyncSupplier() {
    this(EXECUTOR);
  }

  protected AsyncSupplier(final Executor executor) {
    _executor = executor;
  }

  protected Executor getExecutor() {
    return _executor;
  }

  /**
   * Passes the result to any blocked callers to {@link #get} or listeners.
   * <p>
   * A sub-class is responsible for making sure this gets called, typically from a spawned thread or as the result of another supplier producing its value.
   */
  public final void post(final T instance, final RuntimeException exception, final Error error) {
    if (instance == null) {
      if (exception == null) {
        if (error == null) {
          s_logger.debug("Null result available from {}", this);
        } else {
          s_logger.debug("Error available from {}", this);
        }
      } else {
        if (error != null) {
          // Can't set both exception and error
          throw new IllegalArgumentException();
        }
        s_logger.debug("Exception available from {}", this);
      }
    } else {
      if ((exception != null) || (error != null)) {
        // Can't set both instance and exception/error
        throw new IllegalArgumentException();
      }
      s_logger.debug("Result available from {}", this);
    }
    final Collection<Runnable> listeners;
    synchronized (this) {
      assert _listeners != null;
      _instance = instance;
      _exception = exception;
      _error = error;
      notifyAll();
      listeners = _listeners;
      _listeners = null;
    }
    if (!listeners.isEmpty()) {
      s_logger.info("Notifying listeners from {}", this);
      for (Runnable listener : listeners) {
        try {
          s_logger.debug("Notifying listener from {}", this);
          getExecutor().execute(listener);
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
    final Runnable notify = new Notifier(listener);
    synchronized (this) {
      if (_listeners != null) {
        s_logger.debug("Registering listener for {}", this);
        if (_listeners.isEmpty()) {
          _listeners = new ArrayList<Runnable>();
        }
        _listeners.add(notify);
        return;
      }
    }
    s_logger.debug("Value already available at {}", this);
    getExecutor().execute(notify);
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
        if (_exception != null) {
          throw _exception;
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

  /**
   * Implementation which converts a value produced by another supplier.
   */
  public static class Filter<X, Y> extends AsyncSupplier<Y> {

    public Filter(final AsyncSupplier<X> underlying, final Function1<X, Y> filter) {
      underlying.get(new Listener<X>() {

        @Override
        public void value(final X instance) {
          final Y filtered;
          try {
            filtered = filter.execute(instance);
          } catch (RuntimeException e) {
            exception(e);
            return;
          } catch (Error e) {
            error(e);
            return;
          }
          post(filtered, null, null);
        }

        @Override
        public void exception(final RuntimeException exception) {
          post(null, exception, null);
        }

        @Override
        public void error(final Error error) {
          post(null, null, error);
        }

      });
    }

  }

  /**
   * Implementation which creates the instance in another thread.
   */
  public static abstract class Spawned<T> extends AsyncSupplier<T> {

    public void start() {
      getExecutor().execute(new Runnable() {
        @Override
        public void run() {
          doGet();
        }
      });
    }

    protected abstract T getImpl();

    protected void doGet() {
      T instance = null;
      RuntimeException exception = null;
      Error error = null;
      try {
        s_logger.info("Creating {}", this);
        instance = getImpl();
        s_logger.info("Created {}", this);
      } catch (RuntimeException e) {
        s_logger.error("Couldn't create {} - {}", this, e);
        s_logger.warn("Caught exception", e);
        exception = e;
      } catch (Error e) {
        s_logger.error("Couldn't create {} - {}", this, e);
        s_logger.warn("Caught error", e);
        error = e;
      } finally {
        post(instance, exception, error);
      }
    }

  }

}
