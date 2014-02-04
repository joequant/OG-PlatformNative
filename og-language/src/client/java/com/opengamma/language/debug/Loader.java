/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.function.FunctionProviderBean;
import com.opengamma.language.livedata.LiveDataProviderBean;
import com.opengamma.language.procedure.ProcedureProviderBean;

/**
 * Extends the contexts with debugging/development tools.
 */
public class Loader extends ContextInitializationBean {

  protected FunctionProviderBean createFunctionProvider() {
    final FunctionProviderBean bean = new FunctionProviderBean();
    bean.addFunction(new DebugFunctionDimension());
    bean.addFunction(new DebugFunctionIncrement());
    bean.addFunction(new DebugFunctionLiteral("DebugFunctionLiteral", 42));
    bean.addFunction(new DebugFunctionMessage());
    bean.addFunction(new DebugFunctionMultipleTypes());
    return bean;
  }

  protected LiveDataProviderBean createLiveDataProvider() {
    final LiveDataProviderBean bean = new LiveDataProviderBean();
    bean.addLiveData(new DebugLiveDataRandom());
    return bean;
  }

  protected ProcedureProviderBean createProcedureProvider() {
    final ProcedureProviderBean bean = new ProcedureProviderBean();
    bean.addProcedure(new DebugProcedureIncrement());
    return bean;
  }

  // ContextInitializationBean

  @Override
  protected void initContext(final MutableSessionContext sessionContext) {
    sessionContext.getFunctionProvider().addProvider(createFunctionProvider());
    sessionContext.getLiveDataProvider().addProvider(createLiveDataProvider());
    sessionContext.getProcedureProvider().addProvider(createProcedureProvider());
  }

}
