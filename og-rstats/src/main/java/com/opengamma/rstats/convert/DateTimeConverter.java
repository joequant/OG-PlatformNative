/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.rstats.convert;

import static com.opengamma.language.convert.TypeMap.MAJOR_LOSS;
import static com.opengamma.language.convert.TypeMap.MINOR_LOSS;
import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;
import static com.opengamma.language.definition.types.OpenGammaTypes.EXPIRY;
import static com.opengamma.language.definition.types.OpenGammaTypes.EXPIRY_ALLOW_NULL;
import static com.opengamma.language.definition.types.ThreeTenTypes.INSTANT;
import static com.opengamma.language.definition.types.ThreeTenTypes.INSTANT_ALLOW_NULL;
import static com.opengamma.language.definition.types.ThreeTenTypes.LOCAL_DATE;
import static com.opengamma.language.definition.types.ThreeTenTypes.LOCAL_DATE_ALLOW_NULL;
import static com.opengamma.language.definition.types.ThreeTenTypes.ZONED_DATE_TIME;
import static com.opengamma.language.definition.types.ThreeTenTypes.ZONED_DATE_TIME_ALLOW_NULL;
import static com.opengamma.language.definition.types.TransportTypes.VALUE;
import static com.opengamma.language.definition.types.TransportTypes.VALUE_ALLOW_NULL;
import static org.threeten.bp.temporal.ChronoField.INSTANT_SECONDS;

import java.util.Map;

import org.threeten.bp.DateTimeException;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAccessor;

import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;
import com.opengamma.util.time.Expiry;

/**
 * Converts dates and times to an R representation.
 * <p>
 * The following are represented as strings:
 * <ul>
 * <li>LocalDate
 * </ul>
 * The following are represented as doubles (seconds since 1970-01-01 epoch):
 * <ul>
 * <li>Expiry*
 * <li>Instant
 * <li>ZonedDateTime*
 * </ul>
 * * although sent to R as a double, they can also be received as strings
 */
public class DateTimeConverter extends AbstractTypeConverter {

  private static final TypeMap TO_VALUE = TypeMap.of(ZERO_LOSS, INSTANT, LOCAL_DATE).with(MINOR_LOSS, ZONED_DATE_TIME).with(MAJOR_LOSS, EXPIRY);
  private static final TypeMap TO_VALUE_ALLOW_NULL = TypeMap.of(ZERO_LOSS, INSTANT_ALLOW_NULL, LOCAL_DATE_ALLOW_NULL).with(MINOR_LOSS, ZONED_DATE_TIME_ALLOW_NULL)
      .with(MAJOR_LOSS, EXPIRY_ALLOW_NULL);
  private static final TypeMap FROM_VALUE = TypeMap.of(MINOR_LOSS, VALUE);
  private static final TypeMap FROM_VALUE_ALLOW_NULL = TypeMap.of(MINOR_LOSS, VALUE_ALLOW_NULL);

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      return true;
    } else {
      for (JavaTypeInfo<?> toData : (targetType.isAllowNull() ? TO_VALUE_ALLOW_NULL : TO_VALUE).keySet()) {
        if (clazz == toData.getRawClass()) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if ((value == null) && type.isAllowNull()) {
      conversionContext.setResult(null);
      return;
    }
    final Class<?> clazz = type.getRawClass();
    if (clazz == Value.class) {
      if (value instanceof LocalDate) {
        conversionContext.setResult(ValueUtils.of(((LocalDate) value).toString()));
      } else if (value instanceof TemporalAccessor && ((TemporalAccessor) value).isSupported(INSTANT_SECONDS)) {
        conversionContext.setResult(ValueUtils.of((double) ((TemporalAccessor) value).get(INSTANT_SECONDS)));
      } else {
        conversionContext.setFail();
      }
    } else if (clazz == LocalDate.class) {
      final String str = ((Value) value).getStringValue();
      if (str != null) {
        try {
          conversionContext.setResult(LocalDate.parse(str));
        } catch (DateTimeException e) {
          conversionContext.setFail();
        }
      } else {
        conversionContext.setFail();
      }
    } else if (clazz == Instant.class) {
      final Double epochSeconds = ((Value) value).getDoubleValue();
      if (epochSeconds != null) {
        conversionContext.setResult(Instant.ofEpochSecond(epochSeconds.longValue()));
      } else {
        conversionContext.setFail();
      }
    } else if (clazz == ZonedDateTime.class) {
      final ZonedDateTime zdt = valueToZonedDateTime((Value) value);
      if (zdt != null) {
        conversionContext.setResult(zdt);
      } else {
        conversionContext.setFail();
      }
    } else if (clazz == Expiry.class) {
      final ZonedDateTime zdt = valueToZonedDateTime((Value) value);
      if (zdt != null) {
        conversionContext.setResult(new Expiry(zdt));
      } else {
        conversionContext.setFail();
      }
    } else {
      conversionContext.setFail();
    }
  }

  private static ZonedDateTime valueToZonedDateTime(final Value v) {
    final Double epochSeconds = v.getDoubleValue();
    if (epochSeconds != null) {
      return LocalDateTime.ofEpochSecond(epochSeconds.longValue(), 0, ZoneOffset.UTC).atZone(ZoneOffset.UTC);
    } else {
      final String dateTime = v.getStringValue();
      if (dateTime != null) {
        try {
          return ZonedDateTime.parse(dateTime);
        } catch (DateTimeException e) {
          // Ignore
        }
        try {
          return LocalDate.parse(dateTime).atTime(LocalTime.NOON).atZone(ZoneOffset.UTC);
        } catch (DateTimeException e) {
          // Ignore
        }
      }
    }
    return null;
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final ValueConversionContext conversionContext, final JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      return targetType.isAllowNull() ? TO_VALUE_ALLOW_NULL : TO_VALUE;
    } else {
      return targetType.isAllowNull() ? FROM_VALUE_ALLOW_NULL : FROM_VALUE;
    }
  }

}
