/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.security;

import java.util.Arrays;
import java.util.List;

import com.opengamma.financial.security.irs.Rate;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Creates a portfolio node from one or more positions or other nodes
 */
public class ConstantRateFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ConstantRateFunction INSTANCE = new ConstantRateFunction();

  private final MetaFunction _meta;

  private static final int RATE = 0;

  private static List<MetaParameter> parameters() {
    final MetaParameter rate = new MetaParameter("rate", JavaTypeInfo.builder(Double.class).get());
    return Arrays.asList(rate);
  }

  private ConstantRateFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.SECURITY, "ConstantRate", getParameters(), this));
  }

  protected ConstantRateFunction() {
    this(new DefinitionAnnotater(ConstantRateFunction.class));
  }

  public static Rate invoke(final Double rateV) {
    Rate rate = new Rate(rateV);
    return rate;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((Double) parameters[RATE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
