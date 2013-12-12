/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import com.opengamma.language.connector.UserMessage;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Defines a visitor interface for any user messages bounced back by the C++ layer because they were not expected.
 * 
 * @param <T> the data type
 */
public interface UnhandledMessageHandler<T> {

  void unhandledMessage(UserMessage message, T data) throws AsynchronousExecution;

}
