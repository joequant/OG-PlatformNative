/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.datetime;

import org.threeten.bp.ZoneId;

import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts time zone related objects.
 */
public class TimeZoneConverter extends AbstractMappedConverter {

  private static final JavaTypeInfo<ZoneId> ZONE_ID = JavaTypeInfo.builder(ZoneId.class).allowNull().get();
  private static final JavaTypeInfo<String> STRING = JavaTypeInfo.builder(String.class).allowNull().get();

  /**
   * Default instance.
   */
  public static final TimeZoneConverter INSTANCE = new TimeZoneConverter();

  protected TimeZoneConverter() {
    conversion(TypeMap.ZERO_LOSS, ZONE_ID, STRING, new Action<ZoneId, String>() {
      @Override
      protected String convert(final ZoneId value) {
        return value.getId();
      }
    }, new Action<String, ZoneId>() {
      @Override
      protected ZoneId convert(final String value) {
        return ZoneId.of(value);
      }
    });
  }

}
