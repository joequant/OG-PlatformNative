/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.security.fra;

import java.util.Arrays;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.language.security.IndexUtils;
import com.opengamma.util.async.AsynchronousExecution;

public class FRASecurityFromIndexFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final FRASecurityFromIndexFunction INSTANCE = new FRASecurityFromIndexFunction();
  
  private static final int START_DATE = 0;
  private static final int END_DATE = 1;
  private static final int NOTIONAL = 2;
  private static final int TRADE_RATE = 3;
  private static final int INDEX = 4;
  
  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    MetaParameter startDate = new MetaParameter("startDate", JavaTypeInfo.builder(LocalDate.class).get());
    MetaParameter endDate = new MetaParameter("endDate", JavaTypeInfo.builder(LocalDate.class).get());
    MetaParameter notional = new MetaParameter("notional", JavaTypeInfo.builder(double.class).get());
    MetaParameter tradeRate = new MetaParameter("tradeRate", JavaTypeInfo.builder(double.class).get());
    MetaParameter index = new MetaParameter("index", JavaTypeInfo.builder(String.class).get());
    return Arrays.asList(startDate, endDate, notional, tradeRate, index);
  }
  
  protected FRASecurityFromIndexFunction() {
    this(new DefinitionAnnotater(FRASecurityFromIndexFunction.class));
  }
  
  private FRASecurityFromIndexFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.SECURITY, "FRASecurityFromIndex", getParameters(), this));
  }

  public static FRASecurity invoke(SecuritySource securitySource, ConventionSource conventionSource, HolidaySource holidaySource, LocalDate startDate, LocalDate endDate, double notional, double tradeRate, String index) {
    ZonedDateTime startDateTime = startDate.atStartOfDay(ZoneOffset.UTC);
    ZonedDateTime endDateTime = endDate.atStartOfDay(ZoneOffset.UTC);
    ExternalId indexId = IndexUtils.getIndexId(index);
    IborIndex indexSecurity = IndexUtils.getIndexSecurity(securitySource, indexId);
    IborIndexConvention indexConvention = conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
    Calendar regionCalendar = new HolidaySourceCalendarAdapter(holidaySource, indexConvention.getRegionCalendar());
    LocalDate fixingDate = ScheduleCalculator.getAdjustedDate(startDate, -1 * indexConvention.getSettlementDays(), regionCalendar);
    ZonedDateTime fixingDateTime = fixingDate.atTime(indexConvention.getFixingTime()).atZone(ZoneId.of(indexConvention.getFixingTimeZone()));
    return new FRASecurity(indexConvention.getCurrency(), indexConvention.getRegionCalendar(), startDateTime, endDateTime, tradeRate, notional, indexId, fixingDateTime);
  }

  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] arguments) throws AsynchronousExecution {
    return invoke(
        sessionContext.getGlobalContext().getSecuritySource(),
        sessionContext.getGlobalContext().getConventionSource(),
        sessionContext.getGlobalContext().getHolidaySource(),
        (LocalDate) arguments[START_DATE],
        (LocalDate) arguments[END_DATE],
        (Double) arguments[NOTIONAL],
        (Double) arguments[TRADE_RATE], 
        (String) arguments[INDEX]);
  }
  
  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }
  
}
