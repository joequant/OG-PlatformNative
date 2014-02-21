/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.security;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLegSchedule;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLegSchedule.Builder;
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
public class FloatingInterestRateSwapLegScheduleFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final FloatingInterestRateSwapLegScheduleFunction INSTANCE = new FloatingInterestRateSwapLegScheduleFunction();

  private final MetaFunction _meta;

  private static final int DATES = 0;
  private static final int PAYMENT_DATES = 1;
  private static final int CALCULATION_DATES = 2;

  private static List<MetaParameter> parameters() {
    final MetaParameter dates = new MetaParameter("dates", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(Integer.class).get()).get());
    final MetaParameter paymentDates = new MetaParameter("paymentDates", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(LocalDate.class).get()).get());
    final MetaParameter calculationDates = new MetaParameter("calculationDates", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(LocalDate.class).get()).get());
    return Arrays.asList(dates, paymentDates, calculationDates);
  }

  private FloatingInterestRateSwapLegScheduleFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.SECURITY, "FloatingInterestRateSwapLegSchedule", getParameters(), this));
  }

  protected FloatingInterestRateSwapLegScheduleFunction() {
    this(new DefinitionAnnotater(FloatingInterestRateSwapLegScheduleFunction.class));
  }

  public static FloatingInterestRateSwapLegSchedule invoke(final List<Integer> dates, final List<LocalDate> paymentDates, final List<LocalDate> calculationDates) {
    Builder builder = FloatingInterestRateSwapLegSchedule.builder();
    builder.dates(intListToArr(dates));
    builder.paymentDates(paymentDates.toArray(new LocalDate[] {}));
    builder.calculationDates(calculationDates.toArray(new LocalDate[] {}));
    FloatingInterestRateSwapLegSchedule schedule = builder.build();
    return schedule;
  }
  
  private static int[] intListToArr(List<Integer> list) {
    int[] arr = new int[list.size()];
    Iterator<Integer> iter = list.iterator();
    for (int i = 0; i < list.size(); i++) {
      Integer val = iter.next();
      if (val == null) {
        throw new OpenGammaRuntimeException("Null element in dates at position " + i);
      }
      arr[i] = val;
    }
    return arr;
  }
  
  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((List<Integer>) parameters[DATES], (List<LocalDate>) parameters[PAYMENT_DATES], (List<LocalDate>) parameters[CALCULATION_DATES]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
