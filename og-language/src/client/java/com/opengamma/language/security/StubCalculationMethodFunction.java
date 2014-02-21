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
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.irs.StubCalculationMethod;
import com.opengamma.financial.security.irs.StubCalculationMethod.Builder;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Creates a portfolio node from one or more positions or other nodes
 */
public class StubCalculationMethodFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final StubCalculationMethodFunction INSTANCE = new StubCalculationMethodFunction();

  private final MetaFunction _meta;

  private static final int TYPE = 0;
  private static final int FIRST_STUB_RATE = 1;
  private static final int LAST_STUB_RATE = 2;
  private static final int FIRST_STUB_END_DATE = 3;
  private static final int LAST_STUB_END_DATE = 4;
  private static final int FIRST_STUB_START_INDEX = 5;
  private static final int FIRST_STUB_END_INDEX = 6;
  private static final int LAST_STUB_START_INDEX = 7;
  private static final int LAST_STUB_END_INDEX = 8;

  private static List<MetaParameter> parameters() {
    final MetaParameter type = new MetaParameter("type", JavaTypeInfo.builder(StubType.class).get());
    final MetaParameter firstStubRate = new MetaParameter("firstStubRate", JavaTypeInfo.builder(Double.class).allowNull().get());
    final MetaParameter lastStubRate = new MetaParameter("lastStubRate", JavaTypeInfo.builder(Double.class).allowNull().get());
    final MetaParameter firstStubEndDate = new MetaParameter("firstStubEndDate", JavaTypeInfo.builder(LocalDate.class).allowNull().get());
    final MetaParameter lastStubEndDate = new MetaParameter("lastStubEndDate", JavaTypeInfo.builder(LocalDate.class).allowNull().get());
    final MetaParameter firstStubStartIndex = new MetaParameter("firstStubStartIndex", JavaTypeInfo.builder(Tenor.class).allowNull().get());
    final MetaParameter firstStubEndIndex = new MetaParameter("firstStubEndIndex", JavaTypeInfo.builder(Tenor.class).allowNull().get());
    final MetaParameter lastStubStartIndex = new MetaParameter("lastStubStartIndex", JavaTypeInfo.builder(Tenor.class).allowNull().get());
    final MetaParameter lastStubEndIndex = new MetaParameter("lastStubEndIndex", JavaTypeInfo.builder(Tenor.class).allowNull().get());
    return Arrays.asList(type, firstStubRate, lastStubRate, firstStubEndDate, lastStubEndDate, firstStubStartIndex, firstStubEndIndex, lastStubStartIndex, lastStubEndIndex);
  }

  private StubCalculationMethodFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.SECURITY, "StubCalculationMethod", getParameters(), this));
  }

  protected StubCalculationMethodFunction() {
    this(new DefinitionAnnotater(StubCalculationMethodFunction.class));
  }

  public static StubCalculationMethod invoke(final StubType type, final Double firstStubRate, final Double lastStubRate,
                                             final LocalDate firstStubEndDate, final LocalDate lastStubEndDate,
                                             final Tenor firstStubStartIndex, final Tenor firstStubEndIndex,
                                             final Tenor lastStubStartIndex, final Tenor lastStubEndIndex) {
    Builder builder = StubCalculationMethod.builder();
    builder.type(type);
    if (firstStubRate != null) {
      builder.firstStubRate(firstStubRate);
    }
    if (lastStubRate != null) {
      builder.lastStubRate(lastStubRate);
    }
    if (firstStubEndDate != null) {
      builder.firstStubEndDate(firstStubEndDate);
    }
    if (lastStubEndDate != null) {
      builder.lastStubEndDate(lastStubEndDate);
    }
    if (firstStubStartIndex != null) {
      builder.firstStubStartIndex(firstStubStartIndex);
    }
    if (firstStubEndIndex != null) {
      builder.firstStubEndIndex(firstStubEndIndex);
    }
    if (lastStubStartIndex != null) {
      builder.lastStubStartIndex(lastStubStartIndex);
    }
    if (lastStubEndIndex != null) {
      builder.lastStubEndIndex(lastStubEndIndex);
    }
    return builder.build();
  }

  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((StubType) parameters[TYPE], (Double) parameters[FIRST_STUB_RATE], (Double) parameters[LAST_STUB_RATE], 
                  (LocalDate) parameters[FIRST_STUB_END_DATE], (LocalDate) parameters[LAST_STUB_END_DATE],
                  (Tenor) parameters[FIRST_STUB_START_INDEX], (Tenor) parameters[FIRST_STUB_END_INDEX],
                  (Tenor) parameters[LAST_STUB_START_INDEX], (Tenor) parameters[LAST_STUB_END_INDEX]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
