/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.types.TransportTypes;

/**
 * Default implementation of {@link ResultConverter}.
 */
public class DefaultResultConverter implements ResultConverter {

  @Override
  public Data convertResult(final SessionContext sessionContext, final Object result) {
    final ValueConverter converter = sessionContext.getGlobalContext().getValueConverter();
    return converter.convertValue(sessionContext, result, TransportTypes.DATA);
  }

}
