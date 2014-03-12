/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.security;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLegSchedule;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.irs.RateAveragingMethod;
import com.opengamma.financial.security.irs.StubCalculationMethod;
import com.opengamma.financial.security.swap.FloatingRateType;
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
public class FloatingInterestRateSwapLegFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final FloatingInterestRateSwapLegFunction INSTANCE = new FloatingInterestRateSwapLegFunction();

  private final MetaFunction _meta;

  private static final int CUSTOM_RATES = 0;
  private static final int RATE_AVERAGING_METHOD = 1;
  private static final int RATE_CUTOFF_DAYS_OFFSET = 2;
  private static final int CAP_RATE = 3;
  private static final int FLOOR_RATE = 4;
  private static final int GEARING = 5;
  private static final int SPREAD_SCHEDULE = 6;
  private static final int SCHEDULE = 7;
  private static final int FLOATING_REFERENCE_RATE_ID = 8;
  private static final int FLOATING_RATE_TYPE = 9;
  private static final int RESET_PERIOD_CALENDARS = 10;
  private static final int RESET_PERIOD_BUSINESS_DAY_CONVENTION = 11;
  private static final int RESET_PERIOD_FREQUENCY = 12;
  private static final int RESET_DATE_RELATIVE_TO = 13;
  private static final int COMPOUNDING_METHOD = 14;
  private static final int FIXING_DATE_CALENDARS = 15;
  private static final int FIXING_DATE_BUSINESS_DAY_CONVENTION = 16;
  private static final int FIXING_DATE_OFFSET = 17;
  private static final int FIXING_DATE_OFFSET_TYPE = 18;
  private static final int NOTIONAL = 19;
  private static final int PAY_RECEIVE_TYPE = 20;
  private static final int STUB_CALCULATION_METHOD = 21;
  private static final int DAY_COUNT_CONVENTION = 22;
  private static final int ROLL_CONVENTION = 23;
  private static final int MATURITY_DATE_CALENDARS = 24;
  private static final int MATURITY_DATE_BUSINESS_DAY_CONVENTION = 25;
  private static final int PAYMENT_DATE_CALENDARS = 26;
  private static final int PAYMENT_DATE_BUSINESS_DAY_CONVENTION = 27;
  private static final int PAYMENT_DATE_FREQUENCY = 28;
  private static final int PAYMENT_DATE_RELATIVE_TO = 29;
  private static final int PAYMENT_OFFSET = 30;
  private static final int ACCRUAL_PERIOD_CALENDARS = 31;
  private static final int ACCRUAL_PERIOD_BUSINESS_DAY_CONVENTION = 32;
  private static final int ACCRUAL_PERIOD_FREQUENCY = 33;

  private static List<MetaParameter> parameters() {
    final MetaParameter customRates = new MetaParameter("customRates", JavaTypeInfo.builder(Rate.class).allowNull().get());
    final MetaParameter rateAveragingMethod = new MetaParameter("rateAveragingMethod", JavaTypeInfo.builder(RateAveragingMethod.class).allowNull().get());
    final MetaParameter rateCutoffDaysOffset = new MetaParameter("rateCutoffDaysOffset", JavaTypeInfo.builder(Integer.class).defaultValue(0).get()); // init to 0 if not specified
    final MetaParameter capRate = new MetaParameter("capRate", JavaTypeInfo.builder(Double.class).allowNull().get());
    final MetaParameter floorRate = new MetaParameter("floorRate", JavaTypeInfo.builder(Double.class).allowNull().get());
    final MetaParameter gearing = new MetaParameter("gearing", JavaTypeInfo.builder(Double.class).get());
    final MetaParameter spreadSchedule = new MetaParameter("spreadSchedule", JavaTypeInfo.builder(Rate.class).allowNull().get());
    final MetaParameter schedule = new MetaParameter("schedule", JavaTypeInfo.builder(FloatingInterestRateSwapLegSchedule.class).allowNull().get());
    final MetaParameter floatingReferenceRateId = new MetaParameter("floatingReferenceRateId", JavaTypeInfo.builder(ExternalId.class).get());
    final MetaParameter floatingRateType = new MetaParameter("floatingRateType", JavaTypeInfo.builder(FloatingRateType.class).get());
    final MetaParameter resetPeriodCalendars = new MetaParameter("resetPeriodCalendars", JavaTypeInfo.builder(Set.class).allowNull().parameter(JavaTypeInfo.builder(ExternalId.class).get())
        .defaultValue(Sets.newHashSet()).get()); // allow null even though not null because initialised with default
    final MetaParameter resetPeriodBusinessDayConvention = new MetaParameter("resetPeriodBusinessDayConvention", JavaTypeInfo.builder(BusinessDayConvention.class).get());
    final MetaParameter resetPeriodFrequency = new MetaParameter("resetPeriodFrequency", JavaTypeInfo.builder(Frequency.class).get());
    final MetaParameter resetDateRelativeTo = new MetaParameter("resetDateRelativeTo", JavaTypeInfo.builder(DateRelativeTo.class).defaultValue(DateRelativeTo.START).get()); // allow null even though NotNull because initialised with default
    final MetaParameter compoundingMethod = new MetaParameter("compoundingMethod", JavaTypeInfo.builder(CompoundingMethod.class).defaultValue(CompoundingMethod.NONE).get()); // initialised, convert null to don't set.
    final MetaParameter fixingDateCalendars = new MetaParameter("fixingDateCalendars", JavaTypeInfo.builder(Set.class).allowNull().parameter(JavaTypeInfo.builder(ExternalId.class).get())
        .get()); // allow null even though not null because initialised with default
    final MetaParameter fixingDateBusinessDayConvention = new MetaParameter("fixingDateBusinessDayConvention", JavaTypeInfo.builder(BusinessDayConvention.class).get());
    final MetaParameter fixingDateOffset = new MetaParameter("fixingDateOffset", JavaTypeInfo.builder(Integer.class).allowNull().get()); // init to 0 if not specified
    final MetaParameter fixingDateOffsetType = new MetaParameter("fixingDateOffsetType", JavaTypeInfo.builder(OffsetType.class).allowNull().get());
    final MetaParameter notional = new MetaParameter("notional", JavaTypeInfo.builder(InterestRateSwapNotional.class).allowNull().get());
    final MetaParameter payReceiveType = new MetaParameter("payReceiveType", JavaTypeInfo.builder(PayReceiveType.class).get());
    final MetaParameter stubCalculationMethod = new MetaParameter("stubCalculationMethod", JavaTypeInfo.builder(StubCalculationMethod.class).allowNull().get());
    final MetaParameter dayCountConvention = new MetaParameter("dayCountConvention", JavaTypeInfo.builder(DayCount.class).get());
    final MetaParameter rollConvention = new MetaParameter("rollConvention", JavaTypeInfo.builder(RollConvention.class).allowNull().get());
    final MetaParameter maturityDateCalendars = new MetaParameter("maturityDateCalendars", JavaTypeInfo.builder(Set.class).allowNull()
        .parameter(JavaTypeInfo.builder(ExternalId.class).get()).get());
    final MetaParameter maturityDateBusinessDayConvention = new MetaParameter("maturityDateBusinessDayConvention", JavaTypeInfo.builder(BusinessDayConvention.class).get());
    final MetaParameter paymentDateCalendars = new MetaParameter("paymentDateCalendars", JavaTypeInfo.builder(Set.class).allowNull().parameter(JavaTypeInfo.builder(ExternalId.class).get())
        .get());
    final MetaParameter paymentDateBusinessDayConvention = new MetaParameter("paymentDateBusinessDayConvention", JavaTypeInfo.builder(BusinessDayConvention.class).get());
    final MetaParameter paymentDateFrequency = new MetaParameter("paymentDateFrequency", JavaTypeInfo.builder(Frequency.class).get());
    final MetaParameter paymentDateRelativeTo = new MetaParameter("paymentDateRelativeTo", JavaTypeInfo.builder(DateRelativeTo.class).allowNull().get()); // allow null even though NotNull field because initialised
    final MetaParameter paymentOffset = new MetaParameter("paymentOffset", JavaTypeInfo.builder(Integer.class).allowNull().get()); // allow null even though NotNull field because initialised    
    final MetaParameter accrualDateCalendars = new MetaParameter("accrualPeriodCalendars", JavaTypeInfo.builder(Set.class).allowNull()
        .parameter(JavaTypeInfo.builder(ExternalId.class).get()).get());
    final MetaParameter accrualDateBusinessDayConvention = new MetaParameter("accrualPeriodBusinessDayConvention", JavaTypeInfo.builder(BusinessDayConvention.class).get());
    final MetaParameter accrualDateFrequency = new MetaParameter("accrualPeriodFrequency", JavaTypeInfo.builder(Frequency.class).get());
    return Arrays.asList(customRates, rateAveragingMethod, rateCutoffDaysOffset, capRate, floorRate, gearing, spreadSchedule, schedule, floatingReferenceRateId, floatingRateType,
        resetPeriodCalendars, resetPeriodBusinessDayConvention, resetPeriodFrequency, resetDateRelativeTo, compoundingMethod, fixingDateCalendars, fixingDateBusinessDayConvention,
        fixingDateOffset, fixingDateOffsetType, notional, payReceiveType, stubCalculationMethod, dayCountConvention, rollConvention, maturityDateCalendars,
        maturityDateBusinessDayConvention, paymentDateCalendars, paymentDateBusinessDayConvention, paymentDateFrequency, paymentDateRelativeTo, paymentOffset, accrualDateCalendars,
        accrualDateBusinessDayConvention, accrualDateFrequency);
  }

  private FloatingInterestRateSwapLegFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.SECURITY, "FloatingInterestRateSwapLeg", getParameters(), this));
  }

  protected FloatingInterestRateSwapLegFunction() {
    this(new DefinitionAnnotater(FloatingInterestRateSwapLegFunction.class));
  }

  public static FloatingInterestRateSwapLeg invoke(final Rate customRates, final RateAveragingMethod rateAveragingMethod, final Integer rateCutoffDaysOffset, final Double capRate,
      final Double floorRate, final Double gearing, final Rate spreadSchedule, final FloatingInterestRateSwapLegSchedule schedule, final ExternalId floatingReferenceRateId,
      final FloatingRateType floatingRateType, Set<ExternalId> resetPeriodCalendars, final BusinessDayConvention resetPeriodBusinessDayConvention, final Frequency resetPeriodFrequency,
      final DateRelativeTo resetDateRelativeTo, final CompoundingMethod compoundingMethod, final Set<ExternalId> fixingDateCalendars,
      final BusinessDayConvention fixedDateBusinessDayConvention, final Integer fixingDateOffset, final OffsetType fixingDateOffsetType, final InterestRateSwapNotional notional,
      final PayReceiveType payReceiveType, final StubCalculationMethod stubCalculationMethod, final DayCount dayCountConvention, final RollConvention rollConvention,
      final Set<ExternalId> maturityDateCalendars, final BusinessDayConvention maturityDateBusinessDayConvention, final Set<ExternalId> paymentDateCalendars,
      final BusinessDayConvention paymentDateBusienssDayConvention, final Frequency paymentDateFrequency, final DateRelativeTo paymentDateRelativeTo, final Integer paymentOffset,
      final Set<ExternalId> accrualPeriodCalendars, final BusinessDayConvention accrualPeriodBusinessDayConvention, final Frequency accrualPeriodFrequency) {
    FloatingInterestRateSwapLeg leg = new FloatingInterestRateSwapLeg();
    leg.setCustomRates(customRates);
    leg.setRateAveragingMethod(rateAveragingMethod);
    leg.setRateCutoffDaysOffset(rateCutoffDaysOffset);
    leg.setCapRate(capRate);
    leg.setFloorRate(floorRate);
    leg.setGearing(gearing);
    leg.setSpreadSchedule(spreadSchedule);
    leg.setSchedule(schedule);
    leg.setFloatingReferenceRateId(floatingReferenceRateId);
    leg.setFloatingRateType(floatingRateType);
    leg.setResetPeriodCalendars(resetPeriodCalendars);
    leg.setResetPeriodBusinessDayConvention(resetPeriodBusinessDayConvention);
    leg.setResetPeriodFrequency(resetPeriodFrequency);
    leg.setResetDateRelativeTo(resetDateRelativeTo);
    if (compoundingMethod != null) {
      leg.setCompoundingMethod(compoundingMethod);
    }
    if (fixingDateCalendars != null) {
      leg.setFixingDateCalendars(fixingDateCalendars);
    }
    leg.setFixingDateBusinessDayConvention(maturityDateBusinessDayConvention);
    if (fixingDateOffset != null) { // keep default zero value if null
      leg.setFixingDateOffset(fixingDateOffset);
    }
    leg.setFixingDateOffsetType(fixingDateOffsetType);
    leg.setNotional(notional);
    leg.setPayReceiveType(payReceiveType);
    leg.setStubCalculationMethod(stubCalculationMethod);
    leg.setDayCountConvention(dayCountConvention);
    if (rollConvention != null) {
      leg.setRollConvention(rollConvention);
    }
    if (maturityDateCalendars != null) {
      leg.setMaturityDateCalendars(maturityDateCalendars);
    }
    leg.setPaymentDateBusinessDayConvention(paymentDateBusienssDayConvention);
    if (paymentDateCalendars != null) {
      leg.setPaymentDateCalendars(paymentDateCalendars);
    }
    leg.setPaymentDateBusinessDayConvention(paymentDateBusienssDayConvention);
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
    return invoke((Rate) parameters[CUSTOM_RATES], (RateAveragingMethod) parameters[RATE_AVERAGING_METHOD], (Integer) parameters[RATE_CUTOFF_DAYS_OFFSET], (Double) parameters[CAP_RATE],
        (Double) parameters[FLOOR_RATE], (Double) parameters[GEARING], (Rate) parameters[SPREAD_SCHEDULE], (FloatingInterestRateSwapLegSchedule) parameters[SCHEDULE],
        (ExternalId) parameters[FLOATING_REFERENCE_RATE_ID], (FloatingRateType) parameters[FLOATING_RATE_TYPE], (Set<ExternalId>) parameters[RESET_PERIOD_CALENDARS],
        (BusinessDayConvention) parameters[RESET_PERIOD_BUSINESS_DAY_CONVENTION], (Frequency) parameters[RESET_PERIOD_FREQUENCY], (DateRelativeTo) parameters[RESET_DATE_RELATIVE_TO],
        (CompoundingMethod) parameters[COMPOUNDING_METHOD], (Set<ExternalId>) parameters[FIXING_DATE_CALENDARS], (BusinessDayConvention) parameters[FIXING_DATE_BUSINESS_DAY_CONVENTION],
        (Integer) parameters[FIXING_DATE_OFFSET], (OffsetType) parameters[FIXING_DATE_OFFSET_TYPE], (InterestRateSwapNotional) parameters[NOTIONAL],
        (PayReceiveType) parameters[PAY_RECEIVE_TYPE], (StubCalculationMethod) parameters[STUB_CALCULATION_METHOD], (DayCount) parameters[DAY_COUNT_CONVENTION],
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
