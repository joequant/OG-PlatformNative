/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.security;

import java.util.Arrays;
import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.security.irs.StubCalculationMethod;
import com.opengamma.financial.security.irs.StubCalculationMethod.Builder;
import com.opengamma.id.ExternalId;
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
    final MetaParameter firstStubReferenceRateId = new MetaParameter("firstStubStartReferenceRateId", JavaTypeInfo.builder(ExternalId.class).allowNull().get());
    final MetaParameter firstStubEndReferenceRateId = new MetaParameter("firstStubEndReferenceRateId", JavaTypeInfo.builder(ExternalId.class).allowNull().get());
    final MetaParameter lastStubStartReferenceRateId = new MetaParameter("lastStubStartReferenceRateId", JavaTypeInfo.builder(ExternalId.class).allowNull().get());
    final MetaParameter lastStubEndReferenceRateId = new MetaParameter("lastStubEndReferenceRateId", JavaTypeInfo.builder(ExternalId.class).allowNull().get());
    return Arrays.asList(type, firstStubRate, lastStubRate, firstStubEndDate, lastStubEndDate, firstStubReferenceRateId, firstStubEndReferenceRateId, lastStubStartReferenceRateId, lastStubEndReferenceRateId);
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
                                             final ExternalId firstStubStartReferenceRateId, final ExternalId firstStubEndReferenceRateId,
                                             final ExternalId lastStubStartReferenceRateId, final ExternalId lastStubEndReferenceRateId) {
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
    if (firstStubStartReferenceRateId != null) {
      builder.firstStubStartReferenceRateId(firstStubStartReferenceRateId);
    }
    if (firstStubEndReferenceRateId != null) {
      builder.firstStubEndReferenceRateId(firstStubEndReferenceRateId);
    }
    if (lastStubStartReferenceRateId != null) {
      builder.lastStubStartReferenceRateId(lastStubStartReferenceRateId);
    }
    if (lastStubEndReferenceRateId != null) {
      builder.lastStubEndReferenceRateId(lastStubEndReferenceRateId);
    }
    return builder.build();
  }

  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((StubType) parameters[TYPE], (Double) parameters[FIRST_STUB_RATE], (Double) parameters[LAST_STUB_RATE],
                  (LocalDate) parameters[FIRST_STUB_END_DATE], (LocalDate) parameters[LAST_STUB_END_DATE],
                  (ExternalId) parameters[FIRST_STUB_START_INDEX], (ExternalId) parameters[FIRST_STUB_END_INDEX],
                  (ExternalId) parameters[LAST_STUB_START_INDEX], (ExternalId) parameters[LAST_STUB_END_INDEX]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
