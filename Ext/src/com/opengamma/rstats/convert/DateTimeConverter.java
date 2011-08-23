/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.rstats.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Converts dates and times to an R representation. Strings are used.
 */
public class DateTimeConverter extends AbstractTypeConverter {

  private static final JavaTypeInfo<String> STRING = JavaTypeInfo.builder(String.class).get();
  private static final JavaTypeInfo<LocalDate> LOCAL_DATE = JavaTypeInfo.builder(LocalDate.class).get();
  private static final JavaTypeInfo<String> STRING_ALLOW_NULL = JavaTypeInfo.builder(String.class).allowNull().get();
  private static final JavaTypeInfo<LocalDate> LOCAL_DATE_ALLOW_NULL = JavaTypeInfo.builder(LocalDate.class).allowNull().get();

  private static final TypeMap TO_STRING = TypeMap.of(ZERO_LOSS, LOCAL_DATE);
  private static final TypeMap FROM_STRING = TypeMap.of(ZERO_LOSS, STRING);
  private static final TypeMap TO_STRING_ALLOW_NULL = TypeMap.of(ZERO_LOSS, LOCAL_DATE_ALLOW_NULL);
  private static final TypeMap FROM_STRING_ALLOW_NULL = TypeMap.of(ZERO_LOSS, STRING_ALLOW_NULL);

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == String.class) {
      return true;
    } else {
      for (JavaTypeInfo<?> toData : (targetType.isAllowNull() ? TO_STRING_ALLOW_NULL : TO_STRING).keySet()) {
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
    if (clazz == String.class) {
      if (value instanceof LocalDate) {
        conversionContext.setResult(((LocalDate) value).toString());
      } else {
        conversionContext.setFail();
      }
    } else {
      if (clazz == LocalDate.class) {
        conversionContext.setResult(LocalDate.parse((String) value));
      } else {
        conversionContext.setFail();
      }
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == String.class) {
      return targetType.isAllowNull() ? TO_STRING_ALLOW_NULL : TO_STRING;
    } else {
      return targetType.isAllowNull() ? FROM_STRING_ALLOW_NULL : FROM_STRING;
    }
  }

}
