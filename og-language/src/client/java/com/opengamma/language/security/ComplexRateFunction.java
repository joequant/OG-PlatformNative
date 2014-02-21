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
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.irs.Rate.Builder;
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
public class ComplexRateFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ComplexRateFunction INSTANCE = new ComplexRateFunction();

  private final MetaFunction _meta;

  private static final int DATES = 0;
  private static final int RATES = 1;
  private static final int TYPES = 2;

  private static List<MetaParameter> parameters() {
    final MetaParameter dates = new MetaParameter("dates", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(Integer.class).get()).get());
    final MetaParameter rates = new MetaParameter("rates", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(Double.class).get()).get());
    final MetaParameter rateShiftTypes = new MetaParameter("types", JavaTypeInfo.builder(List.class).allowNull().parameter(JavaTypeInfo.builder(Rate.ShiftType.class).get()).get());
    return Arrays.asList(dates, rates, rateShiftTypes);
  }

  private ComplexRateFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.SECURITY, "ComplexRate", getParameters(), this));
  }

  protected ComplexRateFunction() {
    this(new DefinitionAnnotater(ComplexRateFunction.class));
  }

  public static Rate invoke(final List<Integer> dates, final List<Double> rates, final List<Rate.ShiftType> rateShiftTypes) {
    Builder builder = Rate.builder();
    builder.dates(intListToArr(dates));
    builder.rates(doubleListToArr(rates));
    builder.types(rateShiftTypes.toArray(new Rate.ShiftType[] {}));
    return builder.build();
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
  
  private static double[] doubleListToArr(List<Double> list) {
    double[] arr = new double[list.size()];
    Iterator<Double> iter = list.iterator();
    for (int i = 0; i < list.size(); i++) {
      Double val = iter.next();
      if (val == null) {
        throw new OpenGammaRuntimeException("Null element in rates at position " + i);
      }
      arr[i] = val;
    }
    return arr;
  }
  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((List<Integer>) parameters[DATES], (List<Double>) parameters[RATES], (List<Rate.ShiftType>) parameters[TYPES]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
