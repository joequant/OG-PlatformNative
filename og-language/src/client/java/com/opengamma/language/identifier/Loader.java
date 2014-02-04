/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;

/**
 * Extends the contexts with support for identifiers.
 */
public class Loader extends ContextInitializationBean {

  // ContextInitializationBean

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    globalContext.getTypeConverterProvider().addTypeConverterProvider(new Converters());
    globalContext.getFunctionProvider().addProvider(new IdentifierFunctionProvider());
  }

}
