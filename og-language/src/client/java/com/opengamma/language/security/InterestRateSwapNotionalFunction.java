/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.security;

import java.util.Arrays;
import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.money.Currency;

/**
 * Creates a portfolio node from one or more positions or other nodes
 */
public class InterestRateSwapNotionalFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final InterestRateSwapNotionalFunction INSTANCE = new InterestRateSwapNotionalFunction();

  private final MetaFunction _meta;

  private static final int CCY = 0;
  private static final int AMOUNT = 1;
  private static final int DATES = 2;
  private static final int NOTIONALS = 3;
  private static final int RATE_SHIFT_TYPE = 4;

  private static List<MetaParameter> parameters() {
    final MetaParameter currency = new MetaParameter("currency", JavaTypeInfo.builder(Currency.class).allowNull().get());
    final MetaParameter amount = new MetaParameter("amount", JavaTypeInfo.builder(Integer.class).allowNull().get());
    final MetaParameter dates = new MetaParameter("dates", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(PortfolioNode.class).get()).get());
    final MetaParameter notionals = new MetaParameter("notionals", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(Position.class).get()).get());
    final MetaParameter rateShiftType = new MetaParameter("types", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(Rate.ShiftType.class).get()).get());
    return Arrays.asList(currency, amount, dates, notionals, rateShiftType);
  }

  private InterestRateSwapNotionalFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.SECURITY, "InterestRateSwapNotional", getParameters(), this));
  }

  protected InterestRateSwapNotionalFunction() {
    this(new DefinitionAnnotater(InterestRateSwapNotionalFunction.class));
  }

  public static InterestRateSwapNotional invoke(final Currency currency, final Integer amount, final List<LocalDate> dates, final List<Double> notionals, final List<Rate.ShiftType> rateShiftTypes) {
    if (amount != null) {
      return InterestRateSwapNotional.of(currency, amount);
    } else {
      return InterestRateSwapNotional.of(currency, dates, notionals, rateShiftTypes);
    }
  }

  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((Currency) parameters[CCY], (Integer) parameters[AMOUNT], (List<LocalDate>) parameters[DATES], (List<Double>) parameters[NOTIONALS], (List<Rate.ShiftType>) parameters[RATE_SHIFT_TYPE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
