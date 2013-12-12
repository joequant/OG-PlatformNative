/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import com.opengamma.language.connector.UserMessage;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Wrapper around an existing handler for intercepting calls.
 * 
 * @param <T> the data type
 */
public class UnhandledMessageHandlerAdapter<T> implements UnhandledMessageHandler<T> {

  private final UnhandledMessageHandler<T> _underlying;

  protected UnhandledMessageHandlerAdapter(final UnhandledMessageHandler<T> underlying) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
  }

  protected UnhandledMessageHandler<T> getUnderlying() {
    return _underlying;
  }

  // UnhandledMessageHandler

  @Override
  public void unhandledMessage(final UserMessage message, final T data) throws AsynchronousExecution {
    getUnderlying().unhandledMessage(message, data);
  }

}
