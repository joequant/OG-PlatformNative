/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.security;

import java.util.Arrays;
import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.irs.NotionalExchange;
import com.opengamma.id.ExternalIdBundle;
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
public class InterestRateSwapSecurityFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final InterestRateSwapSecurityFunction INSTANCE = new InterestRateSwapSecurityFunction();

  private final MetaFunction _meta;

  private static final int NAME = 0;
  private static final int NOTIONAL_EXCHANGE = 1;
  private static final int EFFECTIVE_DATE = 2;
  private static final int UNADJUSTED_MATURITY_DATE = 3;
  private static final int LEGS = 4;

  //TODO does NotionalExchange need exposing as a published function
  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).get());
    final MetaParameter dates = new MetaParameter("notionalExchange", JavaTypeInfo.builder(NotionalExchange.class).defaultValue(NotionalExchange.NO_EXCHANGE).get());
    final MetaParameter effectiveDate = new MetaParameter("effectiveDate", JavaTypeInfo.builder(LocalDate.class).get());
    final MetaParameter unadjustedMaturityDate = new MetaParameter("unadjustedMaturityDate", JavaTypeInfo.builder(LocalDate.class).get());
    final MetaParameter legs = new MetaParameter("legs", JavaTypeInfo.builder(List.class).parameter(JavaTypeInfo.builder(InterestRateSwapLeg.class).get()).get());
    return Arrays.asList(name, dates, effectiveDate, unadjustedMaturityDate, legs);
  }

  private InterestRateSwapSecurityFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.SECURITY, "InterestRateSwapSecurity", getParameters(), this));
  }

  protected InterestRateSwapSecurityFunction() {
    this(new DefinitionAnnotater(InterestRateSwapSecurityFunction.class));
  }

  public static InterestRateSwapSecurity invoke(final String name, final NotionalExchange notionalExchange, final LocalDate effectiveDate, final LocalDate unadjustedMaturityDate, final List<InterestRateSwapLeg> legs) {
    InterestRateSwapSecurity swap = new InterestRateSwapSecurity(ExternalIdBundle.EMPTY,
                                                                 name,
                                                                 effectiveDate,
                                                                 unadjustedMaturityDate,
                                                                 legs);
    swap.setNotionalExchange(notionalExchange);
    return swap;
  }
  
  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((String) parameters[NAME],
                  (NotionalExchange) parameters[NOTIONAL_EXCHANGE],
                  (LocalDate) parameters[EFFECTIVE_DATE],
                  (LocalDate) parameters[UNADJUSTED_MATURITY_DATE],
                  (List<InterestRateSwapLeg>) parameters[LEGS]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
