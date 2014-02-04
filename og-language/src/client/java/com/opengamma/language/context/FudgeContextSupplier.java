/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import org.fudgemsg.FudgeContext;
import org.springframework.beans.factory.InitializingBean;

import com.opengamma.language.connector.AsyncSupplier;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

public class FudgeContextSupplier extends AsyncSupplier.Spawned<FudgeContext> implements InitializingBean {

  // InitializingBean

  @Override
  public void afterPropertiesSet() {
    start();
  }

  // AsyncSupplier.Spawned

  @Override
  protected FudgeContext getImpl() {
    return OpenGammaFudgeContext.getInstance();
  }

  // Object

  @Override
  public String toString() {
    return "OpenGammaFudgeContext";
  }

}
