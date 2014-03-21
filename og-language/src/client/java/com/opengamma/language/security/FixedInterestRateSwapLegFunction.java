/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.security;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLegSchedule;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.irs.StubCalculationMethod;
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
public class FixedInterestRateSwapLegFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final FixedInterestRateSwapLegFunction INSTANCE = new FixedInterestRateSwapLegFunction();

  private final MetaFunction _meta;
  private static final int RATE = 0;
  private static final int SCHEDULE = 1;
  private static final int NOTIONAL = 2;
  private static final int PAY_RECEIVE_TYPE = 3;
  private static final int STUB_CALCULATION_METHOD = 4;
  private static final int DAY_COUNT_CONVENTION = 5;
  private static final int ROLL_CONVENTION = 6;
  private static final int MATURITY_DATE_CALENDARS = 7;
  private static final int MATURITY_DATE_BUSINESS_DAY_CONVENTION = 8;
  private static final int PAYMENT_DATE_CALENDARS = 9;
  private static final int PAYMENT_DATE_BUSINESS_DAY_CONVENTION = 10;
  private static final int PAYMENT_DATE_FREQUENCY = 11;
  private static final int PAYMENT_DATE_RELATIVE_TO = 12;
  private static final int PAYMENT_OFFSET = 13;
  private static final int ACCRUAL_PERIOD_CALENDARS = 14;
  private static final int ACCRUAL_PERIOD_BUSINESS_DAY_CONVENTION = 15;
  private static final int ACCRUAL_PERIOD_FREQUENCY = 16;

  private static List<MetaParameter> parameters() {
    final MetaParameter rate = new MetaParameter("rate", JavaTypeInfo.builder(Rate.class).get());
    final MetaParameter schedule = new MetaParameter("schedule", JavaTypeInfo.builder(FloatingInterestRateSwapLegSchedule.class).allowNull().get());
    final MetaParameter notional = new MetaParameter("notional", JavaTypeInfo.builder(InterestRateSwapNotional.class).allowNull().get());
    final MetaParameter payReceiveType = new MetaParameter("payReceiveType", JavaTypeInfo.builder(PayReceiveType.class).get());
    final MetaParameter stubCalculationMethod = new MetaParameter("stubCalculationMethod", JavaTypeInfo.builder(StubCalculationMethod.class).allowNull().get());
    final MetaParameter dayCountConvention = new MetaParameter("dayCountConvention", JavaTypeInfo.builder(DayCount.class).get());
    final MetaParameter rollConvention = new MetaParameter("rollConvention", JavaTypeInfo.builder(RollConvention.class).allowNull().get());
    final MetaParameter maturityDateCalendars = new MetaParameter("maturityDateCalendars", JavaTypeInfo.builder(Set.class).allowNull().parameter(JavaTypeInfo.builder(ExternalId.class).get()).get());
    final MetaParameter maturityDateBusinessDayConvention = new MetaParameter("maturityDateBusinessDayConvention", JavaTypeInfo.builder(BusinessDayConvention.class).get());
    final MetaParameter paymentDateCalendars = new MetaParameter("paymentDateCalendars", JavaTypeInfo.builder(Set.class).allowNull().parameter(JavaTypeInfo.builder(ExternalId.class).get()).get());
    final MetaParameter paymentDateBusinessDayConvention = new MetaParameter("paymentDateBusinessDayConvention", JavaTypeInfo.builder(BusinessDayConvention.class).get());
    final MetaParameter paymentDateFrequency = new MetaParameter("paymentDateFrequency", JavaTypeInfo.builder(Frequency.class).get());
    final MetaParameter paymentDateRelativeTo = new MetaParameter("paymentDateRelativeTo", JavaTypeInfo.builder(DateRelativeTo.class).allowNull().get());    // allow null even though NotNull field because initialised
    final MetaParameter paymentOffset = new MetaParameter("paymentOffset", JavaTypeInfo.builder(Integer.class).allowNull().get());    // allow null even though NotNull field because initialised
    final MetaParameter accrualDateCalendars = new MetaParameter("accrualPeriodCalendars", JavaTypeInfo.builder(Set.class).allowNull().parameter(JavaTypeInfo.builder(ExternalId.class).get()).get());
    final MetaParameter accrualDateBusinessDayConvention = new MetaParameter("accrualPeriodBusinessDayConvention", JavaTypeInfo.builder(BusinessDayConvention.class).get());
    final MetaParameter accrualDateFrequency = new MetaParameter("accrualPeriodFrequency", JavaTypeInfo.builder(Frequency.class).get());
    return Arrays.asList(rate, schedule, notional, payReceiveType, stubCalculationMethod, dayCountConvention, rollConvention,
                         maturityDateCalendars, maturityDateBusinessDayConvention,
                         paymentDateCalendars, paymentDateBusinessDayConvention, paymentDateFrequency, paymentDateRelativeTo, paymentOffset,
                         accrualDateCalendars, accrualDateBusinessDayConvention, accrualDateFrequency);
  }

  private FixedInterestRateSwapLegFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.SECURITY, "FixedInterestRateSwapLeg", getParameters(), this));
  }

  protected FixedInterestRateSwapLegFunction() {
    this(new DefinitionAnnotater(FixedInterestRateSwapLegFunction.class));
  }

  public static FixedInterestRateSwapLeg invoke(final Rate rate, final FloatingInterestRateSwapLegSchedule schedule, final InterestRateSwapNotional notional, final PayReceiveType payReceiveType,
                                                final StubCalculationMethod stubCalculationMethod, final DayCount dayCountConvention,
                                                final RollConvention rollConvention, final Set<ExternalId> maturityDateCalendars, final BusinessDayConvention maturityDateBusinessDayConvention,
                                                final Set<ExternalId> paymentDateCalendars, final BusinessDayConvention paymentDateBusinessDayConvention, final Frequency paymentDateFrequency, final DateRelativeTo paymentDateRelativeTo,
                                                final Integer paymentOffset, final Set<ExternalId> accrualPeriodCalendars, final BusinessDayConvention accrualPeriodBusinessDayConvention,
                                                final Frequency accrualPeriodFrequency) {
    FixedInterestRateSwapLeg leg = new FixedInterestRateSwapLeg();
    leg.setRate(rate);
    leg.setSchedule(schedule);
    leg.setNotional(notional);
    leg.setPayReceiveType(payReceiveType);
    leg.setStubCalculationMethod(stubCalculationMethod);
    leg.setDayCountConvention(dayCountConvention);
    leg.setMaturityDateBusinessDayConvention(maturityDateBusinessDayConvention);
    if (rollConvention != null) {
      leg.setRollConvention(rollConvention);
    }
    if (maturityDateCalendars != null) {
      leg.setMaturityDateCalendars(maturityDateCalendars);
    }
    leg.setPaymentDateBusinessDayConvention(paymentDateBusinessDayConvention);
    if (paymentDateCalendars != null) {
      leg.setPaymentDateCalendars(paymentDateCalendars);
    }
    leg.setPaymentDateBusinessDayConvention(paymentDateBusinessDayConvention);
    leg.setPaymentDateFrequency(paymentDateFrequency);
    if (paymentDateRelativeTo != null) {
      leg.setPaymentDateRelativeTo(paymentDateRelativeTo);
    }
    if (paymentOffset != null) { // implicit null means 0
      leg.setPaymentOffset(paymentOffset);
    }
    if (accrualPeriodCalendars != null) {
      leg.setAccrualPeriodCalendars(accrualPeriodCalendars);
    }
    leg.setAccrualPeriodBusinessDayConvention(accrualPeriodBusinessDayConvention);
    leg.setAccrualPeriodFrequency(accrualPeriodFrequency);
    return leg;
  }

  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((Rate) parameters[RATE], (FloatingInterestRateSwapLegSchedule) parameters[SCHEDULE], (InterestRateSwapNotional) parameters[NOTIONAL], (PayReceiveType) parameters[PAY_RECEIVE_TYPE],
                  (StubCalculationMethod) parameters[STUB_CALCULATION_METHOD], (DayCount) parameters[DAY_COUNT_CONVENTION],
                  (RollConvention) parameters[ROLL_CONVENTION], (Set<ExternalId>) parameters[MATURITY_DATE_CALENDARS], (BusinessDayConvention) parameters[MATURITY_DATE_BUSINESS_DAY_CONVENTION],
                  (Set<ExternalId>) parameters[PAYMENT_DATE_CALENDARS], (BusinessDayConvention) parameters[PAYMENT_DATE_BUSINESS_DAY_CONVENTION], (Frequency) parameters[PAYMENT_DATE_FREQUENCY],
                  (DateRelativeTo) parameters[PAYMENT_DATE_RELATIVE_TO], (Integer) parameters[PAYMENT_OFFSET], (Set<ExternalId>) parameters[ACCRUAL_PERIOD_CALENDARS],
                  (BusinessDayConvention) parameters[ACCRUAL_PERIOD_BUSINESS_DAY_CONVENTION], (Frequency) parameters[ACCRUAL_PERIOD_FREQUENCY]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
