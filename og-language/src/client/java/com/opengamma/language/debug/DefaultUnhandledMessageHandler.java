/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.UserMessage;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Default unhandled message handler that just reports the state to a log.
 * 
 * @param <T> the data type
 */
public class DefaultUnhandledMessageHandler<T> implements UnhandledMessageHandler<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultUnhandledMessageHandler.class);

  @Override
  public void unhandledMessage(final UserMessage message, final T data) throws AsynchronousExecution {
    s_logger.warn("Unhandled {} message with handle {}", message.getPayload().getClass(), message.getHandle());
    s_logger.debug("Message payload: {}", message.getPayload());
  }

}
