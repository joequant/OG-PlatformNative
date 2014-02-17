package com.opengamma.language.security;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeParseException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterFactory;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterUtils;
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
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Gets an absolute end date from an absolute start date and a relative end date using conventions and calendars from
 * an index.
 */
public class GetDateUsingIndexFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetDateUsingIndexFunction INSTANCE = new GetDateUsingIndexFunction();
  
  private static final int START_DATE = 0;
  private static final int RELATIVE_END_DATE = 1;
  private static final int INDEX = 2;
  
  private static final Pattern s_immFormat = Pattern.compile("(\\d+)(mimm|qimm|eom)");
  
  private final MetaFunction _meta;
  
  private static List<MetaParameter> parameters() {
    MetaParameter startDate = new MetaParameter("startDate", JavaTypeInfo.builder(LocalDate.class).get());
    MetaParameter relativeEndDate = new MetaParameter("relativeEndDate", JavaTypeInfo.builder(String.class).get());
    MetaParameter index = new MetaParameter("index", JavaTypeInfo.builder(String.class).get());
    return Arrays.asList(startDate, relativeEndDate, index);
  }
  
  protected GetDateUsingIndexFunction() {
    this(new DefinitionAnnotater(GetDateUsingIndexFunction.class));
  }
  
  private GetDateUsingIndexFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.CONVENTION, "GetDateUsingIndex", getParameters(), this));
  }
  
  public static LocalDate invoke(SecuritySource securitySource, ConventionSource conventionSource, HolidaySource holidaySource, RegionSource regionSource, LocalDate startDate, String relativeEndDate, String index) {
    ExternalId indexId = IndexUtils.getIndexId(index);
    IborIndex indexSecurity = IndexUtils.getIndexSecurity(securitySource, indexId);
    IborIndexConvention indexConvention = conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
    Calendar regionCalendar = CalendarUtils.getCalendar(regionSource, holidaySource, indexConvention.getRegionCalendar());
    LocalDate spotDate = ScheduleCalculator.getAdjustedDate(startDate, indexConvention.getSettlementDays(), regionCalendar);
    return getAdjustedDate(indexConvention, regionCalendar, spotDate, relativeEndDate);
  }

  private static LocalDate getAdjustedDate(IborIndexConvention indexConvention, Calendar regionCalendar, LocalDate spotDate, String relativeEndDate) {
    if (StringUtils.isBlank(relativeEndDate)) {
      return null;
    }
    Matcher immMatcher = s_immFormat.matcher(relativeEndDate.toLowerCase());
    if (immMatcher.matches()) {
      int rollCount = Integer.parseInt(immMatcher.group(1));
      String rollType = immMatcher.group(2);
      return getAdjustedDateByIMMRoll(spotDate, rollCount, rollType, regionCalendar);
    }
    if (Character.toLowerCase(relativeEndDate.charAt(0)) != 'p') {
      relativeEndDate = "P" + relativeEndDate;
    }
    try {
      Period period = Period.parse(relativeEndDate);
      return getAdjustedDateByPeriod(indexConvention, regionCalendar, spotDate, period);
    } catch (DateTimeParseException e) {
      // Skip
    }
    throw new OpenGammaRuntimeException("Unknown relative end date format '" + relativeEndDate + "'. See documentation for supported formats.");
  }

  private static LocalDate getAdjustedDateByIMMRoll(LocalDate spotDate, int rollCount, String rollType, Calendar regionCalendar) {
    switch (rollType) {
      case "mimm":
        rollType = RollDateAdjusterFactory.MONTHLY_IMM_ROLL_STRING;
        break;
      case "qimm":
        rollType = RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING;
        break;
      case "eom":
        rollType = RollDateAdjusterFactory.END_OF_MONTH_ROLL_STRING;
        break;
    }
    RollDateAdjuster adjuster = RollDateAdjusterFactory.of(rollType);
    ZonedDateTime adjustedDateTime = RollDateAdjusterUtils.nthDate(spotDate.atStartOfDay(ZoneOffset.UTC), adjuster, rollCount);
    adjustedDateTime = ScheduleCalculator.getAdjustedDate(adjustedDateTime, 0, regionCalendar);
    return adjustedDateTime.toLocalDate();
  }

  private static LocalDate getAdjustedDateByPeriod(IborIndexConvention indexConvention, Calendar regionCalendar, LocalDate spotDate, Period period) {
    ZonedDateTime adjustedDateTime = ScheduleCalculator.getAdjustedDate(spotDate.atStartOfDay(ZoneOffset.UTC), period, indexConvention.getBusinessDayConvention(), regionCalendar);
    return adjustedDateTime.toLocalDate();
  }

  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] arguments) throws AsynchronousExecution {
    return invoke(
        sessionContext.getGlobalContext().getSecuritySource(),
        sessionContext.getGlobalContext().getConventionSource(),
        sessionContext.getGlobalContext().getHolidaySource(),
        sessionContext.getGlobalContext().getRegionSource(),
        (LocalDate) arguments[START_DATE],
        (String) arguments[RELATIVE_END_DATE],
        (String) arguments[INDEX]);
  }
  
  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
