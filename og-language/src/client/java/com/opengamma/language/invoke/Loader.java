/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeContext;

import com.google.common.base.Supplier;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.convert.FudgeTypeConverter;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends a context factory to attach a type converter provider.
 */
public class Loader extends ContextInitializationBean {

  private List<TypeConverterProvider> _typeConverterProviders;
  private Supplier<FudgeContext> _fudgeContext;

  public void setTypeConverterProvider(final TypeConverterProvider typeConverterProvider) {
    ArgumentChecker.notNull(typeConverterProvider, "typeConverterProvider");
    _typeConverterProviders = Collections.singletonList(typeConverterProvider);
  }

  public void setTypeConverterProviders(final Collection<TypeConverterProvider> typeConverterProviders) {
    ArgumentChecker.noNulls(typeConverterProviders, "typeConverterProviders");
    ArgumentChecker.isFalse(typeConverterProviders.isEmpty(), "typeConverterProviders");
    _typeConverterProviders = new ArrayList<TypeConverterProvider>(typeConverterProviders);
  }

  public TypeConverterProvider getTypeConverterProvider() {
    if ((_typeConverterProviders == null) || _typeConverterProviders.isEmpty()) {
      return null;
    } else {
      return _typeConverterProviders.get(0);
    }
  }

  public Collection<TypeConverterProvider> getTypeConverterProviders() {
    return _typeConverterProviders;
  }

  public void setFudgeContext(final Supplier<FudgeContext> fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public Supplier<FudgeContext> getFudgeContext() {
    return _fudgeContext;
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getTypeConverterProviders(), "typeConverterProviders");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    for (TypeConverterProvider typeConverterProvider : getTypeConverterProviders()) {
      globalContext.getTypeConverterProvider().addTypeConverterProvider(typeConverterProvider);
    }
    Supplier<FudgeContext> fudgeContext = getFudgeContext();
    if (fudgeContext != null) {
      FudgeTypeConverter.setFudgeContext(globalContext, fudgeContext.get());
    }
  }

}
